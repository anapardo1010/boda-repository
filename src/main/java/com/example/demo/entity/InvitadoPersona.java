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
    
    // Constructors
    public InvitadoPersona() {}
    
    public InvitadoPersona(Invitado invitado, String nombreCompleto, Integer orden) {
        this.invitado = invitado;
        this.nombreCompleto = nombreCompleto;
        this.orden = orden;
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
}
