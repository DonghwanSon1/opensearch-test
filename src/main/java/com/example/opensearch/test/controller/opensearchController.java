package com.example.opensearch.test.controller;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.opensearch.test.model.opensearchModel;
import com.example.opensearch.test.repository.opensearchRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/opensearch/test")
public class opensearchController {
  private final opensearchRepository repository;

  public opensearchController(opensearchRepository repository) {
    this.repository = repository;
  }

  @GetMapping(value = "/vm", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Map search(
      @RequestParam(value = "vm", required = false, defaultValue = "") String vm) {
    Map<String, Object> result = new HashMap<>();
    repository.findTop2ByOrderByCollectTimestampsDesc().forEach(v -> {
      result.put(v.getObjectId(), v.getSystem());
    });
    return result;
  }
}
