package com.example.demo.controller;

import com.example.demo.entity.Invitado;
import com.example.demo.entity.InvitadoPersona;
import com.example.demo.repository.InvitadoRepository;
import com.example.demo.repository.InvitadoPersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private InvitadoRepository invitadoRepository;
    
    @Autowired
    private InvitadoPersonaRepository invitadoPersonaRepository;
    
    /**
     * Obtiene todos los invitados
     * GET /api/admin/invitados
     */
    @GetMapping("/invitados")
    public ResponseEntity<List<Invitado>> getAllInvitados() {
        List<Invitado> invitados = invitadoRepository.findAll();
        return ResponseEntity.ok(invitados);
    }
    
    /**
     * Obtiene métricas del dashboard
     * GET /api/admin/metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        List<Invitado> invitados = invitadoRepository.findAll();
        
        int totalInvitados = invitados.size();
        int totalPases = invitados.stream().mapToInt(Invitado::getPasesTotales).sum();
        int pasesConfirmados = invitados.stream().mapToInt(Invitado::getPasesConfirmados).sum();
        long familiasConfirmadas = invitados.stream().filter(Invitado::isConfirmado).count();
        int pasesDisponibles = totalPases - pasesConfirmados;
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalInvitados", totalInvitados);
        metrics.put("totalPases", totalPases);
        metrics.put("pasesConfirmados", pasesConfirmados);
        metrics.put("pasesDisponibles", pasesDisponibles);
        metrics.put("familiasConfirmadas", familiasConfirmadas);
        metrics.put("porcentajeConfirmacion", totalPases > 0 ? (pasesConfirmados * 100.0 / totalPases) : 0);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Crea un nuevo invitado
     * POST /api/admin/invitados
     */
    @PostMapping("/invitados")
    @Transactional
    public ResponseEntity<?> createInvitado(@RequestBody Invitado invitado) {
        try {
            // Validar que el slug no exista
            if (invitadoRepository.findBySlug(invitado.getSlug()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Ya existe un invitado con ese slug"));
            }
            
            // Extraer personas antes de guardar (para evitar cascade issues)
            List<InvitadoPersona> personas = invitado.getPersonas();
            invitado.setPersonas(null);
            
            Invitado saved = invitadoRepository.save(invitado);
            
            // Guardar personas si existen
            if (personas != null && !personas.isEmpty()) {
                for (InvitadoPersona persona : personas) {
                    persona.setInvitado(saved);
                    persona.setEsAdicional(false);
                    persona.setConfirmado(false);
                    invitadoPersonaRepository.save(persona);
                }
            }
            
            // Recargar con personas
            saved = invitadoRepository.findById(saved.getId()).orElse(saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al crear invitado: " + e.getMessage()));
        }
    }
    
    /**
     * Actualiza un invitado existente
     * PUT /api/admin/invitados/{id}
     */
    @PutMapping("/invitados/{id}")
    @Transactional
    public ResponseEntity<?> updateInvitado(@PathVariable Long id, @RequestBody Invitado invitado) {
        Optional<Invitado> existing = invitadoRepository.findById(id);
        
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Invitado no encontrado"));
        }
        
        try {
            Invitado existingInv = existing.get();
            
            // Actualizar datos básicos
            existingInv.setNombreFamilia(invitado.getNombreFamilia());
            existingInv.setSlug(invitado.getSlug());
            existingInv.setTelefono(invitado.getTelefono());
            existingInv.setPasesTotales(invitado.getPasesTotales());
            
            // Manejar personas
            List<InvitadoPersona> nuevasPersonas = invitado.getPersonas();
            
            if (nuevasPersonas != null) {
                // Eliminar solo personas que no son adicionales (pre-llenadas por admin)
                invitadoPersonaRepository.deleteByInvitadoIdAndEsAdicional(id, false);
                
                // Agregar nuevas personas
                for (InvitadoPersona persona : nuevasPersonas) {
                    if (persona.getNombreCompleto() != null && !persona.getNombreCompleto().trim().isEmpty()) {
                        persona.setInvitado(existingInv);
                        persona.setEsAdicional(false);
                        persona.setConfirmado(false);
                        invitadoPersonaRepository.save(persona);
                    }
                }
            }
            
            Invitado updated = invitadoRepository.save(existingInv);
            // Recargar con personas
            updated = invitadoRepository.findById(updated.getId()).orElse(updated);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al actualizar: " + e.getMessage()));
        }
    }
    
    /**
     * Elimina un invitado
     * DELETE /api/admin/invitados/{id}
     */
    @DeleteMapping("/invitados/{id}")
    public ResponseEntity<?> deleteInvitado(@PathVariable Long id) {
        if (!invitadoRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Invitado no encontrado"));
        }
        
        try {
            invitadoRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Invitado eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al eliminar: " + e.getMessage()));
        }
    }
    
    /**
     * Exporta la lista de invitados en formato CSV
     * GET /api/admin/export-lista
     */
    @GetMapping("/export-lista")
    public ResponseEntity<String> exportarLista() {
        List<Invitado> invitados = invitadoRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("Familia,Pases Totales,Pases Confirmados,Nombres de Invitados\n");
        
        for (Invitado inv : invitados) {
            if (inv.isConfirmado()) {
                String nombresPersonas = inv.getPersonas().stream()
                    .map(InvitadoPersona::getNombreCompleto)
                    .collect(Collectors.joining("; "));
                
                csv.append(String.format("\"%s\",%d,%d,\"%s\"\n",
                    inv.getNombreFamilia(),
                    inv.getPasesTotales(),
                    inv.getPasesConfirmados(),
                    nombresPersonas.isEmpty() ? "No especificado" : nombresPersonas
                ));
            }
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "lista-invitados-boda.csv");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(csv.toString());
    }
}
