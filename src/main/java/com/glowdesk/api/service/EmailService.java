package com.glowdesk.api.service;

import com.glowdesk.api.entity.Appointment;
import com.glowdesk.api.entity.Customer;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from:onboarding@resend.dev}")
    private String fromEmail;

    @Async
    public void sendConfirmationEmail(Customer customer, Appointment appointment) {
        String to = customer.getUser().getEmail();
        String subject = "Your GlowDesk appointment is confirmed!";
        String body = buildConfirmationHtml(customer, appointment);
        send(to, subject, body);
    }

    @Async
    public void sendReminderEmail(Customer customer, Appointment appointment) {
        String to = customer.getUser().getEmail();
        String subject = "Reminder: Your appointment starts in 30 minutes";
        String body = buildReminderHtml(customer, appointment);
        send(to, subject, body);
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
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
                  <tr><td><b>Total</b></td><td>₹%s</td></tr>
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
