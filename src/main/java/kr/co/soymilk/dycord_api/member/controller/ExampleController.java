package kr.co.soymilk.dycord_api.member.controller;

import kr.co.soymilk.dycord_api.common.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ExampleController {

    private final AesUtil aesUtil;

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

    @PostMapping("/test/encrypt")
    public ResponseEntity<String> getEncryptTest(@RequestBody HashMap<String, String> body) {
        return ResponseEntity.ok(aesUtil.encrypt(body.get("str")));
    }

    @PostMapping("/test/decrypt")
    public ResponseEntity<String> getDecryptTest(@RequestBody HashMap<String, String> body) {
        return ResponseEntity.ok(aesUtil.decrypt(body.get("str")));
    }

}

