package com.travel.system.controller;

import com.travel.system.dto.FacilityQueryResult;
import com.travel.system.model.Facility;
import com.travel.system.repository.FacilityRepository;
import com.travel.system.service.FacilitySearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
public class FacilityController {
    private final FacilityRepository facilityRepository;
    private final FacilitySearchService facilitySearchService;

    public FacilityController(FacilityRepository facilityRepository,
                              FacilitySearchService facilitySearchService) {
        this.facilityRepository = facilityRepository;
        this.facilitySearchService = facilitySearchService;
    }

    @GetMapping
    public List<Facility> list(@RequestParam(required = false) String type) {
        if (type == null || type.isBlank()) {
            return facilityRepository.findAll();
        }
        return facilityRepository.findByFacilityTypeContainingIgnoreCase(type);
    }

    @GetMapping("/nearby")
    public List<FacilityQueryResult> nearby(@RequestParam Long fromNodeId,
                                            @RequestParam(required = false) String type,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Double maxDistanceMeters,
                                            @RequestParam(defaultValue = "walk") String transport) {
        return facilitySearchService.searchNearby(fromNodeId, type, keyword, maxDistanceMeters, transport);
    }
}
