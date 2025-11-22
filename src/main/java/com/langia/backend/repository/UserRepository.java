package com.langia.backend.repository;

import com.langia.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by email.
     * Used for login and validation.
     *
     * @param email user's email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if an email already exists in the database.
     * Used to validate before registration.
     *
     * @param email email to be checked
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by CPF.
     *
     * @param cpf user's CPF
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.cpfString = :cpf")
    Optional<User> findByCPF(@Param("cpf") String cpf);

    /**
     * Checks if a CPF already exists in the database.
     * Used to validate before registration.
     *
     * @param cpf CPF to be checked
     * @return true if CPF exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.cpfString = :cpf")
    boolean existsByCpf(@Param("cpf") String cpf);

    /**
     * Checks if a phone number already exists in the database.
     * Used to validate before registration.
     *
     * @param phone phone number to be checked
     * @return true if phone exists, false otherwise
     */
    boolean existsByPhone(String phone);
}
