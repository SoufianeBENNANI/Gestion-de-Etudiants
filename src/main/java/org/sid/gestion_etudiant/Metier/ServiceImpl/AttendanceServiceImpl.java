package org.sid.gestion_etudiant.Metier.ServiceImpl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Entity.Attendance;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.Repository.AttendanceRepo;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.sid.gestion_etudiant.Metier.Service.AttendanceService;
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

        boolean activeAttendanceExists =
                attendanceRepo.existsByStudentIdAndDateAndArchivedFalse(
                        attendanceDTO.getStudentId(),
                        attendanceDTO.getDate()
                );

        if (activeAttendanceExists) {
            throw new RuntimeException(
                    "Attendance already exists for this student on this date"
            );
        }

        Attendance attendance = AttendanceMapper.toEntity(attendanceDTO);
        attendance.setStudent(student);
        attendance.setArchived(false);
        attendance.setArchivedAt(null);

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
                .orElseThrow(() -> new RuntimeException("Attendance not found with id " + id));

        return AttendanceMapper.toDto(attendance);
    }

    @Override
    public AttendanceDTO updateAttendance(Long id, AttendanceDTO attendanceDTO) {
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found with id " + id));

        if (attendance.isArchived()) {
            throw new RuntimeException("Cannot update archived attendance with id " + id);
        }

        Long studentId = attendanceDTO.getStudentId() != null
                ? attendanceDTO.getStudentId()
                : attendance.getStudent().getId();

        List<Attendance> sameDateAttendances =
                attendanceRepo.findByStudentIdAndDate(studentId, attendanceDTO.getDate());

        boolean duplicateExists = sameDateAttendances.stream()
                .anyMatch(item ->
                        !item.getId().equals(id) &&
                                !item.isArchived()
                );

        if (duplicateExists) {
            throw new RuntimeException(
                    "Another attendance already exists for this student on this date"
            );
        }

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
                .orElseThrow(() -> new RuntimeException("Attendance not found with id " + id));

        if (attendance.isArchived()) {
            return "Attendance already archived with id " + id;
        }

        attendance.setArchived(true);
        attendance.setArchivedAt(LocalDateTime.now());

        attendanceRepo.save(attendance);

        return "Attendance archived successfully with id " + id
                + ". It will be permanently deleted after 7 days.";
    }

    @Override
    public AttendanceDTO restoreAttendance(Long id) {
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found with id " + id));

        if (!attendance.isArchived()) {
            throw new RuntimeException("Attendance is not archived");
        }

        boolean activeAttendanceExists =
                attendanceRepo.existsByStudentIdAndDateAndArchivedFalse(
                        attendance.getStudent().getId(),
                        attendance.getDate()
                );

        if (activeAttendanceExists) {
            throw new RuntimeException(
                    "Cannot restore this attendance because an active attendance already exists for this student on this date"
            );
        }

        attendance.setArchived(false);
        attendance.setArchivedAt(null);

        Attendance restoredAttendance = attendanceRepo.save(attendance);

        return AttendanceMapper.toDto(restoredAttendance);
    }

    @Transactional
    @Override
    public void deleteOldArchivedAttendances() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<Attendance> oldArchivedAttendances =
                attendanceRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        attendanceRepo.deleteAll(oldArchivedAttendances);
    }
}