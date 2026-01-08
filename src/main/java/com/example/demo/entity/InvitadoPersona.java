package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "invitado_personas")
public class InvitadoPersona {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "invitado_id", nullable = false)
    @JsonIgnore  // Evita la serialización del invitado para prevenir referencia circular
    private Invitado invitado;
    
    @Column(nullable = false)
    private String nombreCompleto;
    
    @Column(nullable = false)
    private Integer orden;
    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean confirmado = false;
    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean esAdicional = false;  // True si fue agregado por el invitado, false si fue pre-llenado
    
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean activo = true;  // Soft delete - false para ocultar sin eliminar de DB
    
    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;
    
    // Constructors
    public InvitadoPersona() {}
    
    public InvitadoPersona(Invitado invitado, String nombreCompleto, Integer orden) {
        this.invitado = invitado;
        this.nombreCompleto = nombreCompleto;
        this.orden = orden;
        this.confirmado = false;
        this.esAdicional = false;
        this.activo = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Invitado getInvitado() {
        return invitado;
    }
    
    public void setInvitado(Invitado invitado) {
        this.invitado = invitado;
    }
    
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    
    public Integer getOrden() {
        return orden;
    }
    
    public void setOrden(Integer orden) {
        this.orden = orden;
    }
    
    public Boolean getConfirmado() {
        return confirmado;
    }
    
    public void setConfirmado(Boolean confirmado) {
        this.confirmado = confirmado;
    }
    
    public Boolean getEsAdicional() {
        return esAdicional;
    }
    
    public void setEsAdicional(Boolean esAdicional) {
        this.esAdicional = esAdicional;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public Mesa getMesa() {
        return mesa;
    }
    
    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }
}
