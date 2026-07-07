package com.glowdesk.api.controller;

import com.glowdesk.api.dto.request.CreateServiceRequest;
import com.glowdesk.api.dto.request.UpdateServiceRequest;
import com.glowdesk.api.dto.response.ServiceResponse;
import com.glowdesk.api.service.ServiceCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Service Module")
@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    @Operation(summary = "Get all active services for a branch [Public]")
    @GetMapping
    public List<ServiceResponse> getAll(
            @Parameter(description = "Branch ID", example = "57929888-e65f-4dce-a0b1-bebd04e3594e")
            @RequestParam UUID branchId) {
        return serviceCatalogService.getAllActive(branchId);
    }

    @Operation(summary = "Create a new service [Admin]")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceResponse create(@Valid @RequestBody CreateServiceRequest request) {
        return serviceCatalogService.create(request);
    }

    @Operation(summary = "Update an existing service [Admin]")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateServiceRequest request) {
        return serviceCatalogService.update(id, request);
    }

    @Operation(summary = "Soft delete a service [Admin]")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        serviceCatalogService.delete(id);
    }
}
