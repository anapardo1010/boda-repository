package com.example.demo.repository;

import com.example.demo.entity.InvitadoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitadoPersonaRepository extends JpaRepository<InvitadoPersona, Long> {
    List<InvitadoPersona> findByInvitadoIdOrderByOrdenAsc(Long invitadoId);
    void deleteByInvitadoId(Long invitadoId);
    void deleteByInvitadoIdAndEsAdicional(Long invitadoId, Boolean esAdicional);
    
    @Query("SELECT COALESCE(MAX(p.orden), 0) FROM InvitadoPersona p WHERE p.invitado.id = ?1")
    int findMaxOrdenByInvitadoId(Long invitadoId);
}
