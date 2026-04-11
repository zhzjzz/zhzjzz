package com.travel.system.repository;

import com.travel.system.model.RoadNode;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * MyBatis Repository（Mapper）用于操作 {@link RoadNode} 实体。
 *
 * <p>提供道路节点的基本查询、单点获取、插入与更新方法，后续可在此基础上实现邻近搜索或路径计算等高级功能。
 *
 * @author 自动生成
 */
@Mapper
public interface RoadNodeRepository {

    /**
     * 查询全部道路节点。
     *
     * @return {@link RoadNode} 列表
     */
    List<RoadNode> findAll();

    /**
     * 保存道路节点（新增或更新）。
     *
     * @param roadNode 道路节点实体
     * @return 保存后的实体
     */
    default RoadNode save(RoadNode roadNode) {
        if (roadNode.getId() == null) {
            insert(roadNode);
        } else {
            update(roadNode);
        }
        return roadNode;
    }

    /**
     * 根据 ID 查询单个道路节点。
     *
     * @param id 主键
     * @return 对应的 {@link RoadNode}，若不存在返回 {@code null}
     */
    RoadNode findById(Long id);

    /**
     * 插入新道路节点。
     *
     * @param roadNode 道路节点实体
     */
    void insert(RoadNode roadNode);

    /**
     * 更新已有道路节点。
     *
     * @param roadNode 道路节点实体
     */
    void update(RoadNode roadNode);
}
