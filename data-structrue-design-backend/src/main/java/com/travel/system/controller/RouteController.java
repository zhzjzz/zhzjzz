package com.travel.system.controller;

import com.travel.system.dto.RoutePlanRequest;
import com.travel.system.dto.RoutePlanResponse;
import com.travel.system.service.RoutePlanningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * {@code RouteController} 负责处理路线规划相关的 HTTP 请求。
 *
 * <p>提供统一的路径规划入口，支持：
 *
 * <ul>
 *   <li>单目标规划：从起点到终点的最短/最优路径；</li>
 *   <li>多目标规划：从起点出发，依次经过多个目标点的访问顺序优化。</li>
 * </ul>
 *
 * <p>规划策略（{@code strategy}）支持：
 *
 * <ul>
 *   <li>{@code distance}：按距离最短规划（默认）；</li>
 *   <li>{@code time}：按时间最短规划（根据交通方式速度计算）。</li>
 * </ul>
 *
 * <p>交通方式（{@code transport}）支持：
 *
 * <ul>
 *   <li>{@code walk}：步行（默认）；</li>
 *   <li>{@code bike}：骑行；</li>
 *   <li>{@code drive}：驾车。</li>
 * </ul>
 *
 * @author 自动生成
 */
@RestController
@RequestMapping("/api/routes")
@Tag(name = "路线规划", description = "路径规划、多目标路线优化等相关接口")
public class RouteController {

    /** 路径规划业务层服务，封装 Dijkstra 等算法的具体实现。 */
    private final RoutePlanningService routePlanningService;

    /**
     * 构造函数注入 {@link RoutePlanningService}。
     *
     * @param routePlanningService 路径规划服务
     */
    public RouteController(RoutePlanningService routePlanningService) {
        this.routePlanningService = routePlanningService;
    }

    /**
     * 单次路线规划接口。
     *
     * <p>根据请求参数自动判断规划模式：
     *
     * <ul>
     *   <li>若提供了 {@code targetNodeIds} 列表，执行多目标规划；</li>
     *   <li>否则执行单目标规划，需要同时提供 {@code fromNodeId} 与 {@code toNodeId}。</li>
     * </ul>
     *
     * <p>若前端未传递策略或交通方式，将自动使用默认值，避免空参数导致异常。
     *
     * @param request 路径规划请求体，包含起点、终点（或目标列表）、策略、交通方式等
     * @return 规划结果 {@link RoutePlanResponse}，包含路径节点列表与总距离/时间
     * @throws ResponseStatusException 若参数不满足规划前提条件，返回 400 错误
     */
    @Operation(summary = "路线规划", description = "支持单目标最短路径和多目标路线优化，支持距离/时间策略和多种交通方式")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "规划成功"),
        @ApiResponse(responseCode = "400", description = "请求参数不合法")
    })
    @PostMapping("/plan")
    public RoutePlanResponse plan(@RequestBody RoutePlanRequest request) {
        // 设置默认策略与交通方式，避免空指针
        String strategy = request.getStrategy() == null ? "distance" : request.getStrategy();
        String transport = request.getTransport() == null ? "walk" : request.getTransport();
        List<Long> targetNodeIds = request.getTargetNodeIds();

        // 多目标规划分支：依次经过多个目标点
        if (targetNodeIds != null && !targetNodeIds.isEmpty()) {
            if (request.getFromNodeId() == null) {
                throw new ResponseStatusException(BAD_REQUEST, "多目标规划必须提供起点节点");
            }
            return routePlanningService.multiTargetPath(
                    request.getFromNodeId(), targetNodeIds, strategy, transport);
        }

        // 单目标规划分支：起点到终点的最短路径
        if (request.getFromNodeId() == null || request.getToNodeId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "单目标规划必须提供起点和终点节点");
        }
        return routePlanningService.shortestPath(
                request.getFromNodeId(), request.getToNodeId(), strategy, transport);
    }
}
