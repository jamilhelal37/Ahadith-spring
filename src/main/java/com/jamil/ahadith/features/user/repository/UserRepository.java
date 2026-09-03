package com.jamil.ahadith.features.user.repository;

import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleSubject(String googleSubject);

    @Query("""
            select u from User u
            where (:q is null
                   or lower(u.name) like lower(concat('%', :q, '%'))
                   or lower(u.email) like lower(concat('%', :q, '%')))
              and (:status is null or u.status = :status)
              and (:type is null or u.type = :type)
            """)
    Page<User> searchAdminUsers(@Param("q") String q,
                                @Param("status") UserStatus status,
                                @Param("type") UserType type,
                                Pageable pageable);

    @Query(value = "select * from users where email = :email for update", nativeQuery = true)
    Optional<User> findByEmailForUpdate(@Param("email") String email);

    @Query(value = "select * from users where google_subject = :googleSubject for update", nativeQuery = true)
    Optional<User> findByGoogleSubjectForUpdate(@Param("googleSubject") String googleSubject);

    @Query(value = "select * from users where id = :id for update", nativeQuery = true)
    Optional<User> findByIdForUpdate(@Param("id") UUID id);
}
