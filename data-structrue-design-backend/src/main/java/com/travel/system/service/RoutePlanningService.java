package com.travel.system.service;

import com.travel.system.dto.RoutePlanResponse;
import com.travel.system.model.RoadEdge;
import com.travel.system.repository.RoadEdgeRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoutePlanningService {

    private final RoadEdgeRepository roadEdgeRepository;

    public RoutePlanningService(RoadEdgeRepository roadEdgeRepository) {
        this.roadEdgeRepository = roadEdgeRepository;
    }

    public RoutePlanResponse shortestPath(Long fromId, Long toId, String strategy, String transport) {
        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Long> prev = new HashMap<>();
        runShortestPath(fromId, strategy, transport, dist, prev);

        List<Long> path = buildPath(prev, fromId, toId);
        double totalDistance = 0;
        double totalTimeMinutes = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            Long s = path.get(i);
            Long t = path.get(i + 1);
            RoadEdge edge = roadEdgeRepository.findByFromNodeId(s).stream()
                    .filter(e -> e.getToNode().getId().equals(t))
                    .findFirst()
                    .orElse(null);
            if (edge != null) {
                totalDistance += safe(edge.getDistanceMeters());
                totalTimeMinutes += travelTime(edge);
            }
        }

        return new RoutePlanResponse(path, totalDistance, totalTimeMinutes);
    }

    public Map<Long, Double> shortestDistanceMap(Long fromId, String transport) {
        Map<Long, Double> dist = new HashMap<>();
        runShortestPath(fromId, "distance", transport, dist, new HashMap<>());
        return dist;
    }

    private void runShortestPath(Long fromId,
                                 String strategy,
                                 String transport,
                                 Map<Long, Double> dist,
                                 Map<Long, Long> prev) {
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::distance));
        dist.put(fromId, 0.0);
        pq.offer(new NodeDistance(fromId, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            if (current.distance() > dist.getOrDefault(current.nodeId(), Double.MAX_VALUE)) {
                continue;
            }

            for (RoadEdge edge : roadEdgeRepository.findByFromNodeId(current.nodeId())) {
                if (!allow(edge.getAllowedTransport(), transport)) {
                    continue;
                }
                long next = edge.getToNode().getId();
                double weight = "time".equalsIgnoreCase(strategy) ? travelTime(edge) : safe(edge.getDistanceMeters());
                double candidate = current.distance() + weight;
                if (candidate < dist.getOrDefault(next, Double.MAX_VALUE)) {
                    dist.put(next, candidate);
                    prev.put(next, current.nodeId());
                    pq.offer(new NodeDistance(next, candidate));
                }
            }
        }
    }

    private List<Long> buildPath(Map<Long, Long> prev, Long fromId, Long toId) {
        LinkedList<Long> path = new LinkedList<>();
        Long cur = toId;
        while (cur != null) {
            path.addFirst(cur);
            if (cur.equals(fromId)) {
                return path;
            }
            cur = prev.get(cur);
        }
        return List.of();
    }

    private boolean allow(String allowedTransport, String currentTransport) {
        if (allowedTransport == null || currentTransport == null) {
            return true;
        }
        return Arrays.stream(allowedTransport.split(","))
                .map(String::trim)
                .anyMatch(v -> v.equalsIgnoreCase(currentTransport));
    }

    private double travelTime(RoadEdge edge) {
        double speed = safe(edge.getIdealSpeed()) * Math.max(0.1, safe(edge.getCongestion()));
        return speed <= 0 ? Double.MAX_VALUE : safe(edge.getDistanceMeters()) / speed;
    }

    private double safe(Double v) {
        return v == null ? 0.0 : v;
    }

    private record NodeDistance(Long nodeId, double distance) {
    }
}
