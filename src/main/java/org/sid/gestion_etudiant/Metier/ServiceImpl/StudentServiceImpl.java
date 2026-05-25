package org.sid.gestion_etudiant.Metier.ServiceImpl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.sid.gestion_etudiant.Metier.Service.StudentService;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.dto.StudentDTO;
import org.sid.gestion_etudiant.Metier.exception.StudentNotFoundException;
import org.sid.gestion_etudiant.Metier.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.time.format.DateTimeFormatter;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepo studentRepo;

    @Override
    public StudentDTO addStudent(StudentDTO dto) {
        Student student = StudentMapper.toEntity(dto);
        return StudentMapper.toDTO(studentRepo.save(student));
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        return studentRepo.findByArchivedFalse()
                .stream()
                .map(StudentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getStudentsByNom(String nom) {

        List<Student> students =
                studentRepo.findByNomContainsIgnoreCaseAndArchivedFalse(nom);

        if (students.isEmpty()) {
            throw new StudentNotFoundException("No active students found with name: " + nom);
        }

        return students.stream()
                .map(StudentMapper::toDTO)
                .toList();
    }

    @Override
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        student.setNom(dto.getNom());
        student.setPrenom(dto.getPrenom());
        student.setEmail(dto.getEmail());
        student.setAdresse(dto.getAdresse());
        student.setTelephone(dto.getTelephone());
        student.setGenre(dto.getGenre());
        student.setDate_Naissance(dto.getDate_Naissance());

        return StudentMapper.toDTO(studentRepo.save(student));
    }

    @Override
    public String deleteStudent(Long id) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + id));

        if (student.isArchived()) {
            return "Student already archived with id " + id;
        }

        student.setArchived(true);
        student.setArchivedAt(LocalDateTime.now());

        studentRepo.save(student);

        return "Student archived successfully with id " + id + ". It will be permanently deleted after 7 days.";
    }

    @Override
    public List<StudentDTO> getArchivedStudents() {
        return studentRepo.findByArchivedTrue()
                .stream()
                .map(StudentMapper::toDTO)
                .toList();
    }

    @Override
    public StudentDTO restoreStudent(Long id) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + id));

        if (!student.isArchived()) {
            throw new RuntimeException("Student is not archived");
        }

        student.setArchived(false);
        student.setArchivedAt(null);

        return StudentMapper.toDTO(studentRepo.save(student));
    }

    @Transactional
    @Override
    public void deleteOldArchivedStudents() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<Student> oldArchivedStudents =
                studentRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        studentRepo.deleteAll(oldArchivedStudents);
    }

    @Override
    public byte[] generateStudentsPdf() {
        try {
            List<Student> students = studentRepo.findByArchivedFalse();

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

            Paragraph title = new Paragraph("Students List", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(6);
            document.add(title);

            String date = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            Paragraph subtitle = new Paragraph("Generated on: " + date, subtitleFont);
            subtitle.setAlignment(Paragraph.ALIGN_CENTER);
            subtitle.setSpacingAfter(18);
            document.add(subtitle);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1f, 1f});
            infoTable.setSpacingAfter(18);

            addInfoCell(infoTable, "Active students: " + students.size(), infoFont);
            addInfoCell(infoTable, "Format: Printable PDF", infoFont);

            document.add(infoTable);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.7f, 1.7f, 3f, 1.8f, 1.4f, 2.6f});

            addTableHeader(table, headerFont, "Last Name");
            addTableHeader(table, headerFont, "First Name");
            addTableHeader(table, headerFont, "Email");
            addTableHeader(table, headerFont, "Phone");
            addTableHeader(table, headerFont, "Gender");
            addTableHeader(table, headerFont, "Address");

            int index = 0;

            for (Student student : students) {
                Color rowColor = index % 2 == 0
                        ? new Color(248, 250, 252)
                        : Color.WHITE;

                addTableCell(table, bodyFont, student.getNom(), rowColor);
                addTableCell(table, bodyFont, student.getPrenom(), rowColor);
                addTableCell(table, bodyFont, student.getEmail(), rowColor);
                addTableCell(table, bodyFont, student.getTelephone(), rowColor);
                addTableCell(
                        table,
                        bodyFont,
                        student.getGenre() != null ? student.getGenre().name() : "",
                        rowColor
                );
                addTableCell(table, bodyFont, student.getAdresse(), rowColor);

                index++;
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Student Management System - Official Students List",
                    footerFont
            );
            footer.setAlignment(Paragraph.ALIGN_CENTER);
            footer.setSpacingBefore(18);
            document.add(footer);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error while generating students PDF", e);
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