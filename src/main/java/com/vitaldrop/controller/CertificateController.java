package com.vitaldrop.controller;

import com.vitaldrop.entity.Donor;
import com.vitaldrop.entity.Request;
import com.vitaldrop.service.CertificateService;
import com.vitaldrop.service.DonorService;
import com.vitaldrop.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/certificate")
@CrossOrigin("*")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private DonorService donorService;

    @GetMapping("/download/{requestId}")
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable Long requestId)
            throws Exception {

        Request request =
                requestService.getRequestById(requestId);

        Donor donor =
                donorService.getDonorById(
                        request.getDonorId()
                );

        if (!"COMPLETED".equals(request.getStatus())) {

            throw new RuntimeException(
                    "Certificate available only for completed donations"
            );
        }

        String donorName =
                donor.getFullName();

        String bloodGroup =
                donor.getBloodGroup();

        String donationDate =
                request.getRequestDate()
                        .toLocalDate()
                        .toString();

        String certificateNumber =
                "VD-2026-" +
                        String.format(
                                "%06d",
                                request.getRequestId()
                        );

        byte[] pdf =
                certificateService.generateCertificate(
                        donorName,
                        bloodGroup,
                        donationDate,
                        certificateNumber
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=certificate.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}