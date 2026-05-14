package org.sid.gestion_etudiant.IA.ControllerIA;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.IA.Entity.IALog;
import org.sid.gestion_etudiant.IA.Service.IALogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@AllArgsConstructor
public class IALogController {

    private final IALogService logService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<IALog> getAllLogs(){
        return logService.getAll();
    }
}
