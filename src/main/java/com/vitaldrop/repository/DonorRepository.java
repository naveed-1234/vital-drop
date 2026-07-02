package com.vitaldrop.repository;

import com.vitaldrop.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DonorRepository extends JpaRepository<Donor, Long> {


    Donor findByEmail(String email);

    List<Donor> findByBloodGroupAndAvailabilityTrue(
            String bloodGroup);

    List<Donor> findByBloodGroupAndCityAndAvailabilityTrue(
            String bloodGroup,
            String city
    );

    long countByAvailabilityTrue();

    @Modifying
    @Transactional
    @Query("UPDATE Donor d SET d.password = :password WHERE d.email = :email")
    void updatePassword(String email, String password);

    // Add this method inside your DonorRepository interface
    List<Donor> findByFullNameContainingIgnoreCase(String fullName);
}