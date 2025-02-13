package dev.auth.lib.service.email.impl;

import dev.auth.lib.exception.SendEmailException;
import dev.auth.lib.service.email.EmailService;
import dev.auth.lib.service.email.types.EmailFormatter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    private final SpringTemplateEngine templateEngine;

    @Value("${info.app.title}")
    private String applicationTitle;

    @Value("${spring.mail.from}")
    private String from;

    @Override
    public void sendEmail(String to, EmailFormatter emailFormatter) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(emailFormatter.getSubject(applicationTitle));
            helper.setText(emailFormatter.getContent(templateEngine), true);

            emailSender.send(message);

        } catch (MessagingException e) {
            log.error("No se ha podido enviar el correo al usuario: {}", e.getMessage());
            throw new SendEmailException("Fail to send message: " + e.getMessage());
        }
    }
}
