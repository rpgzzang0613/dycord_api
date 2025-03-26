package kr.co.soymilk.dycord_api.common.util;

import kr.co.soymilk.dycord_api.common.properties.AesProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class AesUtil {

    private final AesProperties aesProperties;

    public String encrypt(String targetStr) {
        String resultStr = "";

        if (targetStr == null || targetStr.isEmpty()) {
            throw new IllegalArgumentException("targetStr은 null이나 빈값이 올 수 없음");
        }

        try {
            // 랜덤 IV 생성
            byte[] iv = new SecureRandom().generateSeed(16);

            // 암호화 세팅
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(aesProperties.getSecretKey().getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // 평문 String을 byte로 변환
            byte[] plainBytes = targetStr.getBytes();

            // 변환한 byte를 암호화
            byte[] encryptedByte = cipher.doFinal(plainBytes);

            // IV byte와 암호화된 byte를 결합
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedByte.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedByte);
            byte[] combinedBytes = byteBuffer.array();

            // 결합한 byte를 URL Safe Base64 String으로 인코딩
            resultStr = Base64.getUrlEncoder().withoutPadding().encodeToString(combinedBytes);
        } catch (Exception e) {
            throw new IllegalStateException("암호화 오류 발생: " + e.getMessage());
        }

        return resultStr;
    }

    public String decrypt(String targetStr) {
        String resultStr;

        try {
            // Base64 String을 IV와 데이터가 결합된 byte 배열로 디코딩
            byte[] combinedBytes = Base64.getUrlDecoder().decode(targetStr);

            // 결합된 byte 배열을 분리
            byte[] iv = Arrays.copyOfRange(combinedBytes, 0, 16);
            byte[] encryptedByte = Arrays.copyOfRange(combinedBytes, 16, combinedBytes.length);

            // 복호화 세팅
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(aesProperties.getSecretKey().getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 디코딩 후 나온 암호화된 byte 배열을 복호화
            byte[] decryptedByte = cipher.doFinal(encryptedByte);

            // 복호화된 byte 배열을 String으로 변환
            resultStr = new String(decryptedByte);
        } catch (Exception e) {
            throw new IllegalStateException("복호화 오류 발생: " + e.getMessage());
        }

        return resultStr;
    }

}
