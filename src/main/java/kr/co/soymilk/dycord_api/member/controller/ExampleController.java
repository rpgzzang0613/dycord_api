package kr.co.soymilk.dycord_api.member.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@Slf4j
public class ExampleController {

    @GetMapping("/test/get")
    public ResponseEntity<HashMap<String, Object>> getTest(@RequestParam HashMap<String, Object> paramObj) {
        log.info("paramObj: {}", paramObj);
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", "get ok");
        return ResponseEntity.ok(resultMap);
    }

    @PostMapping("/test/post-form")
    public ResponseEntity<HashMap<String, Object>> postFormTest(@RequestParam HashMap<String, Object> paramObj) {
        log.info("paramObj: {}", paramObj);
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", "post form ok");
        return ResponseEntity.ok(resultMap);
    }

    @PostMapping("/test/post-json")
    public ResponseEntity<HashMap<String, Object>> postJsonTest(@RequestBody HashMap<String, Object> paramObj) {
        log.info("paramObj: {}", paramObj);
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", "post json ok");
        return ResponseEntity.ok(resultMap);
    }

    @GetMapping("/deny-test")
    public ResponseEntity<HashMap<String, Object>> getDenyTest() {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", "allowed?");
        return ResponseEntity.ok(resultMap);
    }

}
