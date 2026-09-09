package com.estudos.repositorio;

import com.estudos.entidade.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByEmailAndSenha(String email, String senha);
    boolean existsByEmail(String email);
}