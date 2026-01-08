package com.example.demo.controller;

import com.example.demo.entity.Mesa;
import com.example.demo.entity.InvitadoPersona;
import com.example.demo.repository.MesaRepository;
import com.example.demo.repository.InvitadoPersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/mesas")
@CrossOrigin(origins = "*")
public class MesaController {
    
    @Autowired
    private MesaRepository mesaRepository;
    
    @Autowired
    private InvitadoPersonaRepository personaRepository;
    
    /**
     * Obtiene todas las mesas con sus personas asignadas
     */
    @GetMapping
    public ResponseEntity<List<Mesa>> getAllMesas() {
        List<Mesa> mesas = mesaRepository.findAllByOrderByOrdenAsc();
        return ResponseEntity.ok(mesas);
    }
    
    /**
     * Crea una nueva mesa
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> createMesa(@RequestBody Mesa mesa) {
        try {
            // Obtener el máximo orden actual
            List<Mesa> mesas = mesaRepository.findAll();
            int maxOrden = mesas.stream()
                .mapToInt(Mesa::getOrden)
                .max()
                .orElse(0);
            mesa.setOrden(maxOrden + 1);
            
            Mesa saved = mesaRepository.save(mesa);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al crear mesa: " + e.getMessage()));
        }
    }
    
    /**
     * Actualiza una mesa existente
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateMesa(@PathVariable Long id, @RequestBody Mesa mesa) {
        Optional<Mesa> existing = mesaRepository.findById(id);
        
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Mesa no encontrada"));
        }
        
        try {
            Mesa existingMesa = existing.get();
            existingMesa.setNombre(mesa.getNombre());
            existingMesa.setForma(mesa.getForma());
            existingMesa.setCapacidad(mesa.getCapacidad());
            
            Mesa updated = mesaRepository.save(existingMesa);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al actualizar mesa: " + e.getMessage()));
        }
    }
    
    /**
     * Elimina una mesa
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteMesa(@PathVariable Long id) {
        if (!mesaRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Mesa no encontrada"));
        }
        
        try {
            // Desasignar personas de la mesa antes de eliminar
            Optional<Mesa> mesa = mesaRepository.findById(id);
            if (mesa.isPresent()) {
                for (InvitadoPersona persona : mesa.get().getPersonas()) {
                    persona.setMesa(null);
                    personaRepository.save(persona);
                }
            }
            
            mesaRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Mesa eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al eliminar mesa: " + e.getMessage()));
        }
    }
    
    /**
     * Asigna una persona a una mesa
     */
    @PutMapping("/{mesaId}/asignar/{personaId}")
    @Transactional
    public ResponseEntity<?> asignarPersona(@PathVariable Long mesaId, @PathVariable Long personaId) {
        Optional<Mesa> mesaOpt = mesaRepository.findById(mesaId);
        Optional<InvitadoPersona> personaOpt = personaRepository.findById(personaId);
        
        if (mesaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Mesa no encontrada"));
        }
        
        if (personaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Persona no encontrada"));
        }
        
        try {
            Mesa mesa = mesaOpt.get();
            InvitadoPersona persona = personaOpt.get();
            
            // Verificar capacidad
            long asignados = persona.getMesa() != null && persona.getMesa().getId().equals(mesaId) 
                ? mesa.getPersonas().size() 
                : mesa.getPersonas().size() + 1;
                
            if (asignados > mesa.getCapacidad()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Mesa llena. Capacidad: " + mesa.getCapacidad()));
            }
            
            persona.setMesa(mesa);
            personaRepository.save(persona);
            
            return ResponseEntity.ok(Map.of("message", "Persona asignada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al asignar persona: " + e.getMessage()));
        }
    }
    
    /**
     * Desasigna una persona de su mesa
     */
    @PutMapping("/desasignar/{personaId}")
    @Transactional
    public ResponseEntity<?> desasignarPersona(@PathVariable Long personaId) {
        Optional<InvitadoPersona> personaOpt = personaRepository.findById(personaId);
        
        if (personaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Persona no encontrada"));
        }
        
        try {
            InvitadoPersona persona = personaOpt.get();
            persona.setMesa(null);
            personaRepository.save(persona);
            
            return ResponseEntity.ok(Map.of("message", "Persona desasignada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al desasignar persona: " + e.getMessage()));
        }
    }
    
    /**
     * Obtiene todas las personas sin mesa asignada
     */
    @GetMapping("/personas-sin-mesa")
    public ResponseEntity<List<InvitadoPersona>> getPersonasSinMesa() {
        List<InvitadoPersona> personas = personaRepository.findAll().stream()
            .filter(p -> p.getActivo() && p.getConfirmado() && p.getMesa() == null)
            .toList();
        return ResponseEntity.ok(personas);
    }
    
    /**
     * Exporta la distribución de mesas
     */
    @GetMapping("/exportar")
    public ResponseEntity<Map<String, Object>> exportarDistribucion() {
        List<Mesa> mesas = mesaRepository.findAllByOrderByOrdenAsc();
        
        Map<String, Object> distribucion = new HashMap<>();
        distribucion.put("totalMesas", mesas.size());
        distribucion.put("mesas", mesas.stream().map(mesa -> {
            Map<String, Object> mesaData = new HashMap<>();
            mesaData.put("nombre", mesa.getNombre());
            mesaData.put("forma", mesa.getForma());
            mesaData.put("capacidad", mesa.getCapacidad());
            mesaData.put("ocupados", mesa.getPersonas().size());
            mesaData.put("personas", mesa.getPersonas().stream()
                .map(p -> p.getNombreCompleto())
                .toList());
            return mesaData;
        }).toList());
        
        return ResponseEntity.ok(distribucion);
    }
}
