package com.example.opensearch.test.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Document(indexName = "sym-metric-vmware-host")
@Getter
@Setter
public class openSearchHostModel {
    @Id
    private String id;

    @Field(type = FieldType.Text, name = "object_id")
    private String objectId;

    @Field(type = FieldType.Date, name = "time_")
    private Date timestamp;

    @Field(type = FieldType.Object, name = "basic")
    private Object basic;

    @Field(type = FieldType.Long, name = "collect_timestamps")
    private Long collectTimestamps;
}
