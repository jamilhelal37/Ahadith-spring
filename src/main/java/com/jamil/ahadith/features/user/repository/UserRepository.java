package com.jamil.ahadith.features.user.repository;

import com.jamil.ahadith.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query(value = "select * from users where email = :email for update", nativeQuery = true)
    Optional<User> findByEmailForUpdate(@Param("email") String email);

    @Query(value = "select * from users where id = :id for update", nativeQuery = true)
    Optional<User> findByIdForUpdate(@Param("id") UUID id);
}
