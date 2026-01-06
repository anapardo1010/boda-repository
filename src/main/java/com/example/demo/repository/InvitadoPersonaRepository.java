package com.example.demo.repository;

import com.example.demo.entity.InvitadoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitadoPersonaRepository extends JpaRepository<InvitadoPersona, Long> {
    List<InvitadoPersona> findByInvitadoIdOrderByOrdenAsc(Long invitadoId);
    void deleteByInvitadoId(Long invitadoId);
}
