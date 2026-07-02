package com.vitaldrop.repository;

import com.vitaldrop.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RecipientRepository
        extends JpaRepository<Recipient, Long> {

    Recipient findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Recipient r SET r.password = :password WHERE r.email = :email")
    void updatePassword(String email, String password);
    // Add this method inside your RecipientRepository interface
    List<Recipient> findByFullNameContainingIgnoreCase(String fullName);
}