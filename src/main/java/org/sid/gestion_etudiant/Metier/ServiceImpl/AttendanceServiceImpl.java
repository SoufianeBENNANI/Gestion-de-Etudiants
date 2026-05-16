package org.sid.gestion_etudiant.Metier.Service;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Entity.Attendance;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.Repository.AttendanceRepo;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.sid.gestion_etudiant.Metier.dto.AttendanceDTO;
import org.sid.gestion_etudiant.Metier.mapper.AttendanceMapper;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Attendance attendance = AttendanceMapper.toEntity(attendanceDTO);
        attendance.setStudent(student);
        attendance.setArchived(false);

        Attendance savedAttendance = attendanceRepo.save(attendance);

        return AttendanceMapper.toDto(savedAttendance);
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
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        return AttendanceMapper.toDto(attendance);
    }

    @Override
    public AttendanceDTO updateAttendance(Long id, AttendanceDTO attendanceDTO) {
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        attendance.setDate(attendanceDTO.getDate());
        attendance.setStatus(attendanceDTO.getStatus());

        if (attendanceDTO.getStudentId() != null) {
            Student student = studentRepo.findById(attendanceDTO.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            attendance.setStudent(student);
        }

        Attendance updatedAttendance = attendanceRepo.save(attendance);

        return AttendanceMapper.toDto(updatedAttendance);
    }

    @Override
    public String deleteAttendance(Long id) {
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        attendance.setArchived(true);
        attendance.setArchivedAt(LocalDateTime.now());

        attendanceRepo.save(attendance);

        return "Attendance archived successfully";
    }

    @Override
    public AttendanceDTO restoreAttendance(Long id) {
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        attendance.setArchived(false);
        attendance.setArchivedAt(null);

        Attendance restoredAttendance = attendanceRepo.save(attendance);

        return AttendanceMapper.toDto(restoredAttendance);
    }

    @Override
    public void deleteOldArchivedAttendances() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(30);

        List<Attendance> oldAttendances =
                attendanceRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        attendanceRepo.deleteAll(oldAttendances);
    }
}