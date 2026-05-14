package org.sid.gestion_etudiant.Metier.ServiceImpl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.sid.gestion_etudiant.Metier.Service.StudentService;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.dto.StudentDTO;
import org.sid.gestion_etudiant.Metier.exception.StudentNotFoundException;
import org.sid.gestion_etudiant.Metier.mapper.StudentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepo studentRepo;

    @Override
    public StudentDTO addStudent(StudentDTO dto) {
        Student student = StudentMapper.toEntity(dto);
        return StudentMapper.toDTO(studentRepo.save(student));
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        return studentRepo.findByArchivedFalse()
                .stream()
                .map(StudentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getStudentsByNom(String nom) {

        List<Student> students =
                studentRepo.findByNomContainsIgnoreCaseAndArchivedFalse(nom);

        if (students.isEmpty()) {
            throw new StudentNotFoundException("No active students found with name: " + nom);
        }

        return students.stream()
                .map(StudentMapper::toDTO)
                .toList();
    }

    @Override
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        student.setNom(dto.getNom());
        student.setPrenom(dto.getPrenom());
        student.setEmail(dto.getEmail());
        student.setAdresse(dto.getAdresse());
        student.setTelephone(dto.getTelephone());
        student.setGenre(dto.getGenre());
        student.setDate_Naissance(dto.getDate_Naissance());

        return StudentMapper.toDTO(studentRepo.save(student));
    }

    @Override
    public String deleteStudent(Long id) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + id));

        if (student.isArchived()) {
            return "Student already archived with id " + id;
        }

        student.setArchived(true);
        student.setArchivedAt(LocalDateTime.now());

        studentRepo.save(student);

        return "Student archived successfully with id " + id + ". It will be permanently deleted after 7 days.";
    }

    @Override
    public List<StudentDTO> getArchivedStudents() {
        return studentRepo.findByArchivedTrue()
                .stream()
                .map(StudentMapper::toDTO)
                .toList();
    }

    @Override
    public StudentDTO restoreStudent(Long id) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + id));

        if (!student.isArchived()) {
            throw new RuntimeException("Student is not archived");
        }

        student.setArchived(false);
        student.setArchivedAt(null);

        return StudentMapper.toDTO(studentRepo.save(student));
    }

    @Transactional
    @Override
    public void deleteOldArchivedStudents() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<Student> oldArchivedStudents =
                studentRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        studentRepo.deleteAll(oldArchivedStudents);
    }
}