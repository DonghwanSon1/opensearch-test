package com.example.opensearch.test.repository;

import java.util.List;

import com.example.opensearch.test.model.openSearchHostModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface opensearchHostRepository extends ElasticsearchRepository<openSearchHostModel, String> {


  List<openSearchHostModel> findTop1ByObjectIdOrderByCollectTimestampsDesc(String objectId);

  List<openSearchHostModel> findTop12ByObjectIdOrderByCollectTimestampsDesc(String objectId);
}