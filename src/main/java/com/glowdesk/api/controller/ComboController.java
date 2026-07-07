package com.glowdesk.api.controller;

import com.glowdesk.api.dto.request.CreateComboRequest;
import com.glowdesk.api.dto.request.UpdateComboRequest;
import com.glowdesk.api.dto.response.ComboResponse;
import com.glowdesk.api.service.ComboService;
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

@Tag(name = "Combo Module")
@RestController
@RequestMapping("/api/v1/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    @Operation(summary = "Get all active combos for a branch [Public]")
    @GetMapping
    public List<ComboResponse> getAll(
            @Parameter(description = "Branch ID", example = "57929888-e65f-4dce-a0b1-bebd04e3594e")
            @RequestParam UUID branchId) {
        return comboService.getAllActive(branchId);
    }

    @Operation(summary = "Create a new combo package [Admin]")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ComboResponse create(@Valid @RequestBody CreateComboRequest request) {
        return comboService.create(request);
    }

    @Operation(summary = "Update an existing combo [Admin]")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ComboResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateComboRequest request) {
        return comboService.update(id, request);
    }

    @Operation(summary = "Soft delete a combo [Admin]")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        comboService.delete(id);
    }
}
