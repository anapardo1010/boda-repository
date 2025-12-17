package com.example.demo.repository;

import com.example.demo.entity.Invitado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitadoRepository extends JpaRepository<Invitado, Long> {
    Optional<Invitado> findBySlug(String slug);
}
