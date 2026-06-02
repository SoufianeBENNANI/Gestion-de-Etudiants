package org.sid.gestion_etudiant.Metier.ServiceImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Entity.Payement;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.Repository.PayementRepo;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.sid.gestion_etudiant.Metier.Service.PayementService;
import org.sid.gestion_etudiant.Metier.dto.PayementDTO;
import org.sid.gestion_etudiant.Metier.exception.PayementNotFoundException;
import org.sid.gestion_etudiant.Metier.exception.StudentNotFoundException;
import org.sid.gestion_etudiant.Metier.mapper.PayementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class PayementServiceImpl implements PayementService {

    private final PayementRepo payementRepo;
    private final StudentRepo studentRepo;

    @Override
    public PayementDTO addPayement(PayementDTO payementDTO) {
        Student student = studentRepo.findById(payementDTO.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        Payement payement = PayementMapper.toEntity(payementDTO);

        payement.setStudent(student);
        payement.setArchived(false);
        payement.setArchivedAt(null);

        return PayementMapper.toDto(payementRepo.save(payement));
    }

    @Override
    public List<PayementDTO> getAllPayements() {
        return payementRepo.findByArchivedFalse()
                .stream()
                .map(PayementMapper::toDto)
                .toList();
    }

    @Override
    public List<PayementDTO> getArchivedPayements() {
        return payementRepo.findByArchivedTrue()
                .stream()
                .map(PayementMapper::toDto)
                .toList();
    }

    @Override
    public PayementDTO getPayementById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Le champ ID est obligatoire");
        }

        Payement payement = payementRepo.findById(id)
                .orElseThrow(() -> new PayementNotFoundException("Payement non trouvée avec id : " + id));

        return PayementMapper.toDto(payement);
    }

    @Override
    public PayementDTO updatePayement(Long id, PayementDTO payementDTO) {
        Payement payement = payementRepo.findById(id)
                .orElseThrow(() -> new PayementNotFoundException("Payement non trouvée avec id : " + id));

        if (payement.isArchived()) {
            throw new RuntimeException("Impossible de modifier un payement archivé. Restaurer d'abord.");
        }

        payement.setAmount(payementDTO.getAmount());
        payement.setDate(payementDTO.getDate());
        payement.setStatus(payementDTO.getStatus());

        if (payementDTO.getStudentId() != null) {
            Student student = studentRepo.findById(payementDTO.getStudentId())
                    .orElseThrow(() -> new StudentNotFoundException("Student not found"));
            payement.setStudent(student);
        }

        return PayementMapper.toDto(payementRepo.save(payement));
    }

    @Override
    public String deletePayement(Long id) {
        Payement payement = payementRepo.findById(id)
                .orElseThrow(() -> new PayementNotFoundException("Payement non trouvée avec l'id : " + id));

        if (payement.isArchived()) {
            return "Payement déjà archivé avec id : " + id;
        }

        payement.setArchived(true);
        payement.setArchivedAt(LocalDateTime.now());

        payementRepo.save(payement);

        return "Payement archivé avec succès. Il sera supprimé définitivement après 7 jours.";
    }

    @Override
    public PayementDTO restorePayement(Long id) {
        Payement payement = payementRepo.findById(id)
                .orElseThrow(() -> new PayementNotFoundException("Payement non trouvée avec l'id : " + id));

        if (!payement.isArchived()) {
            throw new RuntimeException("Ce payement n'est pas archivé.");
        }

        payement.setArchived(false);
        payement.setArchivedAt(null);

        return PayementMapper.toDto(payementRepo.save(payement));
    }

    @Override
    @Transactional
    public void deleteOldArchivedPayements() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<Payement> oldArchivedPayements =
                payementRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        payementRepo.deleteAll(oldArchivedPayements);
    }

    @Override
    public byte[] generatePayementsPdf() {
        try {
            List<Payement> payements = payementRepo.findByArchivedFalse();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, new Color(15, 23, 42));
            Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(100, 116, 139));
            Font infoFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(30, 41, 59));
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(30, 41, 59));
            Font footerFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(100, 116, 139));

            Paragraph title = new Paragraph("Payements List", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(6);
            document.add(title);

            String date = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            Paragraph subtitle = new Paragraph("Generated on: " + date, subtitleFont);
            subtitle.setAlignment(Paragraph.ALIGN_CENTER);
            subtitle.setSpacingAfter(18);
            document.add(subtitle);

            double totalAmount = payements.stream()
                    .mapToDouble(payement -> payement.getAmount() != null ? payement.getAmount() : 0)
                    .sum();

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1f, 1f});
            infoTable.setSpacingAfter(18);

            addInfoCell(infoTable, "Active payements: " + payements.size(), infoFont);
            addInfoCell(infoTable, "Total amount: " + totalAmount + " DH", infoFont);

            document.add(infoTable);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2f, 2f, 2f, 2f});

            addTableHeader(table, headerFont, "Student");
            addTableHeader(table, headerFont, "Amount");
            addTableHeader(table, headerFont, "Date");
            addTableHeader(table, headerFont, "Status");
            addTableHeader(table, headerFont, "Archived");

            int index = 0;

            for (Payement payement : payements) {
                Color rowColor = index % 2 == 0
                        ? new Color(248, 250, 252)
                        : Color.WHITE;

                Student student = payement.getStudent();

                String studentName = student != null
                        ? student.getPrenom() + " " + student.getNom()
                        : "N/A";

                addTableCell(table, bodyFont, studentName, rowColor);
                addTableCell(
                        table,
                        bodyFont,
                        payement.getAmount() != null ? payement.getAmount() + " DH" : "0 DH",
                        rowColor
                );
                addTableCell(
                        table,
                        bodyFont,
                        payement.getDate() != null ? payement.getDate().toString() : "",
                        rowColor
                );
                addTableCell(
                        table,
                        bodyFont,
                        payement.getStatus() != null ? payement.getStatus().toString() : "",
                        rowColor
                );
                addTableCell(
                        table,
                        bodyFont,
                        payement.isArchived() ? "Yes" : "No",
                        rowColor
                );

                index++;
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Student Management System - Official Payements List",
                    footerFont
            );
            footer.setAlignment(Paragraph.ALIGN_CENTER);
            footer.setSpacingBefore(18);
            document.add(footer);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error while generating payements PDF", e);
        }
    }

    private void addInfoCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(12);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setBackgroundColor(new Color(241, 245, 249));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, Font font, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(9);
        cell.setBackgroundColor(new Color(15, 23, 42));
        cell.setBorderColor(new Color(15, 23, 42));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, Font font, String text, Color backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(8);
        cell.setBackgroundColor(backgroundColor);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}