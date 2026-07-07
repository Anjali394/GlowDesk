package com.glowdesk.api.service;

import com.glowdesk.api.dto.request.CreateComboRequest;
import com.glowdesk.api.dto.request.UpdateComboRequest;
import com.glowdesk.api.dto.response.ComboResponse;
import com.glowdesk.api.dto.response.ServiceResponse;
import com.glowdesk.api.entity.Branch;
import com.glowdesk.api.entity.Combo;
import com.glowdesk.api.entity.Service;
import com.glowdesk.api.exception.ResourceNotFoundException;
import com.glowdesk.api.repository.BranchRepository;
import com.glowdesk.api.repository.ComboRepository;
import com.glowdesk.api.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ComboService {

    private final ComboRepository comboRepository;
    private final BranchRepository branchRepository;
    private final ServiceRepository serviceRepository;

    public List<ComboResponse> getAllActive(UUID branchId) {
        return comboRepository.findByBranchIdAndIsActiveTrue(branchId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ComboResponse create(CreateComboRequest request) {
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + request.branchId()));

        Set<Service> services = resolveServices(request.serviceIds());

        Combo combo = Combo.builder()
                .branch(branch)
                .name(request.name())
                .description(request.description())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .services(services)
                .build();

        return toResponse(comboRepository.save(combo));
    }

    @Transactional
    public ComboResponse update(UUID id, UpdateComboRequest request) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Combo not found: " + id));

        if (request.name() != null) combo.setName(request.name());
        if (request.description() != null) combo.setDescription(request.description());
        if (request.discountType() != null) combo.setDiscountType(request.discountType());
        if (request.discountValue() != null) combo.setDiscountValue(request.discountValue());
        if (request.serviceIds() != null) combo.setServices(resolveServices(request.serviceIds()));

        return toResponse(comboRepository.save(combo));
    }

    @Transactional
    public void delete(UUID id) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Combo not found: " + id));

        combo.setActive(false);
        comboRepository.save(combo);
    }

    private Set<Service> resolveServices(Set<UUID> serviceIds) {
        Set<Service> services = Set.copyOf(serviceRepository.findAllById(serviceIds));
        if (services.size() != serviceIds.size()) {
            throw new ResourceNotFoundException("One or more service IDs not found");
        }
        return services;
    }

    private ServiceResponse toServiceResponse(Service s) {
        return new ServiceResponse(s.getId(), s.getName(), s.getDescription(),
                s.getDuration(), s.getPrice(), s.isActive());
    }

    private ComboResponse toResponse(Combo c) {
        Set<ServiceResponse> serviceResponses = c.getServices().stream()
                .map(this::toServiceResponse)
                .collect(Collectors.toSet());
        return new ComboResponse(c.getId(), c.getName(), c.getDescription(),
                c.getDiscountType(), c.getDiscountValue(), serviceResponses, c.isActive());
    }
}
