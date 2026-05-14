package org.sid.gestion_etudiant.Metier.Service;

import org.sid.gestion_etudiant.Metier.dto.CoursesDTO;

import java.util.List;

public interface CoursesService {

    CoursesDTO addCourses(CoursesDTO coursesDTO);

    List<CoursesDTO> getAllCourses();

    List<CoursesDTO> getArchivedCourses();

    CoursesDTO getCoursesById(Long id);

    CoursesDTO updateCourses(Long id, CoursesDTO coursesDTO);

    String deleteCourses(Long id);

    CoursesDTO restoreCourses(Long id);

    void deleteOldArchivedCourses();
}