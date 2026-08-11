package org.sid.gestion_etudiant.Metier.Service;

import org.sid.gestion_etudiant.Metier.dto.AttendanceDTO;

import java.util.List;

public interface AttendanceService {

    AttendanceDTO addAttendance(AttendanceDTO attendanceDTO);

    List<AttendanceDTO> getAllAttendances();

    List<AttendanceDTO> getArchivedAttendances();

    AttendanceDTO getAttendanceById(Long id);

    AttendanceDTO updateAttendance(Long id, AttendanceDTO attendanceDTO);

    String deleteAttendance(Long id);

    AttendanceDTO restoreAttendance(Long id);

    void deleteOldArchivedAttendances();

    List<AttendanceDTO> getMyAttendances(String keycloakId, String email);
}