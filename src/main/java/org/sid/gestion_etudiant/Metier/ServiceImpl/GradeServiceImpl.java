package org.sid.gestion_etudiant.Metier.ServiceImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Entity.Courses;
import org.sid.gestion_etudiant.Metier.Entity.Grade;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.Repository.CoursesRepo;
import org.sid.gestion_etudiant.Metier.Repository.GradeRepo;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.sid.gestion_etudiant.Metier.Service.GradeService;
import org.sid.gestion_etudiant.Metier.dto.GradeDTO;
import org.sid.gestion_etudiant.Metier.exception.CoursesNotFoundException;
import org.sid.gestion_etudiant.Metier.exception.GradeNotFoundException;
import org.sid.gestion_etudiant.Metier.exception.StudentNotFoundException;
import org.sid.gestion_etudiant.Metier.mapper.GradeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepo gradeRepo;
    private final StudentRepo studentRepo;
    private final CoursesRepo coursesRepo;

    @Override
    public GradeDTO addGrade(GradeDTO gradeDTO) {
        if (gradeDTO.getStudentId() == null) {
            throw new IllegalArgumentException("Le champ studentId est obligatoire");
        }

        if (gradeDTO.getCourseId() == null) {
            throw new IllegalArgumentException("Le champ courseId est obligatoire");
        }

        Student student = studentRepo.findById(gradeDTO.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        Courses course = coursesRepo.findById(gradeDTO.getCourseId())
                .orElseThrow(() -> new CoursesNotFoundException("Course not found"));

        Grade grade = GradeMapper.toEntity(gradeDTO);

        grade.setStudent(student);
        grade.setCourses(course);
        grade.setArchived(false);
        grade.setArchivedAt(null);

        return GradeMapper.toDto(gradeRepo.save(grade));
    }

    @Override
    public List<GradeDTO> getAllGrade() {
        return gradeRepo.findByArchivedFalse()
                .stream()
                .map(GradeMapper::toDto)
                .toList();
    }

    @Override
    public List<GradeDTO> getArchivedGrades() {
        return gradeRepo.findByArchivedTrue()
                .stream()
                .map(GradeMapper::toDto)
                .toList();
    }

    @Override
    public GradeDTO getGradeById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Le champ ID est obligatoire");
        }

        Grade grade = gradeRepo.findById(id)
                .orElseThrow(() -> new GradeNotFoundException("Grade non trouvée avec id : " + id));

        return GradeMapper.toDto(grade);
    }

    @Override
    public GradeDTO updateGrade(Long id, GradeDTO gradeDTO) {
        Grade grade = gradeRepo.findById(id)
                .orElseThrow(() -> new GradeNotFoundException("Grade non trouvée avec id : " + id));

        if (grade.isArchived()) {
            throw new RuntimeException("Impossible de modifier une note archivée. Restaurer d'abord.");
        }

        grade.setNote(gradeDTO.getNote());
        grade.setSemestre(gradeDTO.getSemestre());

        if (gradeDTO.getStudentId() != null) {
            Student student = studentRepo.findById(gradeDTO.getStudentId())
                    .orElseThrow(() -> new StudentNotFoundException("Student not found"));
            grade.setStudent(student);
        }

        if (gradeDTO.getCourseId() != null) {
            Courses course = coursesRepo.findById(gradeDTO.getCourseId())
                    .orElseThrow(() -> new CoursesNotFoundException("Course not found"));
            grade.setCourses(course);
        }

        return GradeMapper.toDto(gradeRepo.save(grade));
    }

    @Override
    public String deleteGrade(Long id) {
        Grade grade = gradeRepo.findById(id)
                .orElseThrow(() -> new GradeNotFoundException("Grade non trouvée avec l'id : " + id));

        if (grade.isArchived()) {
            return "Grade déjà archivée avec id : " + id;
        }

        grade.setArchived(true);
        grade.setArchivedAt(LocalDateTime.now());

        gradeRepo.save(grade);

        return "Grade archivée avec succès. Elle sera supprimée définitivement après 7 jours.";
    }

    @Override
    public GradeDTO restoreGrade(Long id) {
        Grade grade = gradeRepo.findById(id)
                .orElseThrow(() -> new GradeNotFoundException("Grade non trouvée avec l'id : " + id));

        if (!grade.isArchived()) {
            throw new RuntimeException("Cette grade n'est pas archivée.");
        }

        grade.setArchived(false);
        grade.setArchivedAt(null);

        return GradeMapper.toDto(gradeRepo.save(grade));
    }

    @Override
    @Transactional
    public void deleteOldArchivedGrades() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<Grade> oldArchivedGrades =
                gradeRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        gradeRepo.deleteAll(oldArchivedGrades);
    }
}