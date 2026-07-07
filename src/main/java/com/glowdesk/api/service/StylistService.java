package com.glowdesk.api.service;

import com.glowdesk.api.dto.request.CreateStylistRequest;
import com.glowdesk.api.dto.request.UpdateStylistRequest;
import com.glowdesk.api.dto.response.StylistResponse;
import com.glowdesk.api.entity.Branch;
import com.glowdesk.api.entity.Stylist;
import com.glowdesk.api.exception.ResourceNotFoundException;
import com.glowdesk.api.repository.BranchRepository;
import com.glowdesk.api.repository.StylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StylistService {

    private final StylistRepository stylistRepository;
    private final BranchRepository branchRepository;

    public List<StylistResponse> getAllActive(UUID branchId) {
        return stylistRepository.findByBranchIdAndIsActiveTrue(branchId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StylistResponse create(CreateStylistRequest request) {
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + request.branchId()));

        Stylist stylist = Stylist.builder()
                .branch(branch)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .experience(request.experience())
                .build();

        return toResponse(stylistRepository.save(stylist));
    }

    @Transactional
    public StylistResponse update(UUID id, UpdateStylistRequest request) {
        Stylist stylist = stylistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stylist not found: " + id));

        if (request.firstName() != null) stylist.setFirstName(request.firstName());
        if (request.lastName() != null) stylist.setLastName(request.lastName());
        if (request.experience() != null) stylist.setExperience(request.experience());

        return toResponse(stylistRepository.save(stylist));
    }

    @Transactional
    public void delete(UUID id) {
        Stylist stylist = stylistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stylist not found: " + id));

        stylist.setActive(false);
        stylistRepository.save(stylist);
    }

    private StylistResponse toResponse(Stylist s) {
        return new StylistResponse(
                s.getId(),
                s.getFirstName(),
                s.getLastName(),
                s.getExperience(),
                s.getRating(),
                s.isActive()
        );
    }
}
