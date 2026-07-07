package com.glowdesk.api.service;

import com.glowdesk.api.dto.request.CreateServiceRequest;
import com.glowdesk.api.dto.request.UpdateServiceRequest;
import com.glowdesk.api.dto.response.ServiceResponse;
import com.glowdesk.api.entity.Branch;
import com.glowdesk.api.entity.Service;
import com.glowdesk.api.exception.ResourceNotFoundException;
import com.glowdesk.api.repository.BranchRepository;
import com.glowdesk.api.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final BranchRepository branchRepository;

    public List<ServiceResponse> getAllActive(UUID branchId) {
        return serviceRepository.findByBranchIdAndIsActiveTrue(branchId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ServiceResponse create(CreateServiceRequest request) {
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + request.branchId()));

        Service service = Service.builder()
                .branch(branch)
                .name(request.name())
                .description(request.description())
                .duration(request.duration())
                .price(request.price())
                .build();

        return toResponse(serviceRepository.save(service));
    }

    @Transactional
    public ServiceResponse update(UUID id, UpdateServiceRequest request) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));

        if (request.name() != null) service.setName(request.name());
        if (request.description() != null) service.setDescription(request.description());
        if (request.duration() != null) service.setDuration(request.duration());
        if (request.price() != null) service.setPrice(request.price());

        return toResponse(serviceRepository.save(service));
    }

    @Transactional
    public void delete(UUID id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));

        service.setActive(false);
        serviceRepository.save(service);
    }

    private ServiceResponse toResponse(Service s) {
        return new ServiceResponse(
                s.getId(),
                s.getName(),
                s.getDescription(),
                s.getDuration(),
                s.getPrice(),
                s.isActive()
        );
    }
}
