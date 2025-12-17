package com.example.demo.controller;

import com.example.demo.dto.ConfirmacionRequest;
import com.example.demo.entity.Invitado;
import com.example.demo.repository.InvitadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/invitados")
@CrossOrigin(origins = "*")
public class InvitadoController {
    
    @Autowired
    private InvitadoRepository invitadoRepository;
    
    /**
     * Obtiene los datos de una familia por su slug
     * GET /api/invitados/{slug}
     */
    @GetMapping("/{slug}")
    public ResponseEntity<Invitado> getInvitado(@PathVariable String slug) {
        Optional<Invitado> invitado = invitadoRepository.findBySlug(slug);
        
        if (invitado.isPresent()) {
            return ResponseEntity.ok(invitado.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Confirma la asistencia de una familia
     * POST /api/invitados/confirmar
     */
    @PostMapping("/confirmar")
    public ResponseEntity<Invitado> confirmarAsistencia(@RequestBody ConfirmacionRequest request) {
        Optional<Invitado> invitadoOpt = invitadoRepository.findBySlug(request.getSlug());
        
        if (invitadoOpt.isPresent()) {
            Invitado invitado = invitadoOpt.get();
            
            // Validar que no confirme más personas de las permitidas
            if (request.getPasesConfirmados() > invitado.getPasesTotales()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            invitado.setPasesConfirmados(request.getPasesConfirmados());
            invitado.setConfirmado(request.getPasesConfirmados() > 0);
            invitado.setMensaje(request.getMensaje() != null ? request.getMensaje() : "");
            
            Invitado guardado = invitadoRepository.save(invitado);
            return ResponseEntity.ok(guardado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
