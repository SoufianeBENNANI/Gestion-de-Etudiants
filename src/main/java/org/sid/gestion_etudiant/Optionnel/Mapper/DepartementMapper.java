package org.sid.gestion_etudiant.Optionnel.Mapper;


import org.sid.gestion_etudiant.Optionnel.DTO.DepartementDTO;
import org.sid.gestion_etudiant.Optionnel.Entity.Departement;

public class DepartementMapper {

    public static Departement toEntity(DepartementDTO departementDTO) {
        Departement departement = new Departement();

        departement.setNom(departementDTO.getNom());
        departement.setDescription(departementDTO.getDescription());

        return departement;
    }

    public static DepartementDTO toDto(Departement departement) {
        DepartementDTO departementDTO = new DepartementDTO();

        departementDTO.setId(departement.getId());
        departementDTO.setNom(departement.getNom());
        departementDTO.setDescription(departement.getDescription());

        return departementDTO;
    }
}