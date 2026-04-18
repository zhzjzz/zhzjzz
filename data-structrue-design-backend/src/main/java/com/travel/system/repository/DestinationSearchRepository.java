package com.travel.system.repository;

import com.travel.system.search.DestinationDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch Repository for {@link DestinationDocument}.
 */
@Repository
public interface DestinationSearchRepository extends ElasticsearchRepository<DestinationDocument, String> {

    /**
     * 将 Containing 改为普通的字段名匹配，或者明确加上 Match。
     * 这样底层才会调用 match query，走 IK 分词。
     */
    List<DestinationDocument> findByNameOrCategory(String name, String category);

    // 或者使用自定义 @Query（如果你想让两个字段的权重不同，或者更复杂的查询）
    // @Query("{\"bool\": {\"should\": [{\"match\": {\"name\": \"?0\"}}, {\"match\": {\"category\": \"?1\"}}]}}")
    // List<DestinationDocument> searchByNameOrCategory(String name, String category);
}
