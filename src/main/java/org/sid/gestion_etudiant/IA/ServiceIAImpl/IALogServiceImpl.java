package org.sid.gestion_etudiant.IA.ServiceIAImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.IA.Entity.IALog;
import org.sid.gestion_etudiant.IA.Repository.IALogRepo;
import org.sid.gestion_etudiant.IA.Service.IALogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class IALogServiceImpl implements IALogService {

    private final IALogRepo logRepo;

    @Override
    public List<IALog> getAll() {
        return logRepo.findAll();
    }
}