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

import java.util.List;
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
            
            // PERSONAS ADICIONALES (esAdicional = true): Actualizar existentes o crear nuevas
            if (request.getNombresAdicionales() != null) {
                // Primero, eliminar las personas adicionales que ya NO están en la lista
                List<Long> idsAdicionales = request.getNombresAdicionales().stream()
                    .map(ConfirmacionRequest.PersonaAdicional::getPersonaId)
                    .filter(id -> id != null)
                    .toList();
                
                invitado.getPersonas().removeIf(p -> 
                    p.getEsAdicional() && p.getId() != null && !idsAdicionales.contains(p.getId())
                );
                
                // Luego, actualizar o crear personas adicionales
                int maxOrden = invitado.getPersonas().stream()
                    .mapToInt(InvitadoPersona::getOrden)
                    .max()
                    .orElse(0);
                    
                for (int i = 0; i < request.getNombresAdicionales().size(); i++) {
                    ConfirmacionRequest.PersonaAdicional pa = request.getNombresAdicionales().get(i);
                    String nombre = pa.getNombre();
                    
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        if (pa.getPersonaId() != null) {
                            // Actualizar persona existente
                            invitado.getPersonas().stream()
                                .filter(p -> p.getId() != null && p.getId().equals(pa.getPersonaId()))
                                .findFirst()
                                .ifPresent(persona -> {
                                    persona.setNombreCompleto(nombre.trim());
                                    persona.setConfirmado(true);
                                });
                        } else {
                            // Crear nueva persona adicional
                            InvitadoPersona persona = new InvitadoPersona(invitado, nombre.trim(), maxOrden + i + 1);
                            persona.setEsAdicional(true);
                            persona.setConfirmado(true);
                            invitado.getPersonas().add(persona);
                        }
                    }
                }
            }
            
            // ACTUALIZAR pasesConfirmados: contar personas con confirmado=true
            long confirmados = invitado.getPersonas().stream()
                .filter(InvitadoPersona::getConfirmado)
                .count();
            invitado.setPasesConfirmados((int) confirmados);
            invitado.setConfirmado(confirmados > 0);
            
            Invitado guardado = invitadoRepository.save(invitado);
            return ResponseEntity.ok(guardado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}