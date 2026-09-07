package com.jamil.ahadith.features.user.repository;

import com.jamil.ahadith.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleSubject(String googleSubject);

    @Query(value = "select * from users where email = :email for update", nativeQuery = true)
    Optional<User> findByEmailForUpdate(@Param("email") String email);

    @Query(value = "select * from users where google_subject = :googleSubject for update", nativeQuery = true)
    Optional<User> findByGoogleSubjectForUpdate(@Param("googleSubject") String googleSubject);

    @Query(value = "select * from users where id = :id for update", nativeQuery = true)
    Optional<User> findByIdForUpdate(@Param("id") UUID id);
}
