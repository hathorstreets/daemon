package com.hathor.daemon.services;

import com.hathor.daemon.data.entities.Mint;
import com.hathor.daemon.data.repositories.MintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class MailService {

   private Session session;
   private String from = "hathorstreets@gmail.com";
   private String host = "smtp.gmail.com";
   @Value("${mail.password}")
   private String password;

   Logger logger = LoggerFactory.getLogger(NftService.class);

   private final MintRepository mintRepository;

   public MailService(MintRepository mintRepository) {
      this.mintRepository = mintRepository;
   }

   @PostConstruct
   public void init() {
      Properties properties = System.getProperties();

      properties.put("mail.smtp.host", host);
      properties.put("mail.smtp.port", "465");
      properties.put("mail.smtp.ssl.enable", "true");
      properties.put("mail.smtp.auth", "true");

      this.session = Session.getInstance(properties, new javax.mail.Authenticator() {
         protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(from, password);
         }
      });

//      Mint mint = mintRepository.findById("c953bb77-629a-4384-bb58-c953b961c7c0").get();
//      sendMail(mint);
   }

   public void sendMail(Mint mint) {
      if (mint.getEmail() != null && isValidEmailAddress(mint.getEmail())) {
         try {
            logger.info("Sending mail to " + mint.getEmail());

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mint.getEmail()));
            message.setSubject("Your Hathor Streets are ready!");
            message.setText("See your streets here\n\nhttps://www.hathorstreets.com/mint.html?mint=" + mint.getId());

            Transport.send(message);
            logger.info("Mail sent successfully to " + mint.getEmail());
         } catch (MessagingException mex) {
            logger.error("Could not send mail to " + mint.getEmail(), mex);
         }
      }
   }

   private boolean isValidEmailAddress(String email) {
      boolean result = true;
      try {
         InternetAddress emailAddr = new InternetAddress(email);
         emailAddr.validate();
      } catch (AddressException ex) {
         result = false;
      }
      return result;
   }
}
