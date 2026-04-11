package com.travel.system.repository;

import com.travel.system.model.Destination;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis Repository（Mapper）用于操作 {@link Destination} 实体。
 *
 * <p>实际实现全部委托给对应的 XML 映射文件 {@code resources/mapper/DestinationMapper.xml}。
 *
 * @author 自动生成
 */
@Mapper
public interface DestinationRepository {

    /**
     * 查询目的地记录总数。
     *
     * @return 记录数量
     */
    long count();

    /**
     * 查询全部目的地。
     *
     * @return {@link Destination} 列表
     */
    List<Destination> findAll();

    /**
     * 按名称或类别关键字进行模糊搜索（不区分大小写）。
     *
     * @param keyword 关键字
     * @return 匹配的 {@link Destination}
     */
    List<Destination> findByKeyword(@Param("keyword") String keyword);

    /**
     * 插入新目的地记录。
     *
     * @param destination 目的地实体
     * @return 受影响的行数
     */
    int insert(Destination destination);

    /**
     * 更新已有目的地记录。
     *
     * @param destination 目的地实体
     * @return 受影响的行数
     */
    int update(Destination destination);

    /**
     * 保存（插入或更新）目的地实体。
     *
     * @param destination 要保存的实体
     * @return 保存后的实体
     */
    default Destination save(Destination destination) {
        if (destination.getId() == null) {
            insert(destination);
        } else {
            update(destination);
        }
        return destination;
    }
}
