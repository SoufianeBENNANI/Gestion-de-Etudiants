package org.sid.gestion_etudiant.Metier.mapper;

import org.sid.gestion_etudiant.Metier.Entity.Payement;
import org.sid.gestion_etudiant.Metier.dto.PayementDTO;

public class PayementMapper {

    public static PayementDTO toDto(Payement payement) {
        PayementDTO payementDTO = new PayementDTO();

        payementDTO.setId(payement.getId());
        payementDTO.setAmount(payement.getAmount());
        payementDTO.setDate(payement.getDate());
        payementDTO.setStatus(payement.getStatus());
        payementDTO.setArchived(payement.isArchived());
        payementDTO.setArchivedAt(payement.getArchivedAt());

        if (payement.getStudent() != null) {
            payementDTO.setStudentId(payement.getStudent().getId());
            payementDTO.setStudentNom(payement.getStudent().getNom());
            payementDTO.setStudentPrenom(payement.getStudent().getPrenom());
        }

        return payementDTO;
    }

    public static Payement toEntity(PayementDTO payementDTO) {
        Payement payement = new Payement();

        payement.setAmount(payementDTO.getAmount());
        payement.setDate(payementDTO.getDate());
        payement.setStatus(payementDTO.getStatus());
        payement.setArchived(payementDTO.isArchived());
        payement.setArchivedAt(payementDTO.getArchivedAt());

        return payement;
    }
}