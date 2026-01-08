package com.example.demo.controller;

import com.example.demo.entity.Invitado;
import com.example.demo.entity.InvitadoPersona;
import com.example.demo.repository.InvitadoRepository;
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
            
            // Configurar relación bidireccional correctamente
            if (invitado.getPersonas() != null && !invitado.getPersonas().isEmpty()) {
                for (InvitadoPersona persona : invitado.getPersonas()) {
                    persona.setInvitado(invitado);
                    persona.setEsAdicional(false);
                    persona.setConfirmado(false);
                    persona.setActivo(true);  // Activo por defecto
                }
            }
            
            Invitado saved = invitadoRepository.save(invitado);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al crear invitado: " + e.getMessage()));
        }
    }
    
    /**
     * Actualiza un invitado existente
     * PUT /api/admin/invitados/{id}
     * 
     * LÓGICA REFACTORIZADA:
     * - Actualización incremental (PATCH) sin DELETE
     * - Preserva datos de confirmación del usuario
     * - Usa soft-delete con campo 'activo'
     * - Separa invitados principales de pases adicionales
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
            
            // 1. Actualizar datos básicos
            existingInv.setNombreFamilia(invitado.getNombreFamilia());
            existingInv.setSlug(invitado.getSlug());
            existingInv.setTelefono(invitado.getTelefono());
            existingInv.setPasesTotales(invitado.getPasesTotales());
            
            // 2. GESTIÓN DE INVITADOS PRINCIPALES (esAdicional=false)
            // Solo el admin gestiona estos - NUNCA tocar los adicionales del usuario
            
            // 2.1. Obtener invitados principales actuales (activos)
            List<InvitadoPersona> principalesActuales = existingInv.getPersonas().stream()
                .filter(p -> !p.getEsAdicional() && p.getActivo())
                .toList();
            
            // 2.2. Crear mapa de invitados principales nuevos
            Map<String, InvitadoPersona> nuevosMap = new HashMap<>();
            if (invitado.getPersonas() != null) {
                for (InvitadoPersona nueva : invitado.getPersonas()) {
                    if (nueva.getNombreCompleto() != null && !nueva.getNombreCompleto().trim().isEmpty()) {
                        nuevosMap.put(nueva.getNombreCompleto().trim().toLowerCase(), nueva);
                    }
                }
            }
            
            // 2.3. Soft-delete: Marcar como inactivos los que ya no están en la lista
            for (InvitadoPersona actual : principalesActuales) {
                String nombreKey = actual.getNombreCompleto().trim().toLowerCase();
                if (!nuevosMap.containsKey(nombreKey)) {
                    // Ya no está en la lista nueva - SOFT DELETE
                    actual.setActivo(false);
                } else {
                    // Actualizar nombre si cambió (ej. corrección tipográfica)
                    InvitadoPersona nueva = nuevosMap.get(nombreKey);
                    actual.setNombreCompleto(nueva.getNombreCompleto());
                    actual.setOrden(nueva.getOrden());
                    // Eliminar del map para no crear duplicado
                    nuevosMap.remove(nombreKey);
                }
            }
            
            // 2.4. Agregar nuevos invitados principales que no existían
            for (InvitadoPersona nueva : nuevosMap.values()) {
                nueva.setInvitado(existingInv);
                nueva.setEsAdicional(false);
                nueva.setConfirmado(false);
                nueva.setActivo(true);
                existingInv.getPersonas().add(nueva);
            }
            
            // 3. PASES ADICIONALES: Completamente separados, el admin NO los toca
            // Solo ajustar disponibilidad basándose en pasesTotales
            // Los usuarios gestionan sus propios adicionales desde el frontend
            
            Invitado updated = invitadoRepository.save(existingInv);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
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
     * Cambia el estado de bloqueo de confirmación de una invitación
     * PUT /api/admin/invitados/{id}/toggle-bloqueo
     */
    @PutMapping("/invitados/{id}/toggle-bloqueo")
    @Transactional
    public ResponseEntity<?> toggleBloqueo(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        Optional<Invitado> existing = invitadoRepository.findById(id);
        
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Invitado no encontrado"));
        }
        
        try {
            Invitado invitado = existing.get();
            Boolean bloqueado = request.get("confirmacionBloqueada");
            if (bloqueado != null) {
                invitado.setConfirmacionBloqueada(bloqueado);
            }
            Invitado updated = invitadoRepository.save(invitado);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al cambiar bloqueo: " + e.getMessage()));
        }
    }
    
    /**
     * Exporta la lista de invitados en formato CSV
     * GET /api/admin/export-lista
     * FILTRADO: Solo personas ACTIVAS y CONFIRMADAS
     */
    @GetMapping("/export-lista")
    public ResponseEntity<String> exportarLista() {
        List<Invitado> invitados = invitadoRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("Familia,Nombre Completo,Mesa,Pases Totales,Pases Confirmados\n");
        
        for (Invitado inv : invitados) {
            if (inv.isConfirmado()) {
                // Solo mostrar personas ACTIVAS y con confirmado = true
                List<InvitadoPersona> personasConfirmadas = inv.getPersonas().stream()
                    .filter(p -> p.getActivo() && p.getConfirmado())  // ACTIVAS Y CONFIRMADAS
                    .toList();
                
                for (InvitadoPersona persona : personasConfirmadas) {
                    String nombreMesa = persona.getMesa() != null ? persona.getMesa().getNombre() : "Sin asignar";
                    
                    csv.append(String.format("\"%s\",\"%s\",\"%s\",%d,%d\n",
                        inv.getNombreFamilia(),
                        persona.getNombreCompleto(),
                        nombreMesa,
                        inv.getPasesTotales(),
                        inv.getPasesConfirmados()
                    ));
                }
            }
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "lista-invitados-con-mesas.csv");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(csv.toString());
    }
}
