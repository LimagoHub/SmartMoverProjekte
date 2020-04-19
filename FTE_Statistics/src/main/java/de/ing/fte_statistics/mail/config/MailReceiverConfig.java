package de.ing.fte_statistics.mail.config;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.ChannelInterceptor;

import de.ing.fte_statistics.email.EmailSplitter;
import de.ing.fte_statistics.email.EmailTransformer;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Pollt Nachrichten im eMail-Postfach und reicht sie an den Handler weiter. Der
 * Schreibt die Attachments ins Filesystem
 * 
 * 
 * Inbound (Mail) -> Splitten in Bestandteile der mail (Body, Excel) -> Outbound
 * (File-System)
 * 
 * @author JoachimWagner
 *
 */
@Configuration
@EnableIntegration
@PropertySource("classpath:mailreceiver.properties")
@Slf4j
public class MailReceiverConfig {

	private static final String LOG_CATEGORY = "de.ing.fte_statistics.mail.config.MailReceiverConfig";

	@Value("${mail.host}")
	private String host;

	@Value("${mail.port}")
	private String port;

	@Value("${mail.username}")
	private String username;

	@Value("${mail.password}")
	private String password;

	@Value("${mail.folder}")
	private String folder;

	@Value("${mail.protocol}")
	private String protocol;

	@Value("${mail.debug}")
	private boolean debug;

	@Value("${mail.auth.debug}")
	private boolean authDebug;

	@Value("${mail.smtp.socketFactory.fallback}")
	private boolean fallback;

	@Value("${mail.imap.socketFactory.class}")
	private String socksocketFactory;

	@Value("${mail.store.protocol}")
	private String storeProtocol;

	@Value("${mail.poller.rate}")
	private long pollerRate;

	@Value("${mail.poller.maxMessagesPerPoll}")
	private long maxMessagesPerPoll;

	/**
	 * erzeugt den Empfänger für das E-Mail Postfach
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	public ImapMailReceiver getImapMailReceiver() throws Exception {

		ImapMailReceiver mailReceiver = new ImapMailReceiver(getMailUrl());
		mailReceiver.setShouldMarkMessagesAsRead(false);
		mailReceiver.setShouldDeleteMessages(false);

		Properties mailProperties = new Properties();
		mailProperties.put("mail.debug", debug);
		mailProperties.put("mail.auth.debug", authDebug);
		mailProperties.put("mail.smtp.socketFactory.fallback", fallback);
		mailProperties.put("mail.imap.socketFactory.class", socksocketFactory);
		mailProperties.put("mail.store.protocol", storeProtocol);
		mailReceiver.setJavaMailProperties(mailProperties);
		mailReceiver.setSimpleContent(true);

		// mailReceiver.afterPropertiesSet();

		return mailReceiver;

	}

	/**
	 * Baut die Zugangsdaten zu einem URL-String zusammen. Format
	 * imaps://username:passwort@mailsserver.de:993/INBOX das der Name eine '@'
	 * enthält, wird er URL-encoded
	 * 
	 */
	private String getMailUrl() throws Exception {
		return new StringBuilder().append(protocol).append("://")
				.append(URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8.toString())).append(":")
				.append(password).append("@").append(host).append(":").append(port).append("/").append(folder)
				.toString();
	}

	/**
	 * Einstiegspunkt der Verarbeitungspipeline in den Mail-Empfang. Der
	 * InboundAdapter polled in Intervallen ob eine Mail bereitsteht und leitet sie
	 * an extractAttachments weiter
	 * 
	 * @param imapMailReceiver
	 * @return
	 */
	@Bean
	public IntegrationFlow polledEmails(ImapMailReceiver imapMailReceiver) {
		
		
		
		return IntegrationFlows
				.from(Mail.imapInboundAdapter(imapMailReceiver).get(),
						e -> e.poller(Pollers.fixedRate(pollerRate).maxMessagesPerPoll(maxMessagesPerPoll)))
				.channel(interceptorChannel())
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY, m -> "Eingehende Mail erkannt. Starte Filter")
				.channel(MessageChannels.direct("incomingMail")).get();
	}

	/**
	 * 
	 * prüft den Maileingang auf richtigen Betreff um Spam auszufilter.
	 * 
	 * @return
	 */
	@Bean
	public IntegrationFlow filterSubject() {
		return IntegrationFlows.from("incomingMail")
				.filter(this,"filterSubject")
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY, m -> "Subject Ok, starte Verarbeitung")
				.channel(MessageChannels.direct("subjectFilter")).get();
	}

	/**
	 * Splittet die Mail in Ihre Fragmente und stößt für jedes Fragment eine eigene
	 * Verarbeitung an (Text, Excel, etc.)
	 * 
	 * @return
	 */
	@Bean
	public IntegrationFlow extractAttachments() {

		return IntegrationFlows.from("subjectFilter")
				.transform(new EmailTransformer(), "transformit")
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY, m -> m.getPayload())
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY, m -> "Payload nach Email-Fragment transformiert")
				.split(new EmailSplitter(), "splitIntoMessages")
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY,m -> "Email in Fragmente zerlegt, starte Verarbeitung der einzelnen Fragmente")
				.channel(MessageChannels.direct("attachments")).get();
	}

	/**
	 * Schreibt die einzelenen Mailfragmente als Dateien weg
	 * 
	 * @return
	 */
	@Bean
	public IntegrationFlow writeAttachmentAsFile() {

		return IntegrationFlows.from("attachments")
				.filter(this, "checkAttachmentIsExcelFile")
								.log(LoggingHandler.Level.INFO, LOG_CATEGORY, m -> "Excelfile erkannt!")
				.handle(Files.outboundAdapter("'target/out/' + headers.directory").autoCreateDirectory(true))
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY, m -> "Excelfile gespeichert!")
				.get();
	}

	@Filter
	public boolean filterSubject(MimeMessage message) {
		try {
			return "FTEReport".equals(message.getSubject());
		} catch (MessagingException e) {
			log.error("Fehler beim ermitteln des Subjects",e);
			return false;
		}
	}
	
	@Filter
	public boolean checkAttachmentIsExcelFile(@Header Optional<String> file_name) {
		
		return file_name.orElse("dummy").matches(".*\\.xls.?");
	}
	
	@Bean
	public DirectChannel interceptorChannel() {
		DirectChannel channel =  new DirectChannel();
		channel.setInterceptors(Collections.singletonList(new ChannelInterceptor() {
			  @Override
			  public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
			    System.out.println("Start");
			    return message;
			  }

			  @Override
			  public void afterSendCompletion(final Message<?> message, final MessageChannel channel, final boolean sent, final Exception ex) {
				  System.out.println("End");
			  }
			}));
		return channel;
	}
	
}
