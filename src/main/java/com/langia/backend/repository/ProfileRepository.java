package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Profile;
import com.langia.backend.model.UserProfile;

/**
 * Repository para gerenciar perfis de usuário.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /**
     * Busca perfil por código.
     *
     * @param code código do perfil (enum)
     * @return Optional com o perfil se encontrado
     */
    Optional<Profile> findByCode(UserProfile code);

    /**
     * Busca perfis por nível hierárquico.
     *
     * @param hierarchyLevel nível hierárquico
     * @return lista de perfis
     */
    List<Profile> findByHierarchyLevel(Integer hierarchyLevel);

    /**
     * Busca perfis ativos.
     *
     * @param active status de ativação
     * @return lista de perfis ativos/inativos
     */
    List<Profile> findByActive(Boolean active);

    /**
     * Busca perfis com nível hierárquico menor ou igual.
     *
     * @param hierarchyLevel nível hierárquico máximo
     * @return lista de perfis
     */
    List<Profile> findByHierarchyLevelLessThanEqual(Integer hierarchyLevel);

    /**
     * Busca perfis com nível hierárquico maior ou igual.
     *
     * @param hierarchyLevel nível hierárquico mínimo
     * @return lista de perfis
     */
    List<Profile> findByHierarchyLevelGreaterThanEqual(Integer hierarchyLevel);

    /**
     * Verifica se existe perfil com o código.
     *
     * @param code código do perfil
     * @return true se existe
     */
    boolean existsByCode(UserProfile code);
}
