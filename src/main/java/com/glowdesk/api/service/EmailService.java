package com.glowdesk.api.service;

import com.glowdesk.api.entity.Appointment;
import com.glowdesk.api.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private final RestClient restClient;

    @Value("${mail.from:onboarding@resend.dev}")
    private String fromEmail;

    public EmailService(@Value("${MAIL_PASSWORD}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Async
    public void sendConfirmationEmail(Customer customer, Appointment appointment) {
        send(
            customer.getUser().getEmail(),
            "Your GlowDesk appointment is confirmed!",
            buildConfirmationHtml(customer, appointment)
        );
    }

    @Async
    public void sendReminderEmail(Customer customer, Appointment appointment) {
        send(
            customer.getUser().getEmail(),
            "Reminder: Your appointment starts in 30 minutes",
            buildReminderHtml(customer, appointment)
        );
    }

    private void send(String to, String subject, String html) {
        try {
            restClient.post()
                    .uri("/emails")
                    .body(Map.of(
                            "from", fromEmail,
                            "to", to,
                            "subject", subject,
                            "html", html))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildConfirmationHtml(Customer customer, Appointment appointment) {
        return """
                <html><body>
                <h2>Hi %s,</h2>
                <p>Your appointment has been <strong>confirmed</strong>!</p>
                <table>
                  <tr><td><b>Date</b></td><td>%s</td></tr>
                  <tr><td><b>Time</b></td><td>%s</td></tr>
                  <tr><td><b>Stylist</b></td><td>%s</td></tr>
                  <tr><td><b>Total</b></td><td>&#8377;%s</td></tr>
                </table>
                <p>Please arrive 5 minutes early. See you soon!</p>
                <p>— GlowDesk Team</p>
                </body></html>
                """.formatted(
                customer.getFirstName(),
                appointment.getScheduledDate(),
                appointment.getStartTime(),
                appointment.getStylist().getFirstName() + " " + appointment.getStylist().getLastName(),
                appointment.getTotalPrice());
    }

    private String buildReminderHtml(Customer customer, Appointment appointment) {
        return """
                <html><body>
                <h2>Hi %s,</h2>
                <p>This is a reminder that your appointment starts in <strong>30 minutes</strong>.</p>
                <table>
                  <tr><td><b>Date</b></td><td>%s</td></tr>
                  <tr><td><b>Time</b></td><td>%s</td></tr>
                  <tr><td><b>Stylist</b></td><td>%s</td></tr>
                </table>
                <p>Please start heading to the salon now. See you soon!</p>
                <p>— GlowDesk Team</p>
                </body></html>
                """.formatted(
                customer.getFirstName(),
                appointment.getScheduledDate(),
                appointment.getStartTime(),
                appointment.getStylist().getFirstName() + " " + appointment.getStylist().getLastName());
    }
}
