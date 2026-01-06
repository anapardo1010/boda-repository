package com.example.demo.dto;

import java.util.List;

public class ConfirmacionRequest {
    private String slug;
    private int pasesConfirmados;
    private String mensaje;
    private List<String> nombresInvitados;
    
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
}
