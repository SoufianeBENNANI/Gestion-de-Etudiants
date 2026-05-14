package org.sid.gestion_etudiant.Metier.mapper;

import org.sid.gestion_etudiant.Metier.Entity.Attendance;
import org.sid.gestion_etudiant.Metier.dto.AttendanceDTO;

public class AttendanceMapper {

    public static AttendanceDTO toDto(Attendance attendance) {
        AttendanceDTO attendanceDTO = new AttendanceDTO();

        attendanceDTO.setId(attendance.getId());
        attendanceDTO.setDate(attendance.getDate());
        attendanceDTO.setStatus(attendance.getStatus());
        attendanceDTO.setArchived(attendance.isArchived());
        attendanceDTO.setArchivedAt(attendance.getArchivedAt());

        if (attendance.getStudent() != null) {
            attendanceDTO.setStudentId(attendance.getStudent().getId());
        }

        return attendanceDTO;
    }

    public static Attendance toEntity(AttendanceDTO attendanceDTO) {
        Attendance attendance = new Attendance();

        attendance.setDate(attendanceDTO.getDate());
        attendance.setStatus(attendanceDTO.getStatus());
        attendance.setArchived(attendanceDTO.isArchived());
        attendance.setArchivedAt(attendanceDTO.getArchivedAt());

        return attendance;
    }
}