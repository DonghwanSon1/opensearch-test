package com.example.opensearch.test.controller;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.opensearch.test.repository.opensearchPodRepository;
import com.example.opensearch.test.service.Opensearch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.opensearch.test.repository.opensearchHostRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/opensearch/test")
public class opensearchController {
  private final opensearchHostRepository hostRepository;
  private final opensearchPodRepository podRepository;
  private final Opensearch opensearch;

  public opensearchController(opensearchHostRepository hostRepository, opensearchPodRepository podRepository, Opensearch opensearch) {
    this.hostRepository = hostRepository;
    this.podRepository = podRepository;
    this.opensearch = opensearch;
  }

  /**
   * 단일 host의 cpu cores 조회
   * <p>
   * # OpenSearch SQL 쿼리
   * => select collect_timestamps, basic from sym-metric-vmware-host where object_id = {host} order by collect_timestamps desc limit 1
   */
  @GetMapping(value = "/host/cpu/cores", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Map cpuSearch(
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) {
    Map<Long, Object> result = new HashMap<>();

    long beforeTime = System.currentTimeMillis();
    hostRepository.findTop1ByObjectIdOrderByCollectTimestampsDesc(objectId).forEach(v -> {
      try {
        result.put(v.getCollectTimestamps(), this.getBasic(v.getBasic(), "cpuCores"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });

    long afterTime = System.currentTimeMillis();
    long secDiffTime = (afterTime - beforeTime);
    System.out.println("시간차이(ms) : " + secDiffTime);
    return result;
  }

  /**
   * 단일 host의 memory total 조회
   * <p>
   * # OpenSearch SQL 쿼리
   * => select collect_timestamps, basic from sym-metric-vmware-host where object_id = {host} order by collect_timestamps desc limit 1
   */
  @GetMapping(value = "/host/memory/total", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Map memorySearch(
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) {
    Map<Long, Object> result = new HashMap<>();
    hostRepository.findTop1ByObjectIdOrderByCollectTimestampsDesc(objectId).forEach(v -> {
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
   * <p>
   * # OpenSearch SQL 쿼리
   * => select collect_timestamps, basic from sym-metric-vmware-host where object_id = {host} order by collect_timestamps desc limit 1
   */
  @GetMapping(value = "/host/memory/used", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Map memoryUsedSearch(
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) {
    Map<Long, Object> result = new HashMap<>();
    hostRepository.findTop1ByObjectIdOrderByCollectTimestampsDesc(objectId).forEach(v -> {
      try {
        result.put(v.getCollectTimestamps(), this.getBasic(v.getBasic(), "memoryUsed"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });
    return result;
  }

  /**
   * 단일 host의 memory used 조회
   * <p>
   * # OpenSearch SQL 쿼리
   * => select collect_timestamps, basic from sym-metric-vmware-host where object_id = {host} order by collect_timestamps desc limit 1
   */
  @GetMapping(value = "/host/storage/rw/average", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Map storageReadAverageSearch(
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) {
    List resultList = new ArrayList();

    long beforeTime = System.currentTimeMillis();
    hostRepository.findTop12ByObjectIdOrderByCollectTimestampsDesc(objectId).forEach(v -> {
      try {
        Map<String, Object> result = new HashMap<>();
        result.put("read", this.getBasic(v.getBasic(), "storageReadAverage"));
        result.put("write", this.getBasic(v.getBasic(), "storageWriteAverage"));
        result.put("time", v.getCollectTimestamps());
        resultList.add(result);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });

    long afterTime = System.currentTimeMillis();
    long secDiffTime = (afterTime - beforeTime);
    System.out.println("시간차이(ms) : " + secDiffTime);
    Map<String, Object> result3 = new HashMap<>();
    result3.put("data", resultList);
    return result3;
  }

  // ========================================================= =========================================================

  /**
   * 다중 pod의 cpu 조회
   *
   * # OpenSearch SQL 쿼리
   * => select collect_timestamps, basic from sym-metric-k8s-pod where object_id = {host} order by collect_timestamps desc limit 1
   *
   */
  @PostMapping(value = "/pod/cpu", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map podCpuSearch(
      @RequestBody Map<String, Object> reqParam) {
    Map<Long, Object> result = new HashMap<>();
    List podList = this.getPod(reqParam);
    System.out.println(podList);
    String pod = "example-1-build";
    podRepository.findTop10ByObjectIdOrderByCollectTimestampsDesc(pod).forEach(v -> {
      try {
        result.put(v.getCollectTimestamps(), this.getPodBasic(v.getBasic()));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });
    return result;
  }


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
   *  -> 여러개의 메트릭을 한번에 다 뽑을 수 있다. ( ex)11R - pod_TileMap )
   *  단점.
   *  -> 각 컬럼만 조회가 가능하고, object 컬럼은 따로 가공해서 사용해야된다.
   *  -> java 17이상 사용해야되며, spring Boot는 3이상 사용해야된다.
   *  -> 여러 object_id를 조건을 걸때 for문을 사용해야될거 같다. (성능적으로 안좋을거 같다.) - 더 찾아봐야됨
   *
   *
   *  전에꺼랑 현재꺼랑 시간차이는 비슷함.
   *  평균적으로 10ms정도 전에꺼가 더 빠름.
   */


  private List getPod(Map<String, Object> reqParam) {
    return (List) reqParam.get("resource_id");
  }

  /**
   * Basic을 받아 원하는 값 뽑는 로직
   * type에 원하는 type 설정
   */
  private Object getPodBasic(Object basic) throws JsonProcessingException {

    // Jackson ObjectMapper 객체 생성
    ObjectMapper objectMapper = new ObjectMapper();

    String json = objectMapper.writeValueAsString(basic);
    DecimalFormat df = new DecimalFormat("0.00");

    // JSON 문자열을 Map으로 변환
    Map<String, Object> data = objectMapper.readValue(json, Map.class);

    // "system" 객체 가져오기
    Map<String, Object> k8sData = (Map<String, Object>) data.get("k8s");

    Map<String, Object> podData = (Map<String, Object>) k8sData.get("pod");
    Map<String, Object> cpuData = (Map<String, Object>) podData.get("cpu");
    Map<String, Object> avgData = (Map<String, Object>) cpuData.get("avg");
    Integer cpuPct = (Integer) avgData.get("pct");

    // 가져온 값 출력
    System.out.println("CPU Pct: " + cpuPct);
    return cpuPct;
  }

  /**
   * Basic을 받아 원하는 값 뽑는 로직
   * type에 원하는 type 설정
   */
  private Object getBasic(Object basic, String type) throws JsonProcessingException {

    // Jackson ObjectMapper 객체 생성
    ObjectMapper objectMapper = new ObjectMapper();

    String json = objectMapper.writeValueAsString(basic);
    DecimalFormat df = new DecimalFormat("0.00");

    // JSON 문자열을 Map으로 변환
    Map<String, Object> data = objectMapper.readValue(json, Map.class);

    // "system" 객체 가져오기
    Map<String, Object> systemData = (Map<String, Object>) data.get("host");


    Map<String, Object> cpuData = (Map<String, Object>) systemData.get("cpu");
    Integer cpuCores = (Integer) cpuData.get("cores");

    Map<String, Object> memoryData = (Map<String, Object>) systemData.get("memory");
    Integer memoryTotal = (Integer) memoryData.get("total");
    Map<String, Object> memoryUsedData = (Map<String, Object>) memoryData.get("used");
    Integer memoryUsed = (Integer) memoryUsedData.get("bytes");

    Map<String, Object> storageData = (Map<String, Object>) systemData.get("storage");
    Map<String, Object> storageReadData = (Map<String, Object>) storageData.get("read");
    Double storageReadAverage = (Double) storageReadData.get("average");

    Map<String, Object> storageWriteData = (Map<String, Object>) storageData.get("write");
    Double storageWriteAverage = Double.valueOf(df.format(storageWriteData.get("average")));

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
    System.out.println("Storage Read Average: " + storageReadAverage);
    System.out.println("Storage Write Average: " + storageWriteAverage);
//      System.out.println("CPU Total Norm Pct: " + cpuTotalNormPct);
//      System.out.println("CPU Wait Norm Pct: " + cpuWaitNormPct);
//      System.out.println("CPU Iowait Norm Pct: " + cpuIowaitNormPct);
    if (type.equals("cpuCores")) {
      return cpuCores;
    } else if (type.equals("memoryTotal")) {
      return memoryTotal;
    } else if (type.equals("memoryUsed")) {
      return memoryUsed;
    } else if (type.equals("storageReadAverage")) {
      return storageReadAverage;
    } else {
      return storageWriteAverage;
    }
  }
}
