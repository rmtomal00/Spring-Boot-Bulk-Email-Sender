package com.orbaic.email.config;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Log4j2
public class MailConfig {
    @Value("${spring.sendgrid.api-key}")
    private String apiKey;

    @Value("${spring.sendgrid.from-email}")
    private String fromEmailAddress;

    @Async("backgroundHandle")
    public void sendMail(String to, String subject, String body, boolean isHtmlContent) {
        //System.out.println(fromEmailAddress);
        Email fromEmail = new Email(String.format("Orbaic <%s>", fromEmailAddress));
        Email toEmail = new Email(to);
        Content contentObj = new Content(isHtmlContent ? "text/html" : "text/plain", body);
        Mail mail = new Mail(fromEmail, subject, toEmail, contentObj);

        SendGrid sg = new SendGrid(apiKey);

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() == 202) {
                //System.out.println("Email sent successfully!");
            } else {
                System.err.println("Error sending email: " + response.getStatusCode() + " - " + response.getBody());
                throw new IOException("Failed to send email");
            }
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage());
        }
    }
}
