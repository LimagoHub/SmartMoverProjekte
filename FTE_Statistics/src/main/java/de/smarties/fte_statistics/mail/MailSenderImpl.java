package de.smarties.fte_statistics.mail;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailSenderImpl implements MailSender {

	private final JavaMailSender javaMailSender;
	private final String username;

	@Autowired
	public MailSenderImpl(JavaMailSender javaMailSender, @Value("${mail.username}") String username) {
		this.javaMailSender = javaMailSender;
		this.username = username;
	}
	
	
	@Override
	public void sendMail(String to, String subject, String message) {
		try {
			MimeMessage msg = javaMailSender.createMimeMessage();

			// true = multipart message
			MimeMessageHelper helper = new MimeMessageHelper(msg, false);
			
			helper.setFrom(username);
			
			helper.setTo(to);

			helper.setSubject(subject);

			// default = text/plain
			helper.setText(message);

			// true = text/html
			//helper.setText(message, true);

			// hard coded a file path
			//FileSystemResource file = new FileSystemResource(new File("path/android.png"));

			//helper.addAttachment("my_photo.png", new ClassPathResource("android.png"));

			javaMailSender.send(msg);
		} catch (Exception e) {
			log.error("Fehler beim Senden der Mail",e);
		} 
	}
	
	
}
