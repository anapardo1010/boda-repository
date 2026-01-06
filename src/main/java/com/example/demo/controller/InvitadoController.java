package com.example.demo.controller;

import com.example.demo.dto.ConfirmacionRequest;
import com.example.demo.entity.Invitado;
import com.example.demo.entity.InvitadoPersona;
import com.example.demo.repository.InvitadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
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
            
            // PERSONAS ESPECÍFICAS (esAdicional = false): Solo actualizar confirmado, NUNCA eliminar
            if (request.getPersonasEspecificas() != null) {
                for (ConfirmacionRequest.PersonaConfirmacion pc : request.getPersonasEspecificas()) {
                    invitado.getPersonas().stream()
                        .filter(p -> p.getId() != null && p.getId().equals(pc.getPersonaId()))
                        .findFirst()
                        .ifPresent(persona -> persona.setConfirmado(pc.isConfirmado()));
                }
            }
            
            // PERSONAS ADICIONALES (esAdicional = true): Eliminar y reemplazar completamente
            if (request.getNombresAdicionales() != null) {
                // Remover TODAS las personas adicionales anteriores
                invitado.getPersonas().removeIf(p -> p.getEsAdicional());
                
                // Agregar SOLO las nuevas personas adicionales con nombre
                int maxOrden = invitado.getPersonas().stream()
                    .mapToInt(InvitadoPersona::getOrden)
                    .max()
                    .orElse(0);
                    
                for (int i = 0; i < request.getNombresAdicionales().size(); i++) {
                    String nombre = request.getNombresAdicionales().get(i);
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        InvitadoPersona persona = new InvitadoPersona(invitado, nombre.trim(), maxOrden + i + 1);
                        persona.setEsAdicional(true);
                        persona.setConfirmado(true);  // Adicionales siempre confirmados
                        invitado.getPersonas().add(persona);
                    }
                }
            }
            
            Invitado guardado = invitadoRepository.save(invitado);
            return ResponseEntity.ok(guardado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}