package com.example.opensearch.test.repository;

import java.math.BigDecimal;
import java.util.List;

import com.example.opensearch.test.model.opensearchModel;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface opensearchRepository extends ElasticsearchRepository<opensearchModel, String> {

  List<opensearchModel> findTop2ByOrderByCollectTimestampsDesc();
}