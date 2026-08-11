package org.sid.gestion_etudiant.Metier.Service;

import org.sid.gestion_etudiant.Metier.dto.PayementDTO;

import java.util.List;

public interface PayementService {

    PayementDTO addPayement(PayementDTO payementDTO);

    List<PayementDTO> getAllPayements();

    List<PayementDTO> getMyPayements(String keycloakId, String email);

    List<PayementDTO> getArchivedPayements();

    PayementDTO getPayementById(Long id);

    PayementDTO updatePayement(Long id, PayementDTO payementDTO);

    String deletePayement(Long id);

    PayementDTO restorePayement(Long id);

    void deleteOldArchivedPayements();

    byte[] generatePayementsPdf();
}