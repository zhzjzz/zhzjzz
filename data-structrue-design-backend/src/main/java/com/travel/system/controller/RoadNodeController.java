package com.travel.system.controller;

import com.travel.system.model.RoadNode;
import com.travel.system.repository.RoadNodeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/road-nodes")
public class RoadNodeController {
    private final RoadNodeRepository roadNodeRepository;

    public RoadNodeController(RoadNodeRepository roadNodeRepository) {
        this.roadNodeRepository = roadNodeRepository;
    }

    @GetMapping
    public List<RoadNode> list() {
        return roadNodeRepository.findAll();
    }
}
