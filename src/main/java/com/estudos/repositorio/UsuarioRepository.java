package com.estudos.repositorio;

import com.estudos.entidade.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UsuarioEntity> findByEmailAndSenha(String email, String senha);
    List<UsuarioEntity> findByTipoPerfilOrderByNomeAsc(String tipoPerfil);
}