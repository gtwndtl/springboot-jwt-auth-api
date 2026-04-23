package com.gtwndtl.demo.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gtwndtl.demo.models.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Integer> {
    // For Complex Queries, we can use @Query annotation
    // @Query("SELECT u FROM UserModel u WHERE u.email = :email")
    // Optional<UserModel> findByEmail(@Param("email") String email);

    //For Simple Queries, we can use Spring Data JPA's method naming convention
    Optional<UserModel> findByEmail(String email);

}
