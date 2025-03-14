package kr.co.soymilk.dycord_api.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;

@Slf4j
@Component
public class AesUtil {

    private final String SECRET_KEY = "testtesttesttest";

    public String encrypt(String plainStr) {
        String resultStr = "";

        try {
            // 랜덤 IV 생성
            byte[] iv = new SecureRandom().generateSeed(16);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // 데이터 암호화
            byte[] encryptedByte = cipher.doFinal(plainStr.getBytes());

            // IV와 암호화된 데이터를 합쳐서 Hex로 변환
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedByte.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedByte);

            resultStr =  HexFormat.of().formatHex(byteBuffer.array());
        } catch (Exception e) {
            log.error("encrypt error: {}", e.getMessage());
            resultStr = "";
        }

        return resultStr;
    }

    public String decrypt(String encryptedStr) {
        String plainStr;

        try {
            // Hex 파싱 후 IV와 암호화된 데이터로 분리
            byte[] ivAndDataByte = HexFormat.of().parseHex(encryptedStr);
            byte[] iv = Arrays.copyOfRange(ivAndDataByte, 0, 16);
            byte[] encryptedByte = Arrays.copyOfRange(ivAndDataByte, 16, ivAndDataByte.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decryptedByte = cipher.doFinal(encryptedByte);

            plainStr = new String(decryptedByte);
        } catch (Exception e) {
            log.error("decrypt error: {}", e.getMessage());
            plainStr = "";
        }

        return plainStr;
    }

}
