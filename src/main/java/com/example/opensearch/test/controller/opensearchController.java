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
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) throws JsonProcessingException {

    long startTime = System.nanoTime();
    Map<String, Object> opensearch = this.getBasic(objectId);
    Map<String, Object> result = new HashMap<>();
    result.put("cores", opensearch.get("cpuCores"));

    long endTime = System.nanoTime();
    long elapsedTime  = endTime - startTime;
    DecimalFormat df = new DecimalFormat("0.00");
    System.out.println("시간차이(s) : "  + df.format((elapsedTime * 1e-9)));
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
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) throws JsonProcessingException {

    Map<String, Object> opensearch = this.getBasic(objectId);
    Map<String, Object> result = new HashMap<>();
    result.put("memoryTotal", opensearch.get("memoryTotal"));

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
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) throws JsonProcessingException {

    Map<String, Object> opensearch = this.getBasic(objectId);
    Map<String, Object> result = new HashMap<>();
    result.put("memoryUsed", opensearch.get("memoryUsed"));

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
      @RequestParam(value = "host", required = false, defaultValue = "") String objectId) throws JsonProcessingException {

    Map<String, Object> opensearch = this.getBasic(objectId);
    Map<String, Object> result = new HashMap<>();
    result.put("storageReadAverage", opensearch.get("storageReadAverage"));
    result.put("storageWriteAverage", opensearch.get("storageWriteAverage"));

    return result;
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
    Map<String, Object> result = new HashMap<>();

    // 여러개의 pod를 가져왔을때.
    List<Map<String, Object>> cmpPodList = new ArrayList<>();
    Map<String, Object> podMap = new HashMap<>();
    podMap.put("pod_name", "machine-config-daemon-pwhc4");
    cmpPodList.add(podMap);
    Map<String, Object> podMap2 = new HashMap<>();
    podMap2.put("pod_name", "network-check-target-29h9v");
    cmpPodList.add(podMap2);
    Map<String, Object> podMap3 = new HashMap<>();
    podMap3.put("pod_name", "node-exporter-j5m9f");
    cmpPodList.add(podMap3);
    Map<String, Object> podMap4= new HashMap<>();
    podMap4.put("pod_name", "ingress-canary-9sd76");
    cmpPodList.add(podMap4);


    Map<Long, Double> timeTotalMap = new HashMap<>();
    int a = cmpPodList.size();
    cmpPodList.forEach(v -> {
      podRepository.findTop12ByObjectIdOrderByCollectTimestampsDesc((String) v.get("pod_name")).forEach(r -> {
        try {
        if (timeTotalMap.get(r.getCollectTimestamps()) == null) {
          timeTotalMap.put(r.getCollectTimestamps(), (Double) this.getPodBasic(r.getBasic(), r.getObjectId(), r.getCollectTimestamps()));
        } else {
          timeTotalMap.put(r.getCollectTimestamps(), timeTotalMap.get(r.getCollectTimestamps()) + (Double) this.getPodBasic(r.getBasic(), r.getObjectId(), r.getCollectTimestamps()));
        }
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      });
    });

    Map<String, Object> resultMap = new HashMap<>();
    List<Map<String, Object>> resData = new ArrayList<>();

    DecimalFormat df = new DecimalFormat("0.00");
    timeTotalMap.forEach((key, value) -> {
      Map<String, Object> mainMap = new HashMap<>();

      mainMap.put("time", key);
      mainMap.put("value", Double.valueOf(df.format(value / a)));
      resData.add(mainMap);
      resultMap.put("cpu_usage_avg", resData);
    });

    return resultMap;
  }


  /**
   * 비교.
   *
   * 전 opensearch 조회
   *  장점.
   *  -> 컬럼의 object 속에 있는 키값들을 가져올 수 있다.
   *  -> 원하는 시간 및 간격을 지정할 수 있다.
   *  단점.
   *  -> 너무 많은 소스를 사용해야 되며, 지금 만들어 놓은거 말고는 사용하기 어렵다.
   *  -> 원하는 시간 및 간격을 지정했을 때 수집된 데이터가 없으면 NULl값이 계속 발생한다.
   *
   * 현 opensearch 조회
   *  장점.
   *  -> 사용하기 쉽다.
   *  -> 여러개의 메트릭을 한번에 다 뽑을 수 있다.
   *  -> 상당한 소스를 간략화 할 수 있다.
   *  단점.
   *  -> 각 컬럼만 조회가 가능하고, 컬럼이 object이면 따로 가공해서 사용해야된다.
   *  -> java 17이상 사용해야되며, spring Boot는 3이상 사용해야된다.
   *  -> 여러 object_id를 조건을 걸때 for문을 사용해야될거 같다. (성능적으로 안좋을거 같다.) - IN 절을 사용하면 limit 할때 잘리게 된다. - 더 찾아보면 좋은 방법이 생길수도?
   *  -> 각 인덱스 별 엔티티를 생성해야된다.
   *
   *  ------------------------------------------------------------------------------------------------------------------------------------
   *  속도 비교.
   *  -> 전에꺼랑 현재꺼랑 시간차이는 비슷함. - (opensearch 시작부터 결과값 받아오는 끝까지.)
   *     평균적으로 10ms정도 전에꺼가 더 빠름.
   *
   *  ------------------------------------------------------------------------------------------------------------------------------------
   *  검토.
   *  현재 우리가 사용하는 opensearch에서 꺼내오는것들은 spring data opensearch도 충분히 다 가져올 수 있으며,
   *  성능적으로 속도만 보면 그 전 opensearch에서 꺼내오는게 나은거 같으며, 소스/코드 면으로서 spring Data Opensearch가 나은거 같다.
   *
   *  ------------------------------------------------------------------------------------------------------------------------------------
   *  결론. java 17이상 사용하고, 추후 opensearch에 대해 쿼리를 더 많이 만들거면 spring data opensearch 사용하는게 낫고,
   *  17이하 이고, 현재 대시보드에서 한 조회 기능 말고 더 사용하지 않을거면 그 전의 opensearch를 사용하는게 나은거 같다.
   *
   */


  private List getPod(Map<String, Object> reqParam) {
    return (List) reqParam.get("resource_id");
  }

  /**
   * Basic을 받아 원하는 값 뽑는 로직
   * type에 원하는 type 설정
   */
  private Object getPodBasic(Object basic, String objectId, Long collectTimestamps) throws JsonProcessingException {

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
    Double cpuPct = Double.valueOf(df.format(avgData.get("pct")));

    System.out.println("CPU Pct: " + cpuPct + " / " + "ObjectId: " + objectId + " / " + "Time: " + collectTimestamps);
    return cpuPct;
//
  }

  /**
   * Basic을 받아 원하는 값 뽑는 로직
   */
  private Map<String, Object> getBasic(String objectId) throws JsonProcessingException {

    final Object[] basic = new Object[1];
    hostRepository.findTop1ByObjectIdOrderByCollectTimestampsDesc(objectId).forEach(v -> {
      basic[0] = v.getBasic();
    });

    // Jackson ObjectMapper 객체 생성
    ObjectMapper objectMapper = new ObjectMapper();

    String json = objectMapper.writeValueAsString(basic[0]);
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

    Map<String, Object> result = new HashMap<>();
    result.put("cpuCores", cpuCores);
    result.put("memoryTotal", memoryTotal);
    result.put("memoryUsed", memoryUsed);
    result.put("storageReadAverage", storageReadAverage);
    result.put("storageWriteAverage", storageWriteAverage);

    // 가져온 값 출력
//    System.out.println("CPU Cores: " + cpuCores);
//    System.out.println("Memory Total: " + memoryTotal);
//    System.out.println("Memory Used: " + memoryUsed);
//    System.out.println("Storage Read Average: " + storageReadAverage);
//    System.out.println("Storage Write Average: " + storageWriteAverage);

    return result;
  }
}
