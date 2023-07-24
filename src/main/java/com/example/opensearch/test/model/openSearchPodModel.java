package com.example.opensearch.test.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "sym-metric-k8s-pod")
@Getter
@Setter
public class openSearchPodModel {
    @Id
    private String id;

    @Field(type = FieldType.Text, name = "object_id")
    private String objectId;

    @Field(type = FieldType.Object, name = "basic")
    private Object basic;

    @Field(type = FieldType.Long, name = "collect_timestamps")
    private Long collectTimestamps;
}
