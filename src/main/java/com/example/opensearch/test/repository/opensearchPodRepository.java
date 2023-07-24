package com.example.opensearch.test.repository;

import com.example.opensearch.test.model.openSearchHostModel;
import com.example.opensearch.test.model.openSearchPodModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface opensearchPodRepository extends ElasticsearchRepository<openSearchPodModel, String> {


  List<openSearchPodModel> findTop10ByObjectIdOrderByCollectTimestampsDesc(String objectId);

  List<openSearchPodModel> findTop12ByObjectIdOrderByCollectTimestampsDesc(String objectId);
}