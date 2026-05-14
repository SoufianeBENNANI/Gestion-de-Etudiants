package org.sid.gestion_etudiant.Optionnel.Controller;

import org.sid.gestion_etudiant.Optionnel.Entity.Departement;
import org.sid.gestion_etudiant.Optionnel.Repository.DepartementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartementController {

    @Autowired
    private DepartementRepo departementRepo;

    @PostMapping
    public Departement create(@RequestBody Departement departement) {
        return departementRepo.save(departement);
    }

    @GetMapping
    public List<Departement> getAll() {
        return departementRepo.findAll();
    }
}
