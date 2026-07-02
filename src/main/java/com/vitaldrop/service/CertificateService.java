package com.vitaldrop.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
public class CertificateService {

    public byte[] generateCertificate(
            String donorName,
            String bloodGroup,
            String donationDate,
            String certificateNumber
    ) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Landscape Orientation
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        com.lowagie.text.pdf.PdfContentByte canvas = writer.getDirectContent();
        Rectangle page = document.getPageSize();

        Color primaryRed = new Color(178, 34, 34); // Premium Firebrick Red
        Color goldAccent = new Color(212, 175, 55); // Metallic Gold Accent

        /* --- BORDERS & CORNER ORNAMENTS --- */
        // Outer Thick Border
        canvas.setColorStroke(primaryRed);
        canvas.setLineWidth(4f);
        canvas.rectangle(25, 25, page.getWidth() - 50, page.getHeight() - 50);
        canvas.stroke();

        // Inner Thin Border
        canvas.setColorStroke(goldAccent);
        canvas.setLineWidth(1.5f);
        canvas.rectangle(33, 33, page.getWidth() - 66, page.getHeight() - 66);
        canvas.stroke();

        // Corner Accents
        canvas.setColorFill(primaryRed);
        float offset = 33f;
        float size = 20f;
        canvas.rectangle(offset, page.getHeight() - offset - size, size, size);
        canvas.rectangle(page.getWidth() - offset - size, page.getHeight() - offset - size, size, size);
        canvas.rectangle(offset, offset, size, size);
        canvas.rectangle(page.getWidth() - offset - size, offset, size, size);
        canvas.fill();


        /* --- FONTS CONFIGURATION (Optimized sizes for single-page fit) --- */
        BaseFont cinzel = BaseFont.createFont("src/main/resources/static/fonts/Cinzel-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont cinzelSemiBold = BaseFont.createFont("src/main/resources/static/fonts/Cinzel-SemiBold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont playfair = BaseFont.createFont("src/main/resources/static/fonts/PlayfairDisplay-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont lora = BaseFont.createFont("src/main/resources/static/fonts/Lora-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Font titleFont = new Font(cinzel, 32, Font.NORMAL, primaryRed);
        Font headingFont = new Font(cinzelSemiBold, 16, Font.NORMAL, Color.BLACK);
        Font awardedFont = new Font(lora, 12, Font.ITALIC, Color.DARK_GRAY);
        Font nameFont = new Font(playfair, 30, Font.NORMAL, new Color(115, 0, 0));
        Font bodyFont = new Font(lora, 13, Font.NORMAL, Color.BLACK);
        Font labelFont = new Font(lora, 11, Font.BOLD, Color.DARK_GRAY);
        Font valueFont = new Font(lora, 11, Font.NORMAL, Color.BLACK);
        Font quoteFont = new Font(lora, 13, Font.ITALIC, Color.GRAY);
        Font footerFont = new Font(Font.TIMES_ROMAN, 9, Font.ITALIC, Color.GRAY);


        /* --- BRANDING LOGO --- */
        try {
            Image logo = Image.getInstance("src/main/resources/static/images/logo.png");
            logo.scaleToFit(65, 65); // Slightly smaller logo
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.setSpacingAfter(2);
            document.add(logo);
        } catch (Exception e) {
            Paragraph spacer = new Paragraph(" ");
            spacer.setSpacingAfter(10);
            document.add(spacer);
        }


        /* --- TITLE & DIVIDER --- */
        Paragraph title = new Paragraph("VITAL DROP", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        LineSeparator line = new LineSeparator(1.5f, 25f, goldAccent, Element.ALIGN_CENTER, -5f);

// Add it sequentially so it flows naturally between the title and heading
        document.add(line);


        /* --- HEADER --- */
        Paragraph heading = new Paragraph("CERTIFICATE OF APPRECIATION", headingFont);
        heading.setAlignment(Element.ALIGN_CENTER);
        heading.setSpacingBefore(5);
        heading.setSpacingAfter(20);
        document.add(heading);


        /* --- CONTENT SECTION --- */
        Paragraph awarded = new Paragraph("This certificate is proudly presented to", awardedFont);
        awarded.setAlignment(Element.ALIGN_CENTER);
        awarded.setSpacingAfter(10);
        document.add(awarded);

        Paragraph name = new Paragraph(donorName, nameFont);
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingAfter(15);
        document.add(name);

        String bodyText = "In recognition of your selfless generosity and compassionate contribution\n"
                + "towards saving lives through voluntary blood donation.\n\n"
                + "Your kindness inspires hope and strengthens our community.";
        Paragraph body = new Paragraph(bodyText, bodyFont);
        body.setAlignment(Element.ALIGN_CENTER);
        body.setLeading(16f); // Tightened line height
        body.setSpacingAfter(25);
        document.add(body);

        Paragraph quote = new Paragraph("\"Every Drop Saves a Life.\"", quoteFont);
        quote.setAlignment(Element.ALIGN_CENTER);
        quote.setSpacingAfter(30); // Reduced space before the signature block
        document.add(quote);


        /* --- GRID FOOTER (METADATA & SIGNATURE) --- */
        PdfPTable bottomTable = new PdfPTable(2);
        bottomTable.setWidthPercentage(82);
        bottomTable.setTotalWidth(new float[]{55f, 45f});

        // Left Side: Info Block
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setVerticalAlignment(Element.ALIGN_BOTTOM);

        Phrase bgPhrase = new Phrase();
        bgPhrase.add(new Chunk("Blood Group: ", labelFont));
        bgPhrase.add(new Chunk(bloodGroup, valueFont));

        Phrase ddPhrase = new Phrase();
        ddPhrase.add(new Chunk("Donation Date: ", labelFont));
        ddPhrase.add(new Chunk(donationDate, valueFont));

        Phrase cnPhrase = new Phrase();
        cnPhrase.add(new Chunk("Certificate No: ", labelFont));
        cnPhrase.add(new Chunk(certificateNumber, valueFont));

        Paragraph metadataParagraph = new Paragraph();
        metadataParagraph.setLeading(15f);
        metadataParagraph.add(bgPhrase);
        metadataParagraph.add(Chunk.NEWLINE);
        metadataParagraph.add(ddPhrase);
        metadataParagraph.add(Chunk.NEWLINE);
        metadataParagraph.add(cnPhrase);

        leftCell.addElement(metadataParagraph);
        bottomTable.addCell(leftCell);

        // Right Side: Signature Block
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setVerticalAlignment(Element.ALIGN_BOTTOM);

        rightCell.setPaddingRight(20f);
        rightCell.setPaddingTop(20f);

        try {
            Image signatureImg = Image.getInstance("src/main/resources/static/images/signature.png");

            // Increased bounds from (100, 35) to (160, 60) for a highly realistic signature scale
            signatureImg.scaleToFit(190, 75);

            signatureImg.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(signatureImg);
        } catch (Exception e) {
            Paragraph space = new Paragraph(" ");
            space.setSpacingAfter(15);
            rightCell.addElement(space);
        }

        Paragraph signText = new Paragraph("_____________________\nDirector\nVital Drop Organization", valueFont);
        signText.setAlignment(Element.ALIGN_RIGHT);
        signText.setLeading(14f);

        rightCell.addElement(signText);
        bottomTable.addCell(rightCell);

        document.add(bottomTable);


        /* --- GLOBAL FOOTER LINK --- */
        Paragraph footer = new Paragraph("www.vitaldrop.org", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(15); // Snug fit to pull it back up onto page 1
        document.add(footer);

        document.close();
        return out.toByteArray();
    }
}