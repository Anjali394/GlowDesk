package com.glowdesk.api.controller;

import com.glowdesk.api.dto.response.BranchResponse;
import com.glowdesk.api.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Branch Module")
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @Operation(summary = "Get all cities that have an active branch [Public]")
    @GetMapping("/cities")
    public List<String> getCities() {
        return branchService.getCities();
    }

    @Operation(summary = "Get active branches in a city [Public]")
    @GetMapping
    public List<BranchResponse> getBranchesByCity(
            @RequestParam String city) {
        return branchService.getBranchesByCity(city);
    }
}
