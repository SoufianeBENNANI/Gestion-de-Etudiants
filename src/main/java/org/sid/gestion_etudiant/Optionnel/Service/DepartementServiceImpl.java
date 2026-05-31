package org.sid.gestion_etudiant.Optionnel.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Optionnel.DTO.DepartementDTO;
import org.sid.gestion_etudiant.Optionnel.Entity.Departement;
import org.sid.gestion_etudiant.Optionnel.Exception.DepartementNotFoundException;
import org.sid.gestion_etudiant.Optionnel.Mapper.DepartementMapper;
import org.sid.gestion_etudiant.Optionnel.Repository.DepartementRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Soufiane
 **/
@Service
@AllArgsConstructor
public class DepartementServiceImpl implements DepartementService {

    private final DepartementRepo departementRepo;

    @Override
    public DepartementDTO addDepartment(DepartementDTO dto) {
        Departement departement = DepartementMapper.toEntity(dto);

        departement.setArchived(false);
        departement.setArchivedAt(null);

        return DepartementMapper.toDto(departementRepo.save(departement));
    }

    @Override
    public List<DepartementDTO> getAllDepartments() {
        return departementRepo.findByArchivedFalse()
                .stream()
                .map(DepartementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartementDTO> getDepartmentsByNom(String nom) {
        List<Departement> departments =
                departementRepo.findByNomContainingIgnoreCaseAndArchivedFalse(nom);

        if (departments.isEmpty()) {
            throw new DepartementNotFoundException("No active departments found with name: " + nom);
        }

        return departments.stream()
                .map(DepartementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DepartementDTO updateDepartment(Long id, DepartementDTO dto) {
        Departement departement = departementRepo.findById(id)
                .orElseThrow(() -> new DepartementNotFoundException("Department not found with id " + id));

        departement.setNom(dto.getNom());
        departement.setDescription(dto.getDescription());

        return DepartementMapper.toDto(departementRepo.save(departement));
    }

    @Override
    public String deleteDepartment(Long id) {
        Departement departement = departementRepo.findById(id)
                .orElseThrow(() -> new DepartementNotFoundException("Department not found with id " + id));

        if (departement.isArchived()) {
            return "Department already archived with id " + id;
        }

        departement.setArchived(true);
        departement.setArchivedAt(LocalDateTime.now());

        departementRepo.save(departement);

        return "Department archived successfully with id " + id
                + ". It will be permanently deleted after 7 days.";
    }

    @Override
    public List<DepartementDTO> getArchivedDepartments() {
        return departementRepo.findByArchivedTrue()
                .stream()
                .map(DepartementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DepartementDTO restoreDepartment(Long id) {
        Departement departement = departementRepo.findById(id)
                .orElseThrow(() -> new DepartementNotFoundException("Department not found with id " + id));

        if (!departement.isArchived()) {
            throw new RuntimeException("Department is not archived");
        }

        departement.setArchived(false);
        departement.setArchivedAt(null);

        return DepartementMapper.toDto(departementRepo.save(departement));
    }

    @Transactional
    @Override
    public void deleteOldArchivedDepartments() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<Departement> oldArchivedDepartments =
                departementRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        departementRepo.deleteAll(oldArchivedDepartments);
    }
}