package com.travel.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoutePlanRequest {
    private Long fromNodeId;
    private Long toNodeId;
    private List<Long> targetNodeIds;
    private String strategy;
    private String transport;
}
