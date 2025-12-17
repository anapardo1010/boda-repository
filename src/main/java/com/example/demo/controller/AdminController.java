package com.example.demo.controller;

import com.example.demo.entity.Invitado;
import com.example.demo.repository.InvitadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private InvitadoRepository invitadoRepository;
    
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
    public ResponseEntity<?> createInvitado(@RequestBody Invitado invitado) {
        try {
            // Validar que el slug no exista
            if (invitadoRepository.findBySlug(invitado.getSlug()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Ya existe un invitado con ese slug"));
            }
            
            Invitado saved = invitadoRepository.save(invitado);
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
    public ResponseEntity<?> updateInvitado(@PathVariable Long id, @RequestBody Invitado invitado) {
        Optional<Invitado> existing = invitadoRepository.findById(id);
        
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Invitado no encontrado"));
        }
        
        try {
            invitado.setId(id);
            Invitado updated = invitadoRepository.save(invitado);
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
}
