package de.ing.fte_statistics.mail.config;

import java.net.URLEncoder;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.dsl.Mail;

import de.ing.fte_statistics.email.EmailSplitter;
import de.ing.fte_statistics.email.EmailTransformer;

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
public class MailReceiverConfig {

	private static final String LOG_CATEGORY = "de.ing.fte_statistics.mail.config.MailReceiverConfig";

	@Value("${mail.host}")
	private String host;

	@Value("${mail.inputPort}")
	private String inputPort;

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
	 * Einstiegspunkt der Verarbeitungspipeline in den Mail-Empfang. 
	 * 
	 * @param imapMailReceiver
	 * @return
	 */
	@Bean
	public IntegrationFlow polledEmails(ImapMailReceiver imapMailReceiver) {

		return IntegrationFlows
				.from(Mail.imapInboundAdapter(imapMailReceiver).get(),
						e -> e.poller(Pollers.fixedRate(pollerRate).maxMessagesPerPoll(maxMessagesPerPoll)))

				.enrichHeaders(s -> s.headerExpressions(h -> h.put("subject", "payload.subject").put("from", "payload.from[0].toString()")))
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY,m -> String.format("Mail mit Subject='%s' von '%s' empfangen.", m.getHeaders().get("subject"),m.getHeaders().get("from")))
				.filter("headers['subject']=='FTEReport'")
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY, m -> "Subject Ok, starte Verarbeitung")
				.transform(new EmailTransformer(), "transformit")
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY, m -> "Payload nach Email-Fragment transformiert")
				.split(new EmailSplitter(), "splitIntoMessages")
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY,m -> "Email in Fragmente und Attachments in Dateien zerlegt, starte Verarbeitung der einzelnen Fragmente")
				.filter("headers['file_name'] matches '.*\\.xls.?'")
				// .enrichHeaders(s -> s.headerExpressions(h -> h.put("excelAttachmentPresent",
				// "true")))
				.log(LoggingHandler.Level.INFO, LOG_CATEGORY, m -> "Excelfile erkannt!")
				.handle(Files.outboundAdapter("'target/out/' + headers.directory").autoCreateDirectory(true)).get();
				
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
				.append(password).append("@").append(host).append(":").append(inputPort).append("/").append(folder)
				.toString();
	}

}
