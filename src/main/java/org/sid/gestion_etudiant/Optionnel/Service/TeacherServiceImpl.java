package org.sid.gestion_etudiant.Optionnel.Service;

import org.sid.gestion_etudiant.Optionnel.DTO.TeacherDTO;
import org.sid.gestion_etudiant.Optionnel.Entity.Departement;
import org.sid.gestion_etudiant.Optionnel.Entity.Teacher;
import org.sid.gestion_etudiant.Optionnel.Exception.DepartementNotFoundException;
import org.sid.gestion_etudiant.Optionnel.Mapper.TeacherMapper;
import org.sid.gestion_etudiant.Optionnel.Repository.DepartementRepo;
import org.sid.gestion_etudiant.Optionnel.Repository.TeacherRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherRepo teacherRepo;

    @Autowired
    private DepartementRepo departementRepo;

    @Override
    public TeacherDTO save(TeacherDTO teacherDTO) {
        Teacher teacher = TeacherMapper.toEntity(teacherDTO);
        Departement departement = departementRepo.findById(teacherDTO.getDepartementId())
                .orElseThrow(() -> new DepartementNotFoundException("Department not found"));
        teacher.setDepartement(departement);

        return TeacherMapper.toDto(teacherRepo.save(teacher));
    }
}
