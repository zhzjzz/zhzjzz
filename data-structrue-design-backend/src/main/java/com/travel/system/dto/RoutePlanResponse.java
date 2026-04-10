package com.travel.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RoutePlanResponse {
    private List<Long> pathNodeIds;
    private List<Long> visitOrderNodeIds;
    private Double totalDistanceMeters;
    private Double totalTravelMinutes;
}
