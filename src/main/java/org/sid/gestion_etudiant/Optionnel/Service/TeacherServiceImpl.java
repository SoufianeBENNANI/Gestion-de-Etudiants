package org.sid.gestion_etudiant.Optionnel.Service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Optionnel.DTO.TeacherDTO;
import org.sid.gestion_etudiant.Optionnel.Entity.Departement;
import org.sid.gestion_etudiant.Optionnel.Entity.Teacher;
import org.sid.gestion_etudiant.Optionnel.Exception.DepartementNotFoundException;
import org.sid.gestion_etudiant.Optionnel.Exception.TeacherNotFoundException;
import org.sid.gestion_etudiant.Optionnel.Mapper.TeacherMapper;
import org.sid.gestion_etudiant.Optionnel.Repository.DepartementRepo;
import org.sid.gestion_etudiant.Optionnel.Repository.TeacherRepo;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepo teacherRepo;
    private final DepartementRepo departementRepo;

    @Override
    public TeacherDTO addTeacher(TeacherDTO dto) {
        Teacher teacher = TeacherMapper.toEntity(dto);

        if (dto.getDepartementId() != null) {
            Departement departement = departementRepo.findById(dto.getDepartementId())
                    .orElseThrow(() -> new DepartementNotFoundException("Department not found with id " + dto.getDepartementId()));

            teacher.setDepartement(departement);
        }

        teacher.setArchived(false);
        teacher.setArchivedAt(null);

        return TeacherMapper.toDto(teacherRepo.save(teacher));
    }

    @Override
    public List<TeacherDTO> getAllTeachers() {
        return teacherRepo.findByArchivedFalse()
                .stream()
                .map(TeacherMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherDTO> getTeachersByNom(String nom) {
        List<Teacher> teachers = teacherRepo.findByNomContainingIgnoreCaseAndArchivedFalse(nom);

        if (teachers.isEmpty()) {
            throw new TeacherNotFoundException("No active teachers found with name: " + nom);
        }

        return teachers.stream()
                .map(TeacherMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TeacherDTO getTeacherById(Long id) {
        Teacher teacher = teacherRepo.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id " + id));

        return TeacherMapper.toDto(teacher);
    }

    @Override
    public TeacherDTO updateTeacher(Long id, TeacherDTO dto) {
        Teacher teacher = teacherRepo.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id " + id));

        teacher.setNom(dto.getNom());
        teacher.setPrenom(dto.getPrenom());
        teacher.setEmail(dto.getEmail());
        teacher.setSpecialite(dto.getSpecialite());

        if (dto.getDepartementId() != null) {
            Departement departement = departementRepo.findById(dto.getDepartementId())
                    .orElseThrow(() -> new DepartementNotFoundException("Department not found with id " + dto.getDepartementId()));

            teacher.setDepartement(departement);
        }

        return TeacherMapper.toDto(teacherRepo.save(teacher));
    }

    @Override
    public String deleteTeacher(Long id) {
        Teacher teacher = teacherRepo.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id " + id));

        if (teacher.isArchived()) {
            return "Teacher already archived with id " + id;
        }

        teacher.setArchived(true);
        teacher.setArchivedAt(LocalDateTime.now());

        teacherRepo.save(teacher);

        return "Teacher archived successfully with id " + id
                + ". It will be permanently deleted after 7 days.";
    }

    @Override
    public List<TeacherDTO> getArchivedTeachers() {
        return teacherRepo.findByArchivedTrue()
                .stream()
                .map(TeacherMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TeacherDTO restoreTeacher(Long id) {
        Teacher teacher = teacherRepo.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with id " + id));

        if (!teacher.isArchived()) {
            throw new RuntimeException("Teacher is not archived");
        }

        teacher.setArchived(false);
        teacher.setArchivedAt(null);

        return TeacherMapper.toDto(teacherRepo.save(teacher));
    }

    @Transactional
    @Override
    public void deleteOldArchivedTeachers() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<Teacher> oldArchivedTeachers =
                teacherRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        teacherRepo.deleteAll(oldArchivedTeachers);
    }

    @Override
    public byte[] generateTeachersPdf() {
        try {
            List<Teacher> teachers = teacherRepo.findByArchivedFalse();

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

            Paragraph title = new Paragraph("Teachers List", titleFont);
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

            addInfoCell(infoTable, "Active teachers: " + teachers.size(), infoFont);
            addInfoCell(infoTable, "Format: Printable PDF", infoFont);

            document.add(infoTable);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.8f, 1.8f, 3f, 2.2f, 2.5f});

            addTableHeader(table, headerFont, "Last Name");
            addTableHeader(table, headerFont, "First Name");
            addTableHeader(table, headerFont, "Email");
            addTableHeader(table, headerFont, "Speciality");
            addTableHeader(table, headerFont, "Department");

            int index = 0;

            for (Teacher teacher : teachers) {
                Color rowColor = index % 2 == 0
                        ? new Color(248, 250, 252)
                        : Color.WHITE;

                addTableCell(table, bodyFont, teacher.getNom(), rowColor);
                addTableCell(table, bodyFont, teacher.getPrenom(), rowColor);
                addTableCell(table, bodyFont, teacher.getEmail(), rowColor);
                addTableCell(table, bodyFont, teacher.getSpecialite(), rowColor);
                addTableCell(
                        table,
                        bodyFont,
                        teacher.getDepartement() != null ? teacher.getDepartement().getNom() : "",
                        rowColor
                );

                index++;
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Teacher Management System - Official Teachers List",
                    footerFont
            );
            footer.setAlignment(Paragraph.ALIGN_CENTER);
            footer.setSpacingBefore(18);
            document.add(footer);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error while generating teachers PDF", e);
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