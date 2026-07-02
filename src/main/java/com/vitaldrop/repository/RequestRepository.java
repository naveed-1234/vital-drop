package com.vitaldrop.repository;

import com.vitaldrop.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByDonorId(Long donorId);

    List<Request> findByRecipientId(Long recipientId);

    long countByStatus(String accepted);
    List<Request> findByDonorIdAndStatus(
            Long donorId,
            String status
    );

    List<Request> findByRecipientIdAndStatus(
            Long recipientId,
            String status
    );
}