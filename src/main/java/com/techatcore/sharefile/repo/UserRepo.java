package com.techatcore.sharefile.repo;

import com.techatcore.sharefile.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Shylendra Madda
 */
@Repository
public interface UserRepo extends JpaRepository<User, String> {
    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);
}
