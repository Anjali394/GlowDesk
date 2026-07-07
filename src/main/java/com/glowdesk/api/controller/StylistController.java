package com.glowdesk.api.controller;

import com.glowdesk.api.dto.request.CreateStylistRequest;
import com.glowdesk.api.dto.request.UpdateStylistRequest;
import com.glowdesk.api.dto.response.StylistResponse;
import com.glowdesk.api.service.StylistService;
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

@Tag(name = "Stylist Module")
@RestController
@RequestMapping("/api/v1/stylists")
@RequiredArgsConstructor
public class StylistController {

    private final StylistService stylistService;

    @Operation(summary = "Get all active stylists for a branch [Public]")
    @GetMapping
    public List<StylistResponse> getAll(
            @Parameter(description = "Branch ID", example = "57929888-e65f-4dce-a0b1-bebd04e3594e")
            @RequestParam UUID branchId) {
        return stylistService.getAllActive(branchId);
    }

    @Operation(summary = "Add a new stylist [Admin]")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public StylistResponse create(@Valid @RequestBody CreateStylistRequest request) {
        return stylistService.create(request);
    }

    @Operation(summary = "Update stylist details [Admin]")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StylistResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateStylistRequest request) {
        return stylistService.update(id, request);
    }

    @Operation(summary = "Soft delete a stylist [Admin]")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        stylistService.delete(id);
    }
}
