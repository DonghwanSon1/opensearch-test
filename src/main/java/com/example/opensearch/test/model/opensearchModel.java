package com.example.opensearch.test.model;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "sym-metric-vmware-vm")
@Getter
@Setter
public class opensearchModel {
  @Id
  private String id;

  @Field(type = FieldType.Text, name = "object_id")
  private String objectId;

  @Field(type = FieldType.Date, name = "time_")
  private Date timestamp;

  @Field(type = FieldType.Object, name = "basic")
  private Object basic;

  @Field(type = FieldType.Object, name = "system")
  private Object system;

  @Field(type = FieldType.Object, name = "cpu")
  private Object cpu;

  @Field(type = FieldType.Long, name = "basic.system.cpu.cores")
  private Long cores;

  @Field(type = FieldType.Long, name = "collect_timestamps")
  private Long collectTimestamps;

}