package com.eventmgmt.event_management.service;

import com.eventmgmt.event_management.entity.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PdfTicketService pdfTicketService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            byte[] pdfTicket = pdfTicketService.generateTicket(booking);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("EventManager <priyanshiprasad2004@gmail.com>");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("🎟 Your Ticket for " + booking.getEvent().getTitle());
            helper.setText(buildEmailHtml(booking), true);

            // Important headers that help avoid spam
            message.addHeader("X-Priority", "1");
            message.addHeader("X-Mailer", "EventManager Mailer");
            message.addHeader("Importance", "High");

            helper.addAttachment(
                    "EventManager_Ticket_" + booking.getId() + ".pdf",
                    new org.springframework.core.io.ByteArrayResource(pdfTicket),
                    "application/pdf"
            );

            mailSender.send(message);
            System.out.println("Ticket email sent to: " + booking.getUser().getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    private String buildEmailHtml(Booking booking) {
        String eventName = booking.getEvent().getTitle();
        String attendeeName = booking.getUser().getName();
        String bookingId = "#" + booking.getId();
        String date = booking.getEvent().getEventDate()
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy"));
        String time = booking.getEvent().getEventDate()
                .format(DateTimeFormatter.ofPattern("hh:mm a"));
        String location = booking.getEvent().getLocationDetails() != null
                ? booking.getEvent().getLocationDetails() : "See event details";

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0;padding:0;background:#0a0608;font-family:'Helvetica Neue',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0a0608;padding:40px 20px;">
                <tr><td align="center">
                  <table width="560" cellpadding="0" cellspacing="0" style="background:#13090c;border:1px solid rgba(232,201,126,0.2);">

                    <!-- Gold top bar -->
                    <tr><td style="background:#e8c97e;padding:10px;text-align:center;">
                      <span style="font-size:11px;font-weight:700;color:#1a0e05;letter-spacing:3px;">BOOKING CONFIRMED</span>
                    </td></tr>

                    <!-- Header -->
                    <tr><td style="padding:36px 40px 24px;text-align:center;border-bottom:1px solid rgba(232,201,126,0.1);">
                      <p style="margin:0 0 6px;font-size:28px;font-weight:800;color:#e8c97e;letter-spacing:-0.5px;">EventManager</p>
                      <p style="margin:0;font-size:13px;color:rgba(255,255,255,0.5);">Your ticket is attached to this email</p>
                    </td></tr>

                    <!-- Greeting -->
                    <tr><td style="padding:32px 40px 0;">
                      <p style="margin:0 0 6px;font-size:13px;color:rgba(255,255,255,0.5);letter-spacing:1px;text-transform:uppercase;">Hello,</p>
                      <p style="margin:0;font-size:22px;font-weight:700;color:#fff;">%s</p>
                    </td></tr>

                    <!-- Event name -->
                    <tr><td style="padding:20px 40px 0;">
                      <div style="background:rgba(232,201,126,0.06);border:1px solid rgba(232,201,126,0.2);border-left:3px solid #e8c97e;padding:20px 24px;">
                        <p style="margin:0;font-size:20px;font-weight:700;color:#fff;">%s</p>
                      </div>
                    </td></tr>

                    <!-- Details -->
                    <tr><td style="padding:24px 40px 0;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td style="padding:12px 0;border-bottom:1px solid rgba(255,255,255,0.05);">
                            <p style="margin:0;font-size:10px;color:rgba(255,255,255,0.35);letter-spacing:1.5px;text-transform:uppercase;">Date</p>
                            <p style="margin:4px 0 0;font-size:14px;color:#fff;font-weight:500;">%s</p>
                          </td>
                          <td style="padding:12px 0;border-bottom:1px solid rgba(255,255,255,0.05);">
                            <p style="margin:0;font-size:10px;color:rgba(255,255,255,0.35);letter-spacing:1.5px;text-transform:uppercase;">Time</p>
                            <p style="margin:4px 0 0;font-size:14px;color:#fff;font-weight:500;">%s</p>
                          </td>
                        </tr>
                        <tr>
                          <td colspan="2" style="padding:12px 0;border-bottom:1px solid rgba(255,255,255,0.05);">
                            <p style="margin:0;font-size:10px;color:rgba(255,255,255,0.35);letter-spacing:1.5px;text-transform:uppercase;">Location</p>
                            <p style="margin:4px 0 0;font-size:14px;color:#fff;font-weight:500;">%s</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:12px 0;">
                            <p style="margin:0;font-size:10px;color:rgba(255,255,255,0.35);letter-spacing:1.5px;text-transform:uppercase;">Booking ID</p>
                            <p style="margin:4px 0 0;font-size:18px;font-weight:800;color:#e8c97e;">%s</p>
                          </td>
                          <td style="padding:12px 0;">
                            <p style="margin:0;font-size:10px;color:rgba(255,255,255,0.35);letter-spacing:1.5px;text-transform:uppercase;">Status</p>
                            <p style="margin:4px 0 0;font-size:14px;font-weight:600;color:#4ade80;">✓ Confirmed</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>

                    <!-- PDF note -->
                    <tr><td style="padding:24px 40px;">
                      <div style="background:rgba(74,222,128,0.06);border:1px solid rgba(74,222,128,0.2);padding:16px 20px;text-align:center;">
                        <p style="margin:0;font-size:13px;color:#4ade80;">📎 Your PDF ticket with QR code is attached</p>
                        <p style="margin:6px 0 0;font-size:11px;color:rgba(255,255,255,0.35);">Open the attachment and present it at the event entry</p>
                      </div>
                    </td></tr>

                    <!-- Footer -->
                    <tr><td style="padding:20px 40px 32px;border-top:1px solid rgba(232,201,126,0.1);text-align:center;">
                      <p style="margin:6px 0 0;font-size:10px;color:rgba(255,255,255,0.15);">
                              You received this email because you booked an event on EventManager.
                      </p>
                      <p style="margin:0;font-size:11px;color:rgba(255,255,255,0.25);">EventManager · Your Event Platform</p>
                      <p style="margin:6px 0 0;font-size:10px;color:rgba(255,255,255,0.15);">This is an automated email. Please do not reply.</p>
                    </td></tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                attendeeName,
                eventName,
                date,
                time,
                location,
                bookingId
        );
    }
}
