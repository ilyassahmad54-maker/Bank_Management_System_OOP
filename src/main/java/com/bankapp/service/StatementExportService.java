package com.bankapp.service;

import com.bankapp.model.Account;
import com.bankapp.model.Transaction;
import com.bankapp.model.User;
import com.bankapp.util.CurrencyFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatementExportService {
    private static final Logger log = LoggerFactory.getLogger(StatementExportService.class);
    private static final DateTimeFormatter FMT      = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private static final float MARGIN     = 50f;
    private static final float PAGE_H     = PDRectangle.A4.getHeight();
    private static final float PAGE_W     = PDRectangle.A4.getWidth();
    private static final float ROW_HEIGHT = 14f;
    private static final float MIN_Y      = 60f;

    /**
     * Generates a PDF bank statement and saves it to outputPath.
     * @return the saved File on success, null on failure.
     */
    public File exportStatement(User user, Account account,
                                List<Transaction> transactions, String outputPath) {
        try (PDDocument doc = new PDDocument()) {
            PDType1Font bold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // We manage pages manually so we can open/close streams correctly
            PDPage firstPage = new PDPage(PDRectangle.A4);
            doc.addPage(firstPage);

            PDPageContentStream cs = new PDPageContentStream(doc, firstPage);
            float y = PAGE_H - MARGIN;

            // ── Header ────────────────────────────────────────────────────────
            y = writeLine(cs, bold, 20, MARGIN, y, "NeoBank — Account Statement");
            y -= 4;
            y = writeLine(cs, regular, 10, MARGIN, y,
                    "Generated: " + LocalDateTime.now().format(DATE_FMT));
            y -= 6;
            drawLine(cs, y); y -= 14;

            // ── Account Info ──────────────────────────────────────────────────
            y = writeKV(cs, bold, regular, 11, MARGIN, y, "Account Holder:", user.getUsername());
            y = writeKV(cs, bold, regular, 11, MARGIN, y, "Account Number:", account.getAccountNumber());
            y = writeKV(cs, bold, regular, 11, MARGIN, y, "Account Type:",   account.getAccountType());
            y = writeKV(cs, bold, regular, 11, MARGIN, y, "Current Balance:",
                    CurrencyFormatter.format(account.getBalanceCents()));
            y -= 6;
            drawLine(cs, y); y -= 14;

            // ── Table Header ──────────────────────────────────────────────────
            cs.setFont(bold, 10);
            writeAt(cs, MARGIN,       y, "Date");
            writeAt(cs, MARGIN + 130, y, "Type");
            writeAt(cs, MARGIN + 200, y, "Description");
            writeAt(cs, PAGE_W - MARGIN - 70, y, "Amount");
            y -= 6;
            drawLine(cs, y); y -= ROW_HEIGHT;

            // ── Transaction Rows ──────────────────────────────────────────────
            for (Transaction t : transactions) {
                if (y < MIN_Y) {
                    cs.close();
                    PDPage nextPage = new PDPage(PDRectangle.A4);
                    doc.addPage(nextPage);
                    cs = new PDPageContentStream(doc, nextPage);
                    y = PAGE_H - MARGIN;
                    // Repeat column headers on new page
                    cs.setFont(bold, 10);
                    writeAt(cs, MARGIN,       y, "Date");
                    writeAt(cs, MARGIN + 130, y, "Type");
                    writeAt(cs, MARGIN + 200, y, "Description");
                    writeAt(cs, PAGE_W - MARGIN - 70, y, "Amount");
                    y -= 6;
                    drawLine(cs, y); y -= ROW_HEIGHT;
                }

                cs.setFont(regular, 9);
                String date = t.getTimestamp() != null ? t.getTimestamp().format(FMT) : "";
                String desc = truncate(t.getDescription() != null ? t.getDescription() : "", 28);
                boolean isCredit = isCredit(t);
                String amt = (isCredit ? "+" : "-") + CurrencyFormatter.format(t.getAmountCents());

                writeAt(cs, MARGIN,       y, date);
                writeAt(cs, MARGIN + 130, y, t.getType());
                writeAt(cs, MARGIN + 200, y, desc);
                writeAt(cs, PAGE_W - MARGIN - 70, y, amt);
                y -= ROW_HEIGHT;
            }

            cs.close();

            File out = new File(outputPath);
            doc.save(out);
            log.info("Statement exported: {}", out.getAbsolutePath());
            return out;
        } catch (IOException e) {
            log.error("Failed to export statement to {}", outputPath, e);
            return null;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isCredit(Transaction t) {
        if ("Deposit".equals(t.getType())) return true;
        if ("Transfer".equals(t.getType()) && t.getDescription() != null
                && t.getDescription().startsWith("Transfer from")) return true;
        return false;
    }

    private float writeLine(PDPageContentStream cs, PDType1Font font, float size,
                             float x, float y, String text) throws IOException {
        cs.setFont(font, size);
        writeAt(cs, x, y, text);
        return y - size - 4;
    }

    private float writeKV(PDPageContentStream cs, PDType1Font bold, PDType1Font regular,
                           float size, float x, float y, String key, String value) throws IOException {
        cs.setFont(bold, size);
        writeAt(cs, x, y, key);
        cs.setFont(regular, size);
        writeAt(cs, x + 120, y, value);
        return y - size - 4;
    }

    private void writeAt(PDPageContentStream cs, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private void drawLine(PDPageContentStream cs, float y) throws IOException {
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(PAGE_W - MARGIN, y);
        cs.stroke();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "...";
    }
}
