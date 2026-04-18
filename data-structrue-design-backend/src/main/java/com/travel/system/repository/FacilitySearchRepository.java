package com.travel.system.repository;

import com.travel.system.search.FacilityDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch Repository for {@link FacilityDocument}.
 * 使用 match query 走 IK 分词，而非 wildcard (Containing) 查询。
 */
@Repository
public interface FacilitySearchRepository extends ElasticsearchRepository<FacilityDocument, String> {
    /**
     * 根据名称、设施类型或目的地名称进行全文搜索（IK 分词匹配）
     */
    List<FacilityDocument> findByNameOrFacilityTypeOrDestinationName(String name, String facilityType, String destinationName);
}
