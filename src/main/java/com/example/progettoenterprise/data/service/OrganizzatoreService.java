package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.dto.OrganizzatoreDTO;

public interface OrganizzatoreService {
    OrganizzatoreDTO getProfilo(Long id);
    OrganizzatoreDTO updateProfilo(Long id, OrganizzatoreDTO dto);
}
