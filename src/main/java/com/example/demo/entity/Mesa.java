package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mesas")
public class Mesa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String forma; // "circulo", "cuadrado", "hexagono"
    
    @Column(nullable = false)
    private int capacidad;
    
    @Column(nullable = false)
    private int orden = 0;
    
    @OneToMany(mappedBy = "mesa")
    private List<InvitadoPersona> personas = new ArrayList<>();
    
    // Constructores
    public Mesa() {}
    
    public Mesa(String nombre, String forma, int capacidad) {
        this.nombre = nombre;
        this.forma = forma;
        this.capacidad = capacidad;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getForma() {
        return forma;
    }
    
    public void setForma(String forma) {
        this.forma = forma;
    }
    
    public int getCapacidad() {
        return capacidad;
    }
    
    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }
    
    public int getOrden() {
        return orden;
    }
    
    public void setOrden(int orden) {
        this.orden = orden;
    }
    
    public List<InvitadoPersona> getPersonas() {
        return personas;
    }
    
    public void setPersonas(List<InvitadoPersona> personas) {
        this.personas = personas;
    }
}
