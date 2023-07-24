package com.example.opensearch.test.service;

import com.example.opensearch.test.model.hostDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class Opensearch {

  public hostDTO opensearchBasic(Object basic) throws JsonProcessingException {

    hostDTO dto = new hostDTO();
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
    dto.setCores((Integer) cpuData.get("cores"));
    dto.setTotal((Integer) memoryData.get("total"));
    dto.setUsed((Integer) memoryUsedData.get("bytes"));
//    hostDTO cpuCores = (hostDTO) cpuData.get("cores");
//    hostDTO memoryTotal = (hostDTO) memoryData.get("total");
//    hostDTO memoryUsed = (hostDTO) memoryUsedData.get("bytes");

//      Integer cpuCores = (Integer) cpuData.get("cores");
//      Integer memoryTotal = (Integer) memoryData.get("total");
//      Integer memoryUsed = (Integer) memoryUsedData.get("bytes");

//      Map<String, Object> cpuTotalNorm = (Map<String, Object>) cpuData.get("total");
//      Map<String, Object> cpuTotalNorm2 = (Map<String, Object>) cpuTotalNorm.get("norm");
//      Double cpuTotalNormPct = (Double) cpuTotalNorm2.get("pct");
//      Map<String, Object> cpuWaitNorm = (Map<String, Object>) cpuData.get("wait");
//      Double cpuWaitNormPct = (Double) cpuWaitNorm.get("pct");
//      Map<String, Object> cpuIowaitNorm = (Map<String, Object>) cpuData.get("iowait");
//      Double cpuIowaitNormPct = (Double) cpuIowaitNorm.get("pct");

    // 가져온 값 출력
    System.out.println("CPU Cores: " + dto.getCores());
    System.out.println("Memory Total: " + dto.getTotal());
    System.out.println("Memory Used: " + dto.getUsed());
//      System.out.println("CPU Total Norm Pct: " + cpuTotalNormPct);
//      System.out.println("CPU Wait Norm Pct: " + cpuWaitNormPct);
//      System.out.println("CPU Iowait Norm Pct: " + cpuIowaitNormPct);
//    if (type.equals("cpuCores")) {
//      return cpuCores;
//    } else if (type.equals("memoryTotal")){
//      return memoryTotal;
//    } else {
//      return memoryUsed;
//    }

    return dto;
  }
}
