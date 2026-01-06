package com.example.demo.controller;

import com.example.demo.dto.ConfirmacionRequest;
import com.example.demo.entity.Invitado;
import com.example.demo.entity.InvitadoPersona;
import com.example.demo.repository.InvitadoRepository;
import com.example.demo.repository.InvitadoPersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/invitados")
@CrossOrigin(origins = "*")
public class InvitadoController {
    
    @Autowired
    private InvitadoRepository invitadoRepository;
    
    @Autowired
    private InvitadoPersonaRepository invitadoPersonaRepository;
    
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
            
            // NUEVA LÓGICA: Manejar confirmaciones de personas específicas y adicionales
            if (request.getPersonasEspecificas() != null) {
                // Actualizar estado de confirmación de personas pre-llenadas
                for (ConfirmacionRequest.PersonaConfirmacion pc : request.getPersonasEspecificas()) {
                    Optional<InvitadoPersona> personaOpt = invitadoPersonaRepository.findById(pc.getPersonaId());
                    if (personaOpt.isPresent()) {
                        InvitadoPersona persona = personaOpt.get();
                        persona.setConfirmado(pc.isConfirmado());
                        invitadoPersonaRepository.save(persona);
                    }
                }
            }
            
            // Manejar nombres adicionales (pases extra)
            if (request.getNombresAdicionales() != null && !request.getNombresAdicionales().isEmpty()) {
                // Primero eliminar personas adicionales anteriores
                invitadoPersonaRepository.deleteByInvitadoIdAndEsAdicional(invitado.getId(), true);
                
                // Agregar nuevas personas adicionales
                int maxOrden = invitadoPersonaRepository.findMaxOrdenByInvitadoId(invitado.getId());
                for (int i = 0; i < request.getNombresAdicionales().size(); i++) {
                    String nombre = request.getNombresAdicionales().get(i);
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        InvitadoPersona persona = new InvitadoPersona(invitado, nombre.trim(), maxOrden + i + 1);
                        persona.setEsAdicional(true);
                        persona.setConfirmado(true); // Los adicionales siempre están confirmados
                        invitadoPersonaRepository.save(persona);
                    }
                }
            }
            
            // COMPATIBILIDAD: Si llega con la estructura antigua (nombresInvitados), manejarla
            if (request.getNombresInvitados() != null && !request.getNombresInvitados().isEmpty() 
                && request.getPersonasEspecificas() == null) {
                // Eliminar nombres anteriores
                invitadoPersonaRepository.deleteByInvitadoId(invitado.getId());
                
                // Agregar nuevos nombres
                for (int i = 0; i < request.getNombresInvitados().size(); i++) {
                    String nombre = request.getNombresInvitados().get(i);
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        InvitadoPersona persona = new InvitadoPersona(invitado, nombre.trim(), i + 1);
                        persona.setConfirmado(true);
                        persona.setEsAdicional(true);
                        invitadoPersonaRepository.save(persona);
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