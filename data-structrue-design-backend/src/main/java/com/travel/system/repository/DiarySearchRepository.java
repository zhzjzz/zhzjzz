package com.travel.system.repository;

import com.travel.system.search.DiaryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Elasticsearch Repository for {@link DiaryDocument}.
 * 使用 match query 走 IK 分词，而非 wildcard (Containing) 查询。
 */
public interface DiarySearchRepository extends ElasticsearchRepository<DiaryDocument, String> {
    /**
     * 根据标题或内容进行全文搜索（IK 分词匹配）
     */
    List<DiaryDocument> findByTitleOrContent(String title, String content);
}
