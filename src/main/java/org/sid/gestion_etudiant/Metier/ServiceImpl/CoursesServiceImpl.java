package org.sid.gestion_etudiant.Metier.ServiceImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Entity.Courses;
import org.sid.gestion_etudiant.Metier.Repository.CoursesRepo;
import org.sid.gestion_etudiant.Metier.Service.CoursesService;
import org.sid.gestion_etudiant.Metier.dto.CoursesDTO;
import org.sid.gestion_etudiant.Metier.exception.CoursesNotFoundException;
import org.sid.gestion_etudiant.Metier.mapper.CoursesMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CoursesServiceImpl implements CoursesService {

    private final CoursesRepo coursesRepo;

    @Override
    public CoursesDTO addCourses(CoursesDTO coursesDTO) {
        Courses courses = CoursesMapper.toEntity(coursesDTO);

        courses.setArchived(false);
        courses.setArchivedAt(null);

        return CoursesMapper.toDto(coursesRepo.save(courses));
    }

    @Override
    public List<CoursesDTO> getAllCourses() {
        return coursesRepo.findByArchivedFalse()
                .stream()
                .map(CoursesMapper::toDto)
                .toList();
    }

    @Override
    public List<CoursesDTO> getArchivedCourses() {
        return coursesRepo.findByArchivedTrue()
                .stream()
                .map(CoursesMapper::toDto)
                .toList();
    }

    @Override
    public CoursesDTO getCoursesById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Le champ ID est obligatoire");
        }

        Courses courses = coursesRepo.findById(id)
                .orElseThrow(() -> new CoursesNotFoundException("Courses non trouvée avec id : " + id));

        return CoursesMapper.toDto(courses);
    }

    @Override
    public CoursesDTO updateCourses(Long id, CoursesDTO coursesDTO) {
        if (id == null) {
            throw new IllegalArgumentException("Le champ ID est obligatoire");
        }

        Courses courses = coursesRepo.findById(id)
                .orElseThrow(() -> new CoursesNotFoundException("Courses non trouvée avec id : " + id));

        if (courses.isArchived()) {
            throw new RuntimeException("Impossible de modifier un course archivé. Restaurer d'abord.");
        }

        courses.setNom(coursesDTO.getNom());
        courses.setDescription(coursesDTO.getDescription());
        courses.setCredits(coursesDTO.getCredits());

        return CoursesMapper.toDto(coursesRepo.save(courses));
    }

    @Override
    public String deleteCourses(Long id) {
        Courses courses = coursesRepo.findById(id)
                .orElseThrow(() -> new CoursesNotFoundException("Course non trouvée avec l'id : " + id));

        if (courses.isArchived()) {
            return "Course déjà archivé avec id : " + id;
        }

        courses.setArchived(true);
        courses.setArchivedAt(LocalDateTime.now());

        coursesRepo.save(courses);

        return "Course archivé avec succès. Il sera supprimé définitivement après 7 jours.";
    }

    @Override
    public CoursesDTO restoreCourses(Long id) {
        Courses courses = coursesRepo.findById(id)
                .orElseThrow(() -> new CoursesNotFoundException("Course non trouvée avec l'id : " + id));

        if (!courses.isArchived()) {
            throw new RuntimeException("Ce course n'est pas archivé.");
        }

        courses.setArchived(false);
        courses.setArchivedAt(null);

        return CoursesMapper.toDto(coursesRepo.save(courses));
    }

    @Override
    @Transactional
    public void deleteOldArchivedCourses() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<Courses> oldArchivedCourses =
                coursesRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        coursesRepo.deleteAll(oldArchivedCourses);
    }
}