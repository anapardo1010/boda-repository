package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
    
    @Column(nullable = false)
    private boolean confirmacionBloqueada = false;
    
    @Column(length = 500)
    private String mensaje = "";
    
    @Column(length = 20)
    private String telefono = "";
    
    @OneToMany(mappedBy = "invitado", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<InvitadoPersona> personas = new ArrayList<>();
    
    // Constructores
    public Invitado() {}
    
    public Invitado(String slug, String nombreFamilia, int pasesTotales) {
        this.slug = slug;
        this.nombreFamilia = nombreFamilia;
        this.pasesTotales = pasesTotales;
    }
    
    public Invitado(String slug, String nombreFamilia, int pasesTotales, String telefono) {
        this.slug = slug;
        this.nombreFamilia = nombreFamilia;
        this.pasesTotales = pasesTotales;
        this.telefono = telefono;
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
    
    public boolean isConfirmacionBloqueada() {
        return confirmacionBloqueada;
    }
    
    public void setConfirmacionBloqueada(boolean confirmacionBloqueada) {
        this.confirmacionBloqueada = confirmacionBloqueada;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public List<InvitadoPersona> getPersonas() {
        return personas;
    }
    
    public void setPersonas(List<InvitadoPersona> personas) {
        this.personas = personas;
    }
}
