package org.sid.gestion_etudiant.Metier.ServiceImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Entity.Payement;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.Repository.PayementRepo;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.sid.gestion_etudiant.Metier.Service.PayementService;
import org.sid.gestion_etudiant.Metier.dto.PayementDTO;
import org.sid.gestion_etudiant.Metier.exception.PayementNotFoundException;
import org.sid.gestion_etudiant.Metier.exception.StudentNotFoundException;
import org.sid.gestion_etudiant.Metier.mapper.PayementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class PayementServiceImpl implements PayementService {

    private final PayementRepo payementRepo;
    private final StudentRepo studentRepo;

    @Override
    public PayementDTO addPayement(PayementDTO payementDTO) {
        Student student = studentRepo.findById(payementDTO.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        Payement payement = PayementMapper.toEntity(payementDTO);

        payement.setStudent(student);
        payement.setArchived(false);
        payement.setArchivedAt(null);

        return PayementMapper.toDto(payementRepo.save(payement));
    }

    @Override
    public List<PayementDTO> getAllPayements() {
        return payementRepo.findByArchivedFalse()
                .stream()
                .map(PayementMapper::toDto)
                .toList();
    }

    @Override
    public List<PayementDTO> getArchivedPayements() {
        return payementRepo.findByArchivedTrue()
                .stream()
                .map(PayementMapper::toDto)
                .toList();
    }

    @Override
    public PayementDTO getPayementById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Le champ ID est obligatoire");
        }

        Payement payement = payementRepo.findById(id)
                .orElseThrow(() -> new PayementNotFoundException("Payement non trouvée avec id : " + id));

        return PayementMapper.toDto(payement);
    }

    @Override
    public PayementDTO updatePayement(Long id, PayementDTO payementDTO) {
        Payement payement = payementRepo.findById(id)
                .orElseThrow(() -> new PayementNotFoundException("Payement non trouvée avec id : " + id));

        if (payement.isArchived()) {
            throw new RuntimeException("Impossible de modifier un payement archivé. Restaurer d'abord.");
        }

        payement.setAmount(payementDTO.getAmount());
        payement.setDate(payementDTO.getDate());
        payement.setStatus(payementDTO.getStatus());

        if (payementDTO.getStudentId() != null) {
            Student student = studentRepo.findById(payementDTO.getStudentId())
                    .orElseThrow(() -> new StudentNotFoundException("Student not found"));
            payement.setStudent(student);
        }

        return PayementMapper.toDto(payementRepo.save(payement));
    }

    @Override
    public String deletePayement(Long id) {
        Payement payement = payementRepo.findById(id)
                .orElseThrow(() -> new PayementNotFoundException("Payement non trouvée avec l'id : " + id));

        if (payement.isArchived()) {
            return "Payement déjà archivé avec id : " + id;
        }

        payement.setArchived(true);
        payement.setArchivedAt(LocalDateTime.now());

        payementRepo.save(payement);

        return "Payement archivé avec succès. Il sera supprimé définitivement après 7 jours.";
    }

    @Override
    public PayementDTO restorePayement(Long id) {
        Payement payement = payementRepo.findById(id)
                .orElseThrow(() -> new PayementNotFoundException("Payement non trouvée avec l'id : " + id));

        if (!payement.isArchived()) {
            throw new RuntimeException("Ce payement n'est pas archivé.");
        }

        payement.setArchived(false);
        payement.setArchivedAt(null);

        return PayementMapper.toDto(payementRepo.save(payement));
    }

    @Override
    @Transactional
    public void deleteOldArchivedPayements() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<Payement> oldArchivedPayements =
                payementRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        payementRepo.deleteAll(oldArchivedPayements);
    }
}