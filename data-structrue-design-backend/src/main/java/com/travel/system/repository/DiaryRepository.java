package com.travel.system.repository;

import com.travel.system.model.Diary;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis Repository（Mapper）用于操作 {@link Diary} 实体。
 *
 * <p>本接口提供最基础的 CRUD 方法；全文检索在 {@link com.travel.system.repository.DiarySearchRepository}
 * 中实现，已使用 Elasticsearch（若未配置则回落到 JPA）。
 *
 * @author 自动生成
 */
@Mapper
public interface DiaryRepository {

    /**
     * 查询全部日记。
     *
     * @return {@link Diary} 列表
     */
    /**
     * 查询全部日记。
     *
     * @return {@link Diary} 列表
     */
    List<Diary> findAll();

    /**
     * 按标题进行不区分大小写的模糊匹配。
     *
     * @param title 标题关键字
     * @return 匹配的 {@link Diary} 列表
     */
    List<Diary> findByTitleContainingIgnoreCase(@Param("title") String title);

    /**
     * 按标题或内容进行不区分大小写的模糊匹配。
     *
     * @param keyword 关键字
     * @return 匹配的 {@link Diary} 列表
     */
    List<Diary> findByTitleOrContentContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * 根据 ID 查询单条日记。
     *
     * @param id 主键
     * @return 对应的 {@link Diary}，若不存在返回 {@code null}
     */
    Diary findById(Long id);

    /**
     * 插入新日记记录。
     *
     * @param diary 日记实体
     */
    void insert(Diary diary);

    /**
     * 更新已有日记记录。
     *
     * @param diary 日记实体
     */
    void update(Diary diary);
    /**
     * 保存日记（新增或更新）。
     *
     * @param diary 日记实体
     * @return 保存后的实体
     */
    default Diary save(Diary diary) {
        if (diary.getId() == null) {
            insert(diary);
        } else {
            update(diary);
        }
        return diary;
    }
}
