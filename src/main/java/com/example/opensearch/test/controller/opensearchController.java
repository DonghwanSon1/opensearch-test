package com.example.opensearch.test.controller;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.opensearch.test.model.opensearchModel;
import com.example.opensearch.test.repository.opensearchRepository;
import jakarta.json.JsonObject;
import org.apache.tomcat.util.json.JSONParser;
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

  /**
   * 단일 host의 cpu cores 조회
   *
   * # OpenSearch SQL 쿼리
   * => select collect_timestamps, basic from sym-metric-vmware-host where object_id = {host} order by collect_timestamps desc limit 1
   *
   */
  @GetMapping(value = "/host/cpu/cores", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Map cpuSearch(
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) {
    Map<Long, Object> result = new HashMap<>();
    repository.findTop1ByObjectIdOrderByCollectTimestampsDesc(objectId).forEach(v -> {

      try {
        result.put(v.getCollectTimestamps(), this.getBasic(v.getBasic(), "cpuCores"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });
    return result;
  }

  /**
   * 단일 host의 memory total 조회
   *
   * # OpenSearch SQL 쿼리
   * => select collect_timestamps, basic from sym-metric-vmware-host where object_id = {host} order by collect_timestamps desc limit 1
   *
   */
  @GetMapping(value = "/host/memory/total", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Map memorySearch(
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) {
    Map<Long, Object> result = new HashMap<>();
    repository.findTop1ByObjectIdOrderByCollectTimestampsDesc(objectId).forEach(v -> {
      try {
        result.put(v.getCollectTimestamps(), this.getBasic(v.getBasic(), "memoryTotal"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });
    return result;
  }

  /**
   * 단일 host의 memory used 조회
   *
   * # OpenSearch SQL 쿼리
   * => select collect_timestamps, basic from sym-metric-vmware-host where object_id = {host} order by collect_timestamps desc limit 1
   *
   */
  @GetMapping(value = "/host/memory/used", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Map memoryUsedSearch(
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) {
    Map<Long, Object> result = new HashMap<>();
    repository.findTop1ByObjectIdOrderByCollectTimestampsDesc(objectId).forEach(v -> {
      try {
        result.put(v.getCollectTimestamps(), this.getBasic(v.getBasic(), "memoryUsed"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });
    return result;
  }

  // 현재 한개의 object_id를 조회했을경우 가능한 부분이고, 결국 쿼리로서는 각 컬럼만 가져올 수 있고, 그 컬럼이 Object이면 따로 가공을 해서 가져와야되는 방식이다.
  // 두개 이상의 object_id를 조회하려면 for문으로 Repository에 각각의 Object_id를 넣는 방식으로 가져올 수 있을 거 같다.
  // 비교. 그전 opensearch 한번의 원하는 컬럼을 가져와 사용할 수 있다.
  /**
   * 비교. (현재로서)
   *
   * 전 opensearch 조회
   *  장점.
   *  -> object 컬럼의 키값들을 가져와 사용할 수 있다.
   *  -> 원하는 시간 및 간격을 지정할 수 있다.
   *  단점.
   *  -> 너무 많은 소스를 사용해야 되며, 지금 만들어 놓은거 말고는 사용하기 어렵다.
   *  -> 원하는 시간 및 간격을 지정했을 때 수집된 데이터가 없으면 NULl값이 계속 발생한다.
   *
   * 현 opensearch 조회
   *  장점.
   *  -> 사용하기 쉽다.
   *  -> 시간을 기준으로 order by 하여 가장 최신의 데이터가 나와 null값이 발생 하지 않으며,
   *     간격은 수집을 5분씩 하니 그 간격대로 나와 구지 간격을 지정할 필요없이 TOP으로 가져오면 된다.
   *  단점.
   *  -> 각 컬럼만 조회가 가능하고, object 컬럼은 따로 가공해서 사용해야된다.
   *  -> java 17이상 사용해야되며, spring Boot는 3이상 사용해야된다.
   *  -> 여러 object_id를 조건을 걸때 for문을 사용해야될거 같다. (성능적으로 안좋을거 같다.) - 더 찾아봐야됨
   */


  /**
   * Basic을 받아 원하는 값 뽑는 로직
   * type에 원하는 type 설정
   */
  private Object getBasic(Object basic, String type) throws JsonProcessingException {

    // Jackson ObjectMapper 객체 생성
      ObjectMapper objectMapper = new ObjectMapper();

      String json = objectMapper.writeValueAsString(basic);

      // JSON 문자열을 Map으로 변환
      Map<String, Object> data = objectMapper.readValue(json, Map.class);

      // "system" 객체 가져오기
      Map<String, Object> systemData = (Map<String, Object>) data.get("host");

      // "cpu"의 값을 가져오기
      Map<String, Object> cpuData = (Map<String, Object>) systemData.get("cpu");
      Map<String, Object> memoryData = (Map<String, Object>) systemData.get("memory");
      Map<String, Object> memoryUsedData = (Map<String, Object>) memoryData.get("used");
      Integer cpuCores = (Integer) cpuData.get("cores");
      Integer memoryTotal = (Integer) memoryData.get("total");
      Integer memoryUsed = (Integer) memoryUsedData.get("bytes");
//      Map<String, Object> cpuTotalNorm = (Map<String, Object>) cpuData.get("total");
//      Map<String, Object> cpuTotalNorm2 = (Map<String, Object>) cpuTotalNorm.get("norm");
//      Double cpuTotalNormPct = (Double) cpuTotalNorm2.get("pct");
//      Map<String, Object> cpuWaitNorm = (Map<String, Object>) cpuData.get("wait");
//      Double cpuWaitNormPct = (Double) cpuWaitNorm.get("pct");
//      Map<String, Object> cpuIowaitNorm = (Map<String, Object>) cpuData.get("iowait");
//      Double cpuIowaitNormPct = (Double) cpuIowaitNorm.get("pct");

      // 가져온 값 출력
      System.out.println("CPU Cores: " + cpuCores);
      System.out.println("Memory Total: " + memoryTotal);
      System.out.println("Memory Used: " + memoryUsed);
//      System.out.println("CPU Total Norm Pct: " + cpuTotalNormPct);
//      System.out.println("CPU Wait Norm Pct: " + cpuWaitNormPct);
//      System.out.println("CPU Iowait Norm Pct: " + cpuIowaitNormPct);
    if (type.equals("cpuCores")) {
      return cpuCores;
    } else if (type.equals("memoryTotal")){
      return memoryTotal;
    } else {
      return memoryUsed;
    }
  }
}
