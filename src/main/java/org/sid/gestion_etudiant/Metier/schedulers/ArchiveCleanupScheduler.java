package org.sid.gestion_etudiant.Metier.schedulers;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.IA.Service.IAModelService;
import org.sid.gestion_etudiant.Metier.Service.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ArchiveCleanupScheduler {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final ClassesService classesService;
    private final CoursesService coursesService;
    private final GradeService gradeService;
    private final PayementService payementService;
    private final IAModelService iaModelService;


    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldArchivedData() {
        studentService.deleteOldArchivedStudents();
        attendanceService.deleteOldArchivedAttendances();
        classesService.deleteOldArchivedClasses();
        coursesService.deleteOldArchivedCourses();
        gradeService.deleteOldArchivedGrades();
        payementService.deleteOldArchivedPayements();
        iaModelService.deleteOldArchivedModels();
    }
}