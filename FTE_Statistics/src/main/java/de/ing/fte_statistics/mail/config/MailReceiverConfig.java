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
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.dsl.Mail;

import de.ing.fte_statistics.email.EmailSplitter;
import de.ing.fte_statistics.email.EmailTransformer;

/**
 * 
 * Pollt Nachrichten im eMail-Postfach und reicht sie an den Handler weiter. Der Schreibt die Attachments in Filesystem
 * 
 * 
 * @author JoachimWagner
 *
 */
@Configuration
@EnableIntegration
@PropertySource("classpath:mailreceiver.properties")
public class MailReceiverConfig {

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

	private String getMailUrl() throws Exception {
		return new StringBuilder().append(protocol).append("://")
				.append(URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8.toString())).append(":")
				.append(password).append("@").append(host).append(":").append(port).append("/").append(folder)
				.toString();
	}

	@Bean

	public IntegrationFlow polledEmails(ImapMailReceiver imapMailReceiver) {
		return IntegrationFlows
				.from(Mail.imapInboundAdapter(imapMailReceiver).get(),
						e -> e.poller(Pollers.fixedRate(pollerRate).maxMessagesPerPoll(maxMessagesPerPoll)))
				.channel(MessageChannels.direct("incomingMail")).get();
	}

	@Bean
	public IntegrationFlow extractAttachments() {

		return IntegrationFlows.from("incomingMail").transform(new EmailTransformer(), "transformit")
				.split(new EmailSplitter(), "splitIntoMessages")
				.channel(MessageChannels.direct("attachments"))
				.get();
	}

	@Bean
	public IntegrationFlow writeAttachmentAsFile() {

		return IntegrationFlows.from("attachments")
				// save attachment to file
				.handle(Files.outboundAdapter("'target/out/' + headers.directory")
				.autoCreateDirectory(true)).get();
	}


}
