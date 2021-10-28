package com.creditville.notifications.configurations;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
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

    @Bean
    public Mailer mailer(){
        return MailerBuilder.withSMTPServerHost(mailUrl)
                .withSMTPServerPort(mailPort)
                .withSMTPServerUsername(mailUser)
                .withSMTPServerPassword(mailPass)
                .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
    }
}
