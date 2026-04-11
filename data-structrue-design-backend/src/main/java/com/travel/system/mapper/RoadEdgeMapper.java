package com.travel.system.mapper;

import com.travel.system.model.RoadEdge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis Mapper 接口，用于操作 {@link RoadEdge} 实体。
 *
 * <p>复杂的联表查询 SQL 已迁移至 XML 映射文件 {@code resources/mapper/RoadEdgeMapper.xml}，
 * 保持 Java 接口简洁清晰。
 *
 * <p>主要功能：
 *
 * <ul>
 *   <li>查询全部道路边（联表起点、终点节点）；</li>
 *   <li>按起点节点查询出边；</li>
 *   <li>插入、更新道路边记录。</li>
 * </ul>
 *
 * @author 自动生成
 */
@Mapper
public interface RoadEdgeMapper {

    /**
     * 查询全部道路边，并左联起点和终点节点获取完整信息。
     *
     * @return {@link RoadEdge} 列表（含关联的 fromNode 与 toNode）
     */
    List<RoadEdge> findAll();

    /**
     * 根据起点节点 ID 查询所有出边。
     *
     * @param fromNodeId 起点节点 ID
     * @return 从该节点出发的所有 {@link RoadEdge}
     */
    List<RoadEdge> findByFromNodeId(@Param("fromNodeId") Long fromNodeId);

    /**
     * 插入新道路边记录。
     *
     * @param roadEdge 道路边实体
     */
    void insert(RoadEdge roadEdge);

    /**
     * 更新已有道路边记录。
     *
     * @param roadEdge 道路边实体
     */
    void update(RoadEdge roadEdge);

    /**
     * 保存道路边（新增或更新）。
     *
     * @param roadEdge 道路边实体
     * @return 保存后的实体
     */
    default RoadEdge save(RoadEdge roadEdge) {
        if (roadEdge.getId() == null) {
            insert(roadEdge);
        } else {
            update(roadEdge);
        }
        return roadEdge;
    }
}
