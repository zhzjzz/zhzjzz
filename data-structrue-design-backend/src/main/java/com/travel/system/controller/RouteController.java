package com.travel.system.controller;

import com.travel.system.dto.RoutePlanRequest;
import com.travel.system.dto.RoutePlanResponse;
import com.travel.system.service.RoutePlanningService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * 路线规划控制器。
 * 对外提供统一路径规划入口，支持不同策略和交通工具参数。
 */
@RestController
@RequestMapping("/api/routes")
public class RouteController {
    private final RoutePlanningService routePlanningService;

    public RouteController(RoutePlanningService routePlanningService) {
        this.routePlanningService = routePlanningService;
    }

    /**
     * 单次路线规划接口。
     * 若前端未传策略/交通工具，将自动回落到默认值，避免空参数导致异常。
     */
    @PostMapping("/plan")
    public RoutePlanResponse plan(@RequestBody RoutePlanRequest request) {
        String strategy = request.getStrategy() == null ? "distance" : request.getStrategy();
        String transport = request.getTransport() == null ? "walk" : request.getTransport();
        List<Long> targetNodeIds = request.getTargetNodeIds();
        if (targetNodeIds != null && !targetNodeIds.isEmpty()) {
            if (request.getFromNodeId() == null) {
                throw new ResponseStatusException(BAD_REQUEST, "多目标规划必须提供起点节点");
            }
            return routePlanningService.multiTargetPath(request.getFromNodeId(), targetNodeIds, strategy, transport);
        }
        if (request.getFromNodeId() == null || request.getToNodeId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "单目标规划必须提供起点和终点节点");
        }
        return routePlanningService.shortestPath(request.getFromNodeId(), request.getToNodeId(), strategy, transport);
    }
}
