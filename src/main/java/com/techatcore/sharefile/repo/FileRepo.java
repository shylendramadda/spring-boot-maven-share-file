package com.techatcore.sharefile.repo;

import com.techatcore.sharefile.domain.FileEntity;
import com.techatcore.sharefile.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Shylendra Madda
 */
public interface FileRepo extends JpaRepository<FileEntity, String> {
    Optional<FileEntity> findById(String id);

    List<FileEntity> findByFrom(User user);

    List<FileEntity> findByTo(User user);
}
