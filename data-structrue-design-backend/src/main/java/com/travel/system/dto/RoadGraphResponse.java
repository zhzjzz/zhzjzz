package com.travel.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RoadGraphResponse {
    private List<RoadGraphNode> nodes;
    private List<RoadGraphEdge> edges;

    @Data
    @AllArgsConstructor
    public static class RoadGraphNode {
        private Long id;
        private String name;
        private String nodeType;
        private Double latitude;
        private Double longitude;
    }

    @Data
    @AllArgsConstructor
    public static class RoadGraphEdge {
        private Long id;
        private Long fromNodeId;
        private Long toNodeId;
        private Double distanceMeters;
        private String allowedTransport;
    }
}
