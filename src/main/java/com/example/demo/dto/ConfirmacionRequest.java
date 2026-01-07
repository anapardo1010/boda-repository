package com.example.demo.dto;

import java.util.List;

public class ConfirmacionRequest {
    private String slug;
    private int pasesConfirmados;
    private String mensaje;
    private List<String> nombresInvitados; // Mantener por compatibilidad
    private List<PersonaConfirmacion> personasEspecificas; // Nueva estructura
    private List<PersonaAdicional> nombresAdicionales; // Nuevos pases adicionales con ID
    
    public ConfirmacionRequest() {}
    
    public ConfirmacionRequest(String slug, int pasesConfirmados, String mensaje, List<String> nombresInvitados) {
        this.slug = slug;
        this.pasesConfirmados = pasesConfirmados;
        this.mensaje = mensaje;
        this.nombresInvitados = nombresInvitados;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public int getPasesConfirmados() {
        return pasesConfirmados;
    }
    
    public void setPasesConfirmados(int pasesConfirmados) {
        this.pasesConfirmados = pasesConfirmados;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public List<String> getNombresInvitados() {
        return nombresInvitados;
    }
    
    public void setNombresInvitados(List<String> nombresInvitados) {
        this.nombresInvitados = nombresInvitados;
    }
    
    public List<PersonaConfirmacion> getPersonasEspecificas() {
        return personasEspecificas;
    }
    
    public void setPersonasEspecificas(List<PersonaConfirmacion> personasEspecificas) {
        this.personasEspecificas = personasEspecificas;
    }
    
    public List<PersonaAdicional> getNombresAdicionales() {
        return nombresAdicionales;
    }
    
    public void setNombresAdicionales(List<PersonaAdicional> nombresAdicionales) {
        this.nombresAdicionales = nombresAdicionales;
    }
    
    // Inner class para las confirmaciones de personas específicas
    public static class PersonaConfirmacion {
        private Long personaId;
        private boolean confirmado;
        
        public PersonaConfirmacion() {}
        
        public PersonaConfirmacion(Long personaId, boolean confirmado) {
            this.personaId = personaId;
            this.confirmado = confirmado;
        }
        
        public Long getPersonaId() {
            return personaId;
        }
        
        public void setPersonaId(Long personaId) {
            this.personaId = personaId;
        }
        
        public boolean isConfirmado() {
            return confirmado;
        }
        
        public void setConfirmado(boolean confirmado) {
            this.confirmado = confirmado;
        }
    }
    
    // Inner class para las personas adicionales con ID opcional
    public static class PersonaAdicional {
        private Long personaId; // null si es nueva
        private String nombre;
        
        public PersonaAdicional() {}
        
        public PersonaAdicional(Long personaId, String nombre) {
            this.personaId = personaId;
            this.nombre = nombre;
        }
        
        public Long getPersonaId() {
            return personaId;
        }
        
        public void setPersonaId(Long personaId) {
            this.personaId = personaId;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
    }
}
