package com.fitlink.service;

import com.fitlink.web.dto.AIPrescriptionRequestDTO;
import com.fitlink.web.dto.AIPrescriptionResponseDTO;

public interface AIPrescriptionService {
    AIPrescriptionResponseDTO getPrescription(AIPrescriptionRequestDTO request);
}

