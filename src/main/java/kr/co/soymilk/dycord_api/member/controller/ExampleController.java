package kr.co.soymilk.dycord_api.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class ExampleController {

    @GetMapping("/test")
    public ResponseEntity<HashMap<String, Object>> getTest() {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", "get ok");
        return ResponseEntity.ok(resultMap);
    }

    @PostMapping("/test")
    public ResponseEntity<HashMap<String, Object>> postTest() {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", "post ok");
        return ResponseEntity.ok(resultMap);
    }

    @GetMapping("/deny-test")
    public ResponseEntity<HashMap<String, Object>> getDenyTest() {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", "allowed?");
        return ResponseEntity.ok(resultMap);
    }

}
