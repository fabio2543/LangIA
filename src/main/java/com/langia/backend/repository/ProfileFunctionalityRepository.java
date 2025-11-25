package com.langia.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Functionality;
import com.langia.backend.model.Profile;
import com.langia.backend.model.ProfileFunctionality;

/**
 * Repository para gerenciar associações entre perfis e funcionalidades.
 */
@Repository
public interface ProfileFunctionalityRepository extends JpaRepository<ProfileFunctionality, Long> {

    /**
     * Busca todas as funcionalidades de um perfil.
     *
     * @param profile perfil
     * @return lista de associações
     */
    List<ProfileFunctionality> findByProfile(Profile profile);

    /**
     * Busca todos os perfis que têm uma funcionalidade.
     *
     * @param functionality funcionalidade
     * @return lista de associações
     */
    List<ProfileFunctionality> findByFunctionality(Functionality functionality);

    /**
     * Busca funcionalidades de um perfil por herança.
     *
     * @param profile perfil
     * @param grantedByInheritance se foi concedida por herança
     * @return lista de associações
     */
    List<ProfileFunctionality> findByProfileAndGrantedByInheritance(
            Profile profile, Boolean grantedByInheritance);

    /**
     * Busca associação específica entre perfil e funcionalidade.
     *
     * @param profile perfil
     * @param functionality funcionalidade
     * @return lista de associações (deve ser única)
     */
    List<ProfileFunctionality> findByProfileAndFunctionality(
            Profile profile, Functionality functionality);

    /**
     * Busca códigos de funcionalidades de um perfil (query otimizada).
     *
     * @param profileId ID do perfil
     * @return lista de códigos de funcionalidades
     */
    @Query("SELECT f.code FROM ProfileFunctionality pf " +
           "JOIN pf.functionality f " +
           "WHERE pf.profile.id = :profileId")
    List<String> findFunctionalityCodesByProfileId(@Param("profileId") Long profileId);

    /**
     * Conta funcionalidades de um perfil.
     *
     * @param profile perfil
     * @return quantidade de funcionalidades
     */
    Long countByProfile(Profile profile);

    /**
     * Conta funcionalidades herdadas de um perfil.
     *
     * @param profile perfil
     * @param grantedByInheritance se foi concedida por herança
     * @return quantidade de funcionalidades
     */
    Long countByProfileAndGrantedByInheritance(Profile profile, Boolean grantedByInheritance);

    /**
     * Verifica se um perfil tem uma funcionalidade específica.
     *
     * @param profile perfil
     * @param functionality funcionalidade
     * @return true se o perfil tem a funcionalidade
     */
    boolean existsByProfileAndFunctionality(Profile profile, Functionality functionality);

    /**
     * Deleta todas as associações de um perfil.
     *
     * @param profile perfil
     */
    void deleteByProfile(Profile profile);

    /**
     * Deleta todas as associações de uma funcionalidade.
     *
     * @param functionality funcionalidade
     */
    void deleteByFunctionality(Functionality functionality);
}
