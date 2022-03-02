package com.creditville.notifications.configurations;

import com.creditville.notifications.repositories.RetryLoanRepaymentRepository;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfiguration {
    @Value("${mail.smt.user}")
    String mailUser;
    @Value("${mail.smt.pass}")
    String mailPass;
    @Value("${mail.smt.url}")
    String mailUrl;
    @Value("${mail.smt.port}")
    Integer mailPort;

    @Value("${redwood.mail.smt.user}")
    String redwoodmailUser;
    @Value("${redwood.mail.smt.pass}")
    String redwoodmailPass;
    @Value("${redwood.mail.smt.url}")
    String redwoodmailUrl;
    @Value("${redwood.mail.smt.port}")
    Integer redwoodmailPort;
//
//    @Bean
//    public Mailer mailers(){
//        return null;
//
////      return MailerBuilder.withSMTPServerHost(mailUrl)
////                .withSMTPServerPort(mailPort)
////                .withSMTPServerUsername(mailUser)
////                .withSMTPServerPassword(mailPass)
////                .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
////        return MailerBuilder.withSMTPServerHost(redwoodmailUrl)
////                .withSMTPServerPort(redwoodmailPort)
////                .withSMTPServerUsername(redwoodmailUser)
////                .withSMTPServerPassword(redwoodmailPass)
////                .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
//    }
//
//
//    @Bean
//    public Mailer redWoodMailer(){
//        return MailerBuilder.withSMTPServerHost(redwoodmailUrl)
//                .withSMTPServerPort(redwoodmailPort)
//                .withSMTPServerUsername(redwoodmailUser)
//                .withSMTPServerPassword(redwoodmailPass)
//                .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
//    }

}
