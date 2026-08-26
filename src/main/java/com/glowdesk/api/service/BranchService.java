package com.glowdesk.api.service;

import com.glowdesk.api.dto.response.BranchResponse;
import com.glowdesk.api.entity.Branch;
import com.glowdesk.api.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    public List<String> getCities() {
        return branchRepository.findDistinctActiveCities();
    }

    public List<BranchResponse> getBranchesByCity(String city) {
        return branchRepository.findByCityAndIsActiveTrue(city)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private BranchResponse toResponse(Branch b) {
        return new BranchResponse(
                b.getId(),
                b.getName(),
                b.getAddress(),
                b.getCity(),
                b.getState(),
                b.getPhone(),
                b.getOpeningTime(),
                b.getClosingTime());
    }
}
