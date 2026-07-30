package org.sid.gestion_etudiant.Optionnel.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Metier.dto.CreateTeacherRequest;
import org.sid.gestion_etudiant.Metier.dto.CreatedAccountResponse;
import org.sid.gestion_etudiant.Optionnel.DTO.TeacherDTO;
import org.sid.gestion_etudiant.Optionnel.Entity.Departement;
import org.sid.gestion_etudiant.Optionnel.Entity.Teacher;
import org.sid.gestion_etudiant.Optionnel.Exception.DepartementNotFoundException;
import org.sid.gestion_etudiant.Optionnel.Exception.TeacherNotFoundException;
import org.sid.gestion_etudiant.Optionnel.Mapper.TeacherMapper;
import org.sid.gestion_etudiant.Optionnel.Repository.DepartementRepo;
import org.sid.gestion_etudiant.Optionnel.Repository.TeacherRepo;
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
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepo teacherRepo;
    private final DepartementRepo departementRepo;
    private final KeycloakUserService keycloakUserService;
    private final PasswordGenerator passwordGenerator;

    @Value("${keycloak.groups.teacher}")
    private String teacherGroup;


    @Override
    @Transactional
    public CreatedAccountResponse createTeacherAccount(
            CreateTeacherRequest request
    ) {

        validateCreateTeacherRequest(request);

        String normalizedEmail =
                normalizeEmail(request.email());

        if (teacherRepo.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Un enseignant existe déjà avec cet email"
            );
        }

        Departement departement =
                findDepartementByNom(request.departementNom());

        String temporaryPassword =
                passwordGenerator.generateTemporaryPassword();

        String keycloakId = null;

        try {

            keycloakId = keycloakUserService.createUser(
                    request.prenom().trim(),
                    request.nom().trim(),
                    normalizedEmail,
                    temporaryPassword,
                    teacherGroup
            );

            Teacher teacher = Teacher.builder()
                    .prenom(request.prenom().trim())
                    .nom(request.nom().trim())
                    .email(normalizedEmail)
                    .specialite(request.specialite().trim())
                    .departement(departement)
                    .keycloakId(keycloakId)
                    .archived(false)
                    .archivedAt(null)
                    .build();

            Teacher savedTeacher =
                    teacherRepo.saveAndFlush(teacher);

            return new CreatedAccountResponse(
                    savedTeacher.getId(),
                    savedTeacher.getKeycloakId(),
                    savedTeacher.getEmail(),
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
    public TeacherDTO addTeacher(TeacherDTO dto) {

        validateTeacherDto(dto);

        String normalizedEmail =
                normalizeEmail(dto.getEmail());

        if (teacherRepo.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Un enseignant existe déjà avec cet email"
            );
        }

        Departement departement =
                findDepartementByNom(dto.getDepartementNom());

        Teacher teacher =
                TeacherMapper.toEntity(dto);

        teacher.setEmail(normalizedEmail);
        teacher.setDepartement(departement);
        teacher.setArchived(false);
        teacher.setArchivedAt(null);

        Teacher savedTeacher =
                teacherRepo.save(teacher);

        return TeacherMapper.toDto(savedTeacher);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherDTO> getAllTeachers() {

        return teacherRepo.findByArchivedFalse()
                .stream()
                .map(TeacherMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherDTO> getTeachersByNom(String nom) {

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom de l'enseignant est obligatoire"
            );
        }

        List<Teacher> teachers =
                teacherRepo
                        .findByNomContainingIgnoreCaseAndArchivedFalse(
                                nom.trim()
                        );

        if (teachers.isEmpty()) {
            throw new TeacherNotFoundException(
                    "Aucun enseignant actif trouvé avec le nom : "
                            + nom
            );
        }

        return teachers.stream()
                .map(TeacherMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherDTO getTeacherById(Long id) {

        Teacher teacher = findTeacherById(id);

        return TeacherMapper.toDto(teacher);
    }

    @Transactional(readOnly = true)
    public TeacherDTO getTeacherByKeycloakId(
            String keycloakId
    ) {

        if (keycloakId == null || keycloakId.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant Keycloak est obligatoire"
            );
        }

        Teacher teacher = teacherRepo
                .findByKeycloakId(keycloakId)
                .orElseThrow(() ->
                        new TeacherNotFoundException(
                                "Enseignant introuvable avec l'identifiant Keycloak : "
                                        + keycloakId
                        )
                );

        return TeacherMapper.toDto(teacher);
    }

    @Override
    @Transactional
    public TeacherDTO updateTeacher(
            Long id,
            TeacherDTO dto
    ) {
        validateTeacherDto(dto);

        Teacher teacher = findTeacherById(id);

        String normalizedEmail =
                normalizeEmail(dto.getEmail());

        boolean emailChanged =
                teacher.getEmail() == null
                        || !teacher.getEmail()
                        .equalsIgnoreCase(normalizedEmail);

        if (emailChanged
                && teacherRepo.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Un autre enseignant existe déjà avec cet email"
            );
        }

        Departement departement =
                findDepartementByNom(dto.getDepartementNom());

        teacher.setNom(dto.getNom().trim());
        teacher.setPrenom(dto.getPrenom().trim());
        teacher.setEmail(normalizedEmail);
        teacher.setSpecialite(dto.getSpecialite().trim());
        teacher.setDepartement(departement);

        Teacher savedTeacher =
                teacherRepo.save(teacher);

        return TeacherMapper.toDto(savedTeacher);
    }

    @Override
    @Transactional
    public String deleteTeacher(Long id) {

        Teacher teacher = findTeacherById(id);

        if (teacher.isArchived()) {
            return "L'enseignant est déjà archivé avec l'identifiant "
                    + id;
        }

        teacher.setArchived(true);
        teacher.setArchivedAt(LocalDateTime.now());

        teacherRepo.save(teacher);

        return "Enseignant archivé avec succès avec l'identifiant "
                + id
                + ". Il sera supprimé définitivement après 7 jours.";
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherDTO> getArchivedTeachers() {

        return teacherRepo.findByArchivedTrue()
                .stream()
                .map(TeacherMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public TeacherDTO restoreTeacher(Long id) {

        Teacher teacher = findTeacherById(id);

        if (!teacher.isArchived()) {
            throw new IllegalStateException(
                    "Cet enseignant n'est pas archivé"
            );
        }

        teacher.setArchived(false);
        teacher.setArchivedAt(null);

        Teacher restoredTeacher =
                teacherRepo.save(teacher);

        return TeacherMapper.toDto(restoredTeacher);
    }

    /*
     * Suppression définitive après 7 jours.
     *
     * Le compte Keycloak est également supprimé.
     */
    @Override
    @Transactional
    public void deleteOldArchivedTeachers() {

        LocalDateTime limitDate =
                LocalDateTime.now().minusDays(7);

        List<Teacher> oldArchivedTeachers =
                teacherRepo
                        .findByArchivedTrueAndArchivedAtBefore(
                                limitDate
                        );

        for (Teacher teacher : oldArchivedTeachers) {

            String keycloakId =
                    teacher.getKeycloakId();

            if (keycloakId != null
                    && !keycloakId.isBlank()) {

                try {
                    keycloakUserService.deleteUser(keycloakId);

                } catch (RuntimeException exception) {

                    throw new IllegalStateException(
                            "Impossible de supprimer le compte Keycloak "
                                    + "de l'enseignant avec l'identifiant "
                                    + teacher.getId(),
                            exception
                    );
                }
            }
        }

        teacherRepo.deleteAll(oldArchivedTeachers);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateTeachersPdf() {

        try {

            List<Teacher> teachers =
                    teacherRepo.findByArchivedFalse();

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
                    "Liste des enseignants",
                    titleFont
            );

            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(6);

            document.add(title);

            String generationDate =
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern(
                                    "dd/MM/yyyy HH:mm"
                            )
                    );

            Paragraph subtitle = new Paragraph(
                    "Généré le : " + generationDate,
                    subtitleFont
            );

            subtitle.setAlignment(
                    Paragraph.ALIGN_CENTER
            );

            subtitle.setSpacingAfter(18);

            document.add(subtitle);

            PdfPTable infoTable =
                    new PdfPTable(2);

            infoTable.setWidthPercentage(100);
            infoTable.setWidths(
                    new float[]{1f, 1f}
            );

            infoTable.setSpacingAfter(18);

            addInfoCell(
                    infoTable,
                    "Enseignants actifs : " + teachers.size(),
                    infoFont
            );

            addInfoCell(
                    infoTable,
                    "Format : PDF imprimable",
                    infoFont
            );

            document.add(infoTable);

            PdfPTable table =
                    new PdfPTable(5);

            table.setWidthPercentage(100);

            table.setWidths(
                    new float[]{
                            1.8f,
                            1.8f,
                            3f,
                            2.2f,
                            2.5f
                    }
            );

            addTableHeader(
                    table,
                    headerFont,
                    "Nom"
            );

            addTableHeader(
                    table,
                    headerFont,
                    "Prénom"
            );

            addTableHeader(
                    table,
                    headerFont,
                    "Email"
            );

            addTableHeader(
                    table,
                    headerFont,
                    "Spécialité"
            );

            addTableHeader(
                    table,
                    headerFont,
                    "Département"
            );

            for (int index = 0;
                 index < teachers.size();
                 index++) {

                Teacher teacher =
                        teachers.get(index);

                Color rowColor =
                        index % 2 == 0
                                ? new Color(248, 250, 252)
                                : Color.WHITE;

                addTableCell(
                        table,
                        bodyFont,
                        teacher.getNom(),
                        rowColor
                );

                addTableCell(
                        table,
                        bodyFont,
                        teacher.getPrenom(),
                        rowColor
                );

                addTableCell(
                        table,
                        bodyFont,
                        teacher.getEmail(),
                        rowColor
                );

                addTableCell(
                        table,
                        bodyFont,
                        teacher.getSpecialite(),
                        rowColor
                );

                String departementName =
                        teacher.getDepartement() != null
                                ? teacher.getDepartement().getNom()
                                : "";

                addTableCell(
                        table,
                        bodyFont,
                        departementName,
                        rowColor
                );
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Système de gestion des enseignants",
                    footerFont
            );

            footer.setAlignment(
                    Paragraph.ALIGN_CENTER
            );

            footer.setSpacingBefore(18);

            document.add(footer);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Erreur pendant la génération du PDF des enseignants",
                    exception
            );
        }
    }

    private Teacher findTeacherById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "L'identifiant de l'enseignant est obligatoire"
            );
        }

        return teacherRepo.findById(id)
                .orElseThrow(() ->
                        new TeacherNotFoundException(
                                "Enseignant introuvable avec l'identifiant "
                                        + id
                        )
                );
    }

    private Departement findDepartementByNom(
            String departementNom
    ) {
        requireNotBlank(
                departementNom,
                "Le nom du département est obligatoire"
        );

        String normalizedNom = departementNom.trim();

        return departementRepo
                .findByNomIgnoreCaseAndArchivedFalse(normalizedNom)
                .orElseThrow(() ->
                        new DepartementNotFoundException(
                                "Département actif introuvable avec le nom : "
                                        + normalizedNom
                        )
                );
    }
    private void validateCreateTeacherRequest(
            CreateTeacherRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Les informations de l'enseignant sont obligatoires"
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

    private void validateTeacherDto(
            TeacherDTO dto
    ) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Les données de l'enseignant sont obligatoires"
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

        String normalizedEmail =
                email.trim();

        if (!normalizedEmail.contains("@")
                || normalizedEmail.startsWith("@")
                || normalizedEmail.endsWith("@")) {

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
                new PdfPCell(
                        new Phrase(text, font)
                );

        cell.setPadding(12);

        cell.setBorderColor(
                new Color(226, 232, 240)
        );

        cell.setBackgroundColor(
                new Color(241, 245, 249)
        );

        cell.setHorizontalAlignment(
                PdfPCell.ALIGN_CENTER
        );

        table.addCell(cell);
    }

    private void addTableHeader(
            PdfPTable table,
            Font font,
            String text
    ) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(text, font)
                );

        cell.setPadding(9);

        cell.setBackgroundColor(
                new Color(15, 23, 42)
        );

        cell.setBorderColor(
                new Color(15, 23, 42)
        );

        cell.setHorizontalAlignment(
                PdfPCell.ALIGN_CENTER
        );

        cell.setVerticalAlignment(
                PdfPCell.ALIGN_MIDDLE
        );

        table.addCell(cell);
    }

    private void addTableCell(
            PdfPTable table,
            Font font,
            String text,
            Color backgroundColor
    ) {

        String safeText =
                text != null ? text : "";

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(safeText, font)
                );

        cell.setPadding(8);
        cell.setBackgroundColor(backgroundColor);

        cell.setBorderColor(
                new Color(226, 232, 240)
        );

        cell.setVerticalAlignment(
                PdfPCell.ALIGN_MIDDLE
        );

        table.addCell(cell);
    }
}