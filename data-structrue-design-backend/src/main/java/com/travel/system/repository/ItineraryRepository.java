package com.travel.system.repository;

import com.travel.system.model.Itinerary;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * MyBatis Repository（Mapper）用于操作 {@link Itinerary} 实体。
 *
 * <p>本接口提供行程记录的基本 CRUD 方法，后续可扩展按用户查询、协作编辑等高级功能。
 *
 * @author 自动生成
 */
@Mapper
public interface ItineraryRepository {

    /**
     * 查询全部行程记录。
     *
     * @return {@link Itinerary} 列表
     */
    List<Itinerary> findAll();

    /**
     * 保存行程（新增或更新）。
     *
     * @param itinerary 行程实体
     * @return 保存后的实体
     */
    default Itinerary save(Itinerary itinerary) {
        if (itinerary.getId() == null) {
            insert(itinerary);
        } else {
            update(itinerary);
        }
        return itinerary;
    }

    /**
     * 根据 ID 查询单条行程。
     *
     * @param id 主键
     * @return 对应的 {@link Itinerary}，若不存在返回 {@code null}
     */
    Itinerary findById(Long id);

    /**
     * 插入新行程记录。
     *
     * @param itinerary 行程实体
     */
    void insert(Itinerary itinerary);

    /**
     * 更新已有行程记录。
     *
     * @param itinerary 行程实体
     */
    void update(Itinerary itinerary);
}
