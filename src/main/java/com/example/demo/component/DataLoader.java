package com.example.demo.component;

import com.example.demo.entity.Invitado;
import com.example.demo.repository.InvitadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private InvitadoRepository invitadoRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Cargar datos de prueba
        invitadoRepository.save(new Invitado("familia-perez", "Familia Pérez", 4));
        invitadoRepository.save(new Invitado("familia-garcia", "Familia García", 3));
        invitadoRepository.save(new Invitado("familia-martinez", "Familia Martínez", 5));
    }
}
