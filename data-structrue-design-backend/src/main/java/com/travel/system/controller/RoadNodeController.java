package com.travel.system.controller;

import com.travel.system.dto.RoadGraphResponse;
import com.travel.system.model.RoadEdge;
import com.travel.system.model.RoadNode;
import com.travel.system.repository.RoadEdgeRepository;
import com.travel.system.repository.RoadNodeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/road-nodes")
public class RoadNodeController {
    private final RoadNodeRepository roadNodeRepository;
    private final RoadEdgeRepository roadEdgeRepository;

    public RoadNodeController(RoadNodeRepository roadNodeRepository,
                              RoadEdgeRepository roadEdgeRepository) {
        this.roadNodeRepository = roadNodeRepository;
        this.roadEdgeRepository = roadEdgeRepository;
    }

    @GetMapping
    public List<RoadNode> list() {
        return roadNodeRepository.findAll();
    }

    @GetMapping("/graph")
    public RoadGraphResponse graph() {
        List<RoadGraphResponse.RoadGraphNode> nodes = roadNodeRepository.findAll().stream()
                .map(node -> new RoadGraphResponse.RoadGraphNode(
                        node.getId(),
                        node.getName(),
                        node.getNodeType(),
                        node.getLatitude(),
                        node.getLongitude()
                ))
                .toList();

        List<RoadGraphResponse.RoadGraphEdge> edges = roadEdgeRepository.findAll().stream()
                .map(this::toEdge)
                .toList();

        return new RoadGraphResponse(nodes, edges);
    }

    private RoadGraphResponse.RoadGraphEdge toEdge(RoadEdge edge) {
        return new RoadGraphResponse.RoadGraphEdge(
                edge.getId(),
                edge.getFromNode().getId(),
                edge.getToNode().getId(),
                edge.getDistanceMeters(),
                edge.getAllowedTransport()
        );
    }
}
