package org.sid.gestion_etudiant.Optionnel.Service;

import org.sid.gestion_etudiant.Optionnel.DTO.DepartementDTO;

import java.util.List;

/**
 * @author Soufiane
 **/
public interface DepartementService {

    DepartementDTO addDepartment(DepartementDTO dto);

    List<DepartementDTO> getAllDepartments();

    List<DepartementDTO> getDepartmentsByNom(String nom);

    DepartementDTO updateDepartment(Long id, DepartementDTO dto);

    String deleteDepartment(Long id);

    List<DepartementDTO> getArchivedDepartments();

    DepartementDTO restoreDepartment(Long id);

    void deleteOldArchivedDepartments();
}