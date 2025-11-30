package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Functionality;
import com.langia.backend.model.FunctionalityModule;

/**
 * Repository para gerenciar funcionalidades do sistema.
 */
@Repository
public interface FunctionalityRepository extends JpaRepository<Functionality, UUID> {

    /**
     * Busca funcionalidade por código.
     *
     * @param code código da funcionalidade
     * @return Optional com a funcionalidade se encontrada
     */
    Optional<Functionality> findByCode(String code);

    /**
     * Busca funcionalidades por módulo.
     *
     * @param module módulo das funcionalidades
     * @return lista de funcionalidades do módulo
     */
    List<Functionality> findByModule(FunctionalityModule module);

    /**
     * Busca funcionalidades ativas.
     *
     * @param active status de ativação
     * @return lista de funcionalidades ativas/inativas
     */
    List<Functionality> findByActive(Boolean active);

    /**
     * Busca funcionalidades ativas por módulo.
     *
     * @param module módulo das funcionalidades
     * @param active status de ativação
     * @return lista de funcionalidades do módulo
     */
    List<Functionality> findByModuleAndActive(FunctionalityModule module, Boolean active);

    /**
     * Busca funcionalidades por lista de códigos.
     *
     * @param codes lista de códigos
     * @return lista de funcionalidades
     */
    List<Functionality> findByCodeIn(List<String> codes);

    /**
     * Conta funcionalidades por módulo.
     *
     * @param module módulo das funcionalidades
     * @return quantidade de funcionalidades
     */
    Long countByModule(FunctionalityModule module);

    /**
     * Verifica se existe funcionalidade com o código.
     *
     * @param code código da funcionalidade
     * @return true se existe
     */
    boolean existsByCode(String code);
}
