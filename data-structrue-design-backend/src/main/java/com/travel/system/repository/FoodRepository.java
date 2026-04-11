package com.travel.system.repository;

import com.travel.system.model.Food;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 替代原来的 {@link FoodRepository}，已迁移至 MyBatis {@link com.travel.system.mapper.FoodMapper}。
 *
 * <p>此接口仅保留业务层需要的方法签名，实际实现全部委托给 {@link com.travel.system.mapper.FoodMapper}，对应的
 * SQL 已抽离至 {@code resources/mapper/FoodMapper.xml}。
 *
 * @author 自动生成
 */
@Mapper
public interface FoodRepository {

    /**
     * 查询全部美食（含关联目的地）。
     *
     * @return {@link Food} 列表
     */
    List<Food> findAll();

    /**
     * 按名称、菜系或店名进行模糊搜索（不区分大小写）。
     * 方法名已简化，映射到 FoodMapper.findByKeyword。
     *
     * @param keyword 关键字
     * @return 匹配的 {@link Food} 列表
     */
    List<Food> findByKeyword(String keyword);

    /**
     * 插入新美食记录。
     *
     * @param food 美食实体
     */
    void insert(Food food);

    /**
     * 更新已有美食记录。
     *
     * @param food 美食实体
     */
    void update(Food food);

    /**
     * 保存美食（新增或更新）。
     *
     * @param food 美食实体
     * @return 保存后的实体
     */
    default Food save(Food food) {
        if (food.getId() == null) {
            insert(food);
        } else {
            update(food);
        }
        return food;
    }
}
