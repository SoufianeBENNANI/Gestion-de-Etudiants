package org.sid.gestion_etudiant.Metier.ServiceImpl;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.sid.gestion_etudiant.Metier.Service.StudentService;
import org.sid.gestion_etudiant.Metier.dto.CreateStudentRequest;
import org.sid.gestion_etudiant.Metier.dto.CreatedAccountResponse;
import org.sid.gestion_etudiant.Metier.dto.StudentDTO;
import org.sid.gestion_etudiant.Metier.exception.StudentNotFoundException;
import org.sid.gestion_etudiant.Metier.mapper.StudentMapper;
import org.sid.gestion_etudiant.Security.KeycloakUserService;
import org.sid.gestion_etudiant.Security.PasswordGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepo studentRepo;
    private final KeycloakUserService keycloakUserService;
    private final PasswordGenerator passwordGenerator;

    @Value("${keycloak.groups.student}")
    private String studentGroup;

    @Override
    @Transactional
    public CreatedAccountResponse createStudentAccount(
            CreateStudentRequest request
    ) {

        validateCreateStudentRequest(request);

        String normalizedEmail =
                normalizeEmail(request.email());

        if (studentRepo.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Un étudiant existe déjà avec cet email"
            );
        }

        String temporaryPassword =
                passwordGenerator.generateTemporaryPassword();

        String keycloakId = null;

        try {

            keycloakId = keycloakUserService.createUser(
                    request.prenom().trim(),
                    request.nom().trim(),
                    normalizedEmail,
                    temporaryPassword,
                    studentGroup
            );

            Student student = Student.builder()
                    .prenom(request.prenom().trim())
                    .nom(request.nom().trim())
                    .email(normalizedEmail)
                    .date_Naissance(request.dateNaissance())
                    .genre(request.genre())
                    .adresse(
                            request.adresse() != null
                                    ? request.adresse().trim()
                                    : null
                    )
                    .telephone(
                            request.telephone() != null
                                    ? request.telephone().trim()
                                    : null
                    )
                    .keycloakId(keycloakId)
                    .archived(false)
                    .archivedAt(null)
                    .build();

            Student savedStudent =
                    studentRepo.saveAndFlush(student);

            return new CreatedAccountResponse(
                    savedStudent.getId(),
                    savedStudent.getKeycloakId(),
                    savedStudent.getEmail(),
                    temporaryPassword
            );

        } catch (RuntimeException exception) {

            if (keycloakId != null && !keycloakId.isBlank()) {

                try {
                    keycloakUserService.deleteUser(keycloakId);

                } catch (RuntimeException deleteException) {
                    exception.addSuppressed(deleteException);
                }
            }

            throw exception;
        }
    }
    @Override
    @Transactional
    public StudentDTO addStudent(StudentDTO dto) {

        validateStudentDto(dto);

        String normalizedEmail =
                normalizeEmail(dto.getEmail());

        if (studentRepo.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Un étudiant existe déjà avec cet email"
            );
        }

        Student student =
                StudentMapper.toEntity(dto);

        student.setEmail(normalizedEmail);
        student.setArchived(false);
        student.setArchivedAt(null);

        return StudentMapper.toDTO(
                studentRepo.save(student)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {

        return studentRepo.findByArchivedFalse()
                .stream()
                .map(StudentMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> getStudentsByNom(String nom) {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                    "Le champ 'nom' est obligatoire"
            );
        }

        List<Student> students =
                studentRepo
                        .findByNomContainsIgnoreCaseAndArchivedFalse(
                                nom.trim()
                        );

        if (students.isEmpty()) {
            throw new StudentNotFoundException(
                    "Aucun étudiant actif trouvé avec le nom : "
                            + nom
            );
        }

        return students.stream()
                .map(StudentMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public StudentDTO updateStudent(
            Long id,
            StudentDTO dto
    ) {

        validateStudentDto(dto);

        Student student =
                findStudentById(id);

        String normalizedEmail =
                normalizeEmail(dto.getEmail());

        boolean emailChanged =
                student.getEmail() == null
                        || !student.getEmail()
                        .equalsIgnoreCase(normalizedEmail);

        if (emailChanged
                && studentRepo.existsByEmailIgnoreCase(
                normalizedEmail
        )) {

            throw new IllegalArgumentException(
                    "Un autre étudiant existe déjà avec cet email"
            );
        }

        student.setNom(dto.getNom().trim());
        student.setPrenom(dto.getPrenom().trim());
        student.setEmail(normalizedEmail);
        student.setAdresse(dto.getAdresse());
        student.setTelephone(dto.getTelephone());
        student.setGenre(dto.getGenre());
        student.setDate_Naissance(dto.getDate_Naissance());

        return StudentMapper.toDTO(
                studentRepo.save(student)
        );
    }

    @Override
    @Transactional
    public String deleteStudent(Long id) {

        Student student =
                findStudentById(id);

        if (student.isArchived()) {
            return "L'étudiant est déjà archivé avec l'identifiant "
                    + id;
        }

        student.setArchived(true);
        student.setArchivedAt(LocalDateTime.now());

        studentRepo.save(student);

        return "Étudiant archivé avec succès. "
                + "Il sera définitivement supprimé après 7 jours.";
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> getArchivedStudents() {

        return studentRepo.findByArchivedTrue()
                .stream()
                .map(StudentMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public StudentDTO restoreStudent(Long id) {

        Student student =
                findStudentById(id);

        if (!student.isArchived()) {
            throw new IllegalStateException(
                    "Cet étudiant n'est pas archivé"
            );
        }

        student.setArchived(false);
        student.setArchivedAt(null);

        return StudentMapper.toDTO(
                studentRepo.save(student)
        );
    }

    @Override
    @Transactional
    public void deleteOldArchivedStudents() {

        LocalDateTime limitDate =
                LocalDateTime.now().minusDays(7);

        List<Student> oldStudents =
                studentRepo
                        .findByArchivedTrueAndArchivedAtBefore(
                                limitDate
                        );

        for (Student student : oldStudents) {

            String keycloakId =
                    student.getKeycloakId();

            if (keycloakId != null
                    && !keycloakId.isBlank()) {

                keycloakUserService.deleteUser(keycloakId);
            }
        }

        studentRepo.deleteAll(oldStudents);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateStudentsPdf() {

        try {
            List<Student> students =
                    studentRepo.findByArchivedFalse();

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document = new Document(
                    PageSize.A4.rotate(),
                    30,
                    30,
                    30,
                    30
            );

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            Font titleFont = new Font(
                    Font.HELVETICA,
                    22,
                    Font.BOLD,
                    new Color(15, 23, 42)
            );

            Font subtitleFont = new Font(
                    Font.HELVETICA,
                    11,
                    Font.NORMAL,
                    new Color(100, 116, 139)
            );

            Font infoFont = new Font(
                    Font.HELVETICA,
                    11,
                    Font.BOLD,
                    new Color(30, 41, 59)
            );

            Font headerFont = new Font(
                    Font.HELVETICA,
                    10,
                    Font.BOLD,
                    Color.WHITE
            );

            Font bodyFont = new Font(
                    Font.HELVETICA,
                    9,
                    Font.NORMAL,
                    new Color(30, 41, 59)
            );

            Font footerFont = new Font(
                    Font.HELVETICA,
                    9,
                    Font.NORMAL,
                    new Color(100, 116, 139)
            );

            Paragraph title = new Paragraph(
                    "Liste des étudiants",
                    titleFont
            );

            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(6);

            document.add(title);

            String date = LocalDateTime.now()
                    .format(
                            DateTimeFormatter.ofPattern(
                                    "dd/MM/yyyy HH:mm"
                            )
                    );

            Paragraph subtitle = new Paragraph(
                    "Généré le : " + date,
                    subtitleFont
            );

            subtitle.setAlignment(Paragraph.ALIGN_CENTER);
            subtitle.setSpacingAfter(18);

            document.add(subtitle);

            PdfPTable infoTable =
                    new PdfPTable(2);

            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1f, 1f});
            infoTable.setSpacingAfter(18);

            addInfoCell(
                    infoTable,
                    "Étudiants actifs : " + students.size(),
                    infoFont
            );

            addInfoCell(
                    infoTable,
                    "Format : PDF imprimable",
                    infoFont
            );

            document.add(infoTable);

            PdfPTable table =
                    new PdfPTable(6);

            table.setWidthPercentage(100);

            table.setWidths(
                    new float[]{
                            1.7f,
                            1.7f,
                            3f,
                            1.8f,
                            1.4f,
                            2.6f
                    }
            );

            addTableHeader(table, headerFont, "Nom");
            addTableHeader(table, headerFont, "Prénom");
            addTableHeader(table, headerFont, "Email");
            addTableHeader(table, headerFont, "Téléphone");
            addTableHeader(table, headerFont, "Genre");
            addTableHeader(table, headerFont, "Adresse");

            for (int index = 0;
                 index < students.size();
                 index++) {

                Student student =
                        students.get(index);

                Color rowColor =
                        index % 2 == 0
                                ? new Color(248, 250, 252)
                                : Color.WHITE;

                addTableCell(
                        table,
                        bodyFont,
                        student.getNom(),
                        rowColor
                );

                addTableCell(
                        table,
                        bodyFont,
                        student.getPrenom(),
                        rowColor
                );

                addTableCell(
                        table,
                        bodyFont,
                        student.getEmail(),
                        rowColor
                );

                addTableCell(
                        table,
                        bodyFont,
                        student.getTelephone(),
                        rowColor
                );

                addTableCell(
                        table,
                        bodyFont,
                        student.getGenre() != null
                                ? student.getGenre().name()
                                : "",
                        rowColor
                );

                addTableCell(
                        table,
                        bodyFont,
                        student.getAdresse(),
                        rowColor
                );
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Système de gestion des étudiants",
                    footerFont
            );

            footer.setAlignment(Paragraph.ALIGN_CENTER);
            footer.setSpacingBefore(18);

            document.add(footer);
            document.close();

            return outputStream.toByteArray();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Erreur pendant la génération du PDF des étudiants",
                    exception
            );
        }
    }

    private Student findStudentById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "L'identifiant de l'étudiant est obligatoire"
            );
        }

        return studentRepo.findById(id)
                .orElseThrow(() ->
                        new StudentNotFoundException(
                                "Étudiant introuvable avec l'identifiant "
                                        + id
                        )
                );
    }

    private void validateCreateStudentRequest(
            CreateStudentRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Les informations de l'étudiant sont obligatoires"
            );
        }

        requireNotBlank(
                request.prenom(),
                "Le prénom est obligatoire"
        );

        requireNotBlank(
                request.nom(),
                "Le nom est obligatoire"
        );

        requireNotBlank(
                request.email(),
                "L'email est obligatoire"
        );

        validateEmail(request.email());
    }

    private void validateStudentDto(StudentDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Les informations de l'étudiant sont obligatoires"
            );
        }

        requireNotBlank(
                dto.getNom(),
                "Le nom est obligatoire"
        );

        requireNotBlank(
                dto.getPrenom(),
                "Le prénom est obligatoire"
        );

        requireNotBlank(
                dto.getEmail(),
                "L'email est obligatoire"
        );

        validateEmail(dto.getEmail());
    }

    private void validateEmail(String email) {

        String value = email.trim();

        if (!value.contains("@")
                || value.startsWith("@")
                || value.endsWith("@")) {

            throw new IllegalArgumentException(
                    "L'adresse email est invalide"
            );
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void requireNotBlank(
            String value,
            String message
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void addInfoCell(
            PdfPTable table,
            String text,
            Font font
    ) {

        PdfPCell cell =
                new PdfPCell(new Phrase(text, font));

        cell.setPadding(12);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setBackgroundColor(new Color(241, 245, 249));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);

        table.addCell(cell);
    }

    private void addTableHeader(
            PdfPTable table,
            Font font,
            String text
    ) {

        PdfPCell cell =
                new PdfPCell(new Phrase(text, font));

        cell.setPadding(9);
        cell.setBackgroundColor(new Color(15, 23, 42));
        cell.setBorderColor(new Color(15, 23, 42));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);

        table.addCell(cell);
    }

    private void addTableCell(
            PdfPTable table,
            Font font,
            String text,
            Color backgroundColor
    ) {

        PdfPCell cell = new PdfPCell(
                new Phrase(text != null ? text : "", font)
        );

        cell.setPadding(8);
        cell.setBackgroundColor(backgroundColor);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);

        table.addCell(cell);
    }
}