package com.travel.system.repository;

import com.travel.system.search.FoodDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch Repository for {@link FoodDocument}.
 * 使用 match query 走 IK 分词，而非 wildcard (Containing) 查询。
 */
@Repository
public interface FoodSearchRepository extends ElasticsearchRepository<FoodDocument, String> {
    /**
     * 根据名称、菜系或店名进行全文搜索（IK 分词匹配）
     */
    List<FoodDocument> findByNameOrCuisineOrStoreName(String name, String cuisine, String storeName);
}
