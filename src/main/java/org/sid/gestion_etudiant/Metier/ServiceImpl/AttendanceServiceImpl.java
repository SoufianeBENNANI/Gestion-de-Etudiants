package org.sid.gestion_etudiant.Metier.ServiceImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Entity.Attendance;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.Repository.AttendanceRepo;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.sid.gestion_etudiant.Metier.Service.AttendanceService;
import org.sid.gestion_etudiant.Metier.dto.AttendanceDTO;
import org.sid.gestion_etudiant.Metier.exception.AttendanceNotFoundException;
import org.sid.gestion_etudiant.Metier.exception.StudentNotFoundException;
import org.sid.gestion_etudiant.Metier.mapper.AttendanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepo attendanceRepo;
    private final StudentRepo studentRepo;

    @Override
    public AttendanceDTO addAttendance(AttendanceDTO attendanceDTO) {
        Student student = studentRepo.findById(attendanceDTO.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        Attendance attendance = AttendanceMapper.toEntity(attendanceDTO);

        attendance.setStudent(student);
        attendance.setArchived(false);
        attendance.setArchivedAt(null);

        return AttendanceMapper.toDto(attendanceRepo.save(attendance));
    }

    @Override
    public List<AttendanceDTO> getAllAttendances() {
        return attendanceRepo.findByArchivedFalse()
                .stream()
                .map(AttendanceMapper::toDto)
                .toList();
    }

    @Override
    public List<AttendanceDTO> getArchivedAttendances() {
        return attendanceRepo.findByArchivedTrue()
                .stream()
                .map(AttendanceMapper::toDto)
                .toList();
    }

    @Override
    public AttendanceDTO getAttendanceById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Le champ ID est obligatoire");
        }

        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance non trouvée avec id : " + id));

        return AttendanceMapper.toDto(attendance);
    }

    @Override
    public AttendanceDTO updateAttendance(Long id, AttendanceDTO attendanceDTO) {
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance non trouvée avec id : " + id));

        if (attendance.isArchived()) {
            throw new RuntimeException("Impossible de modifier une attendance archivée. Restaurer d'abord.");
        }

        attendance.setDate(attendanceDTO.getDate());
        attendance.setStatus(attendanceDTO.getStatus());

        if (attendanceDTO.getStudentId() != null) {
            Student student = studentRepo.findById(attendanceDTO.getStudentId())
                    .orElseThrow(() -> new StudentNotFoundException("Student not found"));
            attendance.setStudent(student);
        }

        return AttendanceMapper.toDto(attendanceRepo.save(attendance));
    }

    @Override
    public String deleteAttendance(Long id) {
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance non trouvée avec id : " + id));

        if (attendance.isArchived()) {
            return "Attendance déjà archivée avec id : " + id;
        }

        attendance.setArchived(true);
        attendance.setArchivedAt(LocalDateTime.now());

        attendanceRepo.save(attendance);

        return "Attendance archivée avec succès. Elle sera supprimée définitivement après 7 jours.";
    }

    @Override
    public AttendanceDTO restoreAttendance(Long id) {
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance non trouvée avec id : " + id));

        if (!attendance.isArchived()) {
            throw new RuntimeException("Cette attendance n'est pas archivée.");
        }

        attendance.setArchived(false);
        attendance.setArchivedAt(null);

        return AttendanceMapper.toDto(attendanceRepo.save(attendance));
    }

    @Override
    @Transactional
    public void deleteOldArchivedAttendances() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<Attendance> oldArchivedAttendances =
                attendanceRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        attendanceRepo.deleteAll(oldArchivedAttendances);
    }
}