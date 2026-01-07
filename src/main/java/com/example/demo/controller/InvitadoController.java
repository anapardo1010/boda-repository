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
     * 
     * LÓGICA REFACTORIZADA:
     * - Actualización incremental sin DELETE
     * - Soft delete con campo 'activo'
     * - Preserva datos previamente ingresados
     * - Separación clara entre principales y adicionales
     */
    @PostMapping("/confirmar")
    @Transactional
    public ResponseEntity<Invitado> confirmarAsistencia(@RequestBody ConfirmacionRequest request) {
        Optional<Invitado> invitadoOpt = invitadoRepository.findBySlug(request.getSlug());
        
        if (invitadoOpt.isPresent()) {
            Invitado invitado = invitadoOpt.get();
            
            // Actualizar mensaje
            invitado.setMensaje(request.getMensaje() != null ? request.getMensaje() : "");
            
            // 1. INVITADOS PRINCIPALES (esAdicional=false)
            // Solo actualizar confirmado - NUNCA eliminar ni desactivar
            if (request.getPersonasEspecificas() != null) {
                for (ConfirmacionRequest.PersonaConfirmacion pc : request.getPersonasEspecificas()) {
                    invitado.getPersonas().stream()
                        .filter(p -> p.getId() != null && p.getId().equals(pc.getPersonaId()) && p.getActivo())
                        .findFirst()
                        .ifPresent(persona -> persona.setConfirmado(pc.isConfirmado()));
                }
            }
            
            // 2. PASES ADICIONALES (esAdicional=true)
            // Gestión incremental con soft delete
            if (request.getNombresAdicionales() != null) {
                
                // 2.1. Obtener pases adicionales actuales (activos)
                List<InvitadoPersona> adicionalesActuales = invitado.getPersonas().stream()
                    .filter(p -> p.getEsAdicional() && p.getActivo())
                    .toList();
                
                // 2.2. Crear lista de IDs enviados por el usuario
                List<Long> idsEnviados = request.getNombresAdicionales().stream()
                    .map(ConfirmacionRequest.PersonaAdicional::getPersonaId)
                    .filter(id -> id != null)
                    .toList();
                
                // 2.3. SOFT DELETE: Desactivar los que ya no están en la lista
                for (InvitadoPersona actual : adicionalesActuales) {
                    if (actual.getId() != null && !idsEnviados.contains(actual.getId())) {
                        // Ya no está en la lista - SOFT DELETE
                        actual.setActivo(false);
                        actual.setConfirmado(false);
                    }
                }
                
                // 2.4. Actualizar existentes o crear nuevos
                int maxOrden = invitado.getPersonas().stream()
                    .mapToInt(InvitadoPersona::getOrden)
                    .max()
                    .orElse(0);
                
                int ordenCounter = maxOrden + 1;
                
                for (ConfirmacionRequest.PersonaAdicional pa : request.getNombresAdicionales()) {
                    String nombre = pa.getNombre();
                    
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        if (pa.getPersonaId() != null) {
                            // ACTUALIZAR persona existente (preservar datos)
                            invitado.getPersonas().stream()
                                .filter(p -> p.getId() != null && p.getId().equals(pa.getPersonaId()))
                                .findFirst()
                                .ifPresent(persona -> {
                                    persona.setNombreCompleto(nombre.trim());
                                    persona.setConfirmado(true);
                                    persona.setActivo(true);  // Reactivar si estaba inactivo
                                });
                        } else {
                            // CREAR nueva persona adicional
                            InvitadoPersona persona = new InvitadoPersona(invitado, nombre.trim(), ordenCounter++);
                            persona.setEsAdicional(true);
                            persona.setConfirmado(true);
                            persona.setActivo(true);
                            invitado.getPersonas().add(persona);
                        }
                    }
                }
            }
            
            // 3. CALCULAR pasesConfirmados: contar ACTIVOS con confirmado=true
            long confirmados = invitado.getPersonas().stream()
                .filter(p -> p.getActivo() && p.getConfirmado())
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