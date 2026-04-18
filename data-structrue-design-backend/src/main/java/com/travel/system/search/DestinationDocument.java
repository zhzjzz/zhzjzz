package com.travel.system.search;

import com.travel.system.model.Destination;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(indexName = "travel_destination")
public class DestinationDocument {
    @Id
    private String id;
    // 创建索引时使用 ik_max_word（最细粒度拆分，比如：北京、邮电、大学、北京邮电大学）
    // 搜索时使用 ik_smart（粗粒度拆分，避免匹配到太多无关内容）
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String category;

    @Field(type = FieldType.Keyword)
    private String sceneType;
    @Field(type = FieldType.Double)
    private Double heat;
    @Field(type = FieldType.Double)
    private Double rating;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    private Double latitude;
    private Double longitude;

    public DestinationDocument(Destination destination){
        this.id = String.valueOf(destination.getId());
        this.name = destination.getName();
        this.sceneType = destination.getSceneType();
        this.category = destination.getCategory();
        this.heat = destination.getHeat();
        this.rating = destination.getRating();
        this.description = destination.getDescription();
        this.latitude = destination.getLatitude();
        this.longitude = destination.getLongitude();
    }
}
