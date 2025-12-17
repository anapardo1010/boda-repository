package com.example.demo.dto;

public class ConfirmacionRequest {
    private String slug;
    private int pasesConfirmados;
    private String mensaje;
    
    public ConfirmacionRequest() {}
    
    public ConfirmacionRequest(String slug, int pasesConfirmados, String mensaje) {
        this.slug = slug;
        this.pasesConfirmados = pasesConfirmados;
        this.mensaje = mensaje;
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
}
