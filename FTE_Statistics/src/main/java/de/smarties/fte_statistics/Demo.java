package de.smarties.fte_statistics;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.smarties.fte_statistics.mail.MailSender;

@Component
public class Demo {

	@Autowired
	private MailSender sender;
	
	
	@PostConstruct
	public void init() {
		sender.sendMail("jowagner@limago.de", "allerletzter Test", this.getClass().getName()+"\n\n");
	}
}
