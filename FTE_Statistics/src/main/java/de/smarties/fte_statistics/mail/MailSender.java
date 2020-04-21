package de.smarties.fte_statistics.mail;

public interface MailSender {

	void sendMail(String to, String subject, String message);

}