package com.eventmgmt.event_management.service;

import com.eventmgmt.event_management.entity.Booking;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfTicketService {

    @Autowired
    private QRCodeService qrCodeService;

    private static final DeviceRgb GOLD = new DeviceRgb(232, 201, 126);
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb DARK_BG = new DeviceRgb(10, 6, 8);

    public byte[] generateTicket(Booking booking) throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);

        // Portrait page
        pdfDoc.setDefaultPageSize(PageSize.A5);

        Document document = new Document(pdfDoc);
        document.setMargins(20, 20, 20, 20);

        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        String formattedDate = booking.getEvent().getEventDate()
                .format(DateTimeFormatter.ofPattern("EEE, MMM dd yyyy"));

        String formattedTime = booking.getEvent().getEventDate()
                .format(DateTimeFormatter.ofPattern("hh:mm a"));

        String qrContent = String.format(
                "Booking ID: #%d\nEvent: %s\nAttendee: %s",
                booking.getId(),
                booking.getEvent().getTitle(),
                booking.getUser().getName()
        );

        byte[] qrBytes = qrCodeService.generateQRCode(qrContent, 180, 180);

        Image qrImage = new Image(ImageDataFactory.create(qrBytes))
                .setWidth(140)
                .setHeight(140);

        Table container = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell root = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(DARK_BG)
                .setPadding(20);

        root.add(new Paragraph("EVENT TICKET")
                .setFont(boldFont)
                .setFontSize(18)
                .setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER));

        root.add(new Paragraph("\n"));

        Table mainTable = new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell left = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(DARK_BG);

        left.add(new Paragraph("Event: " + booking.getEvent().getTitle())
                .setFont(boldFont).setFontColor(WHITE));

        left.add(new Paragraph("Date: " + formattedDate)
                .setFont(regularFont).setFontColor(WHITE));

        left.add(new Paragraph("Time: " + formattedTime)
                .setFont(regularFont).setFontColor(WHITE));

        left.add(new Paragraph("Location: " +
                (booking.getEvent().getLocationDetails() == null
                        ? "N/A"
                        : booking.getEvent().getLocationDetails()))
                .setFont(regularFont).setFontColor(WHITE));

        left.add(new Paragraph("\nAttendee: " + booking.getUser().getName())
                .setFont(regularFont).setFontColor(WHITE));

        left.add(new Paragraph("Booking ID: #" + booking.getId())
                .setFont(regularFont).setFontColor(WHITE));

        Cell right = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(DARK_BG);

        right.add(new Paragraph("SCAN")
                .setFont(boldFont)
                .setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER));

        right.add(qrImage);

        mainTable.addCell(left);
        mainTable.addCell(right);

        root.add(mainTable);

        root.add(new Paragraph("\nPresent this ticket at the event entry.")
                .setFont(regularFont)
                .setFontSize(9)
                .setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER));

        container.addCell(root);
        document.add(container);

        document.close();

        return outputStream.toByteArray();
    }
}
