package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "invitados", uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
public class Invitado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String slug;
    
    @Column(nullable = false)
    private String nombreFamilia;
    
    @Column(nullable = false)
    private int pasesTotales;
    
    @Column(nullable = false)
    private int pasesConfirmados = 0;
    
    @Column(nullable = false)
    private boolean confirmado = false;
    
    @Column(length = 500)
    private String mensaje = "";
    
    // Constructores
    public Invitado() {}
    
    public Invitado(String slug, String nombreFamilia, int pasesTotales) {
        this.slug = slug;
        this.nombreFamilia = nombreFamilia;
        this.pasesTotales = pasesTotales;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getNombreFamilia() {
        return nombreFamilia;
    }
    
    public void setNombreFamilia(String nombreFamilia) {
        this.nombreFamilia = nombreFamilia;
    }
    
    public int getPasesTotales() {
        return pasesTotales;
    }
    
    public void setPasesTotales(int pasesTotales) {
        this.pasesTotales = pasesTotales;
    }
    
    public int getPasesConfirmados() {
        return pasesConfirmados;
    }
    
    public void setPasesConfirmados(int pasesConfirmados) {
        this.pasesConfirmados = pasesConfirmados;
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
