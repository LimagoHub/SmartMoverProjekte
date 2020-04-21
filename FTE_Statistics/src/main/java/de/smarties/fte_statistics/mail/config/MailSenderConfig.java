package de.smarties.fte_statistics.mail.config;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import lombok.Setter;



@Configuration
@PropertySource("classpath:mailsender.properties")
@ConfigurationProperties(prefix = "mail")
@Setter
public class MailSenderConfig {
	
	private String host;
	private int port;
	private String username;
	private String password;
	private Properties smtp;
	private String debug;
	private String protocol;
	
	@Bean
	public JavaMailSender getJavaMailSender() {
	    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost(host);
	    mailSender.setPort(port);
	     
	    mailSender.setUsername(username);
	    mailSender.setPassword(password);
	     
	    Properties props = mailSender.getJavaMailProperties();
	    props.put("mail.transport.protocol", protocol);
	    props.put("mail.smtp.auth", smtp.get("auth"));
	    props.put("mail.smtp.starttls.enable", smtp.get("starttls.enable"));
	    props.put("mail.debug", debug);
	     
	    return mailSender;
	}
			
}


