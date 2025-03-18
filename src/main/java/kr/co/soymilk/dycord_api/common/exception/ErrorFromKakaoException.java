package kr.co.soymilk.dycord_api.common.exception;

import kr.co.soymilk.dycord_api.member.dto.auth.KakaoErrorDto;

public class ErrorFromKakaoException extends RuntimeException {

    public ErrorFromKakaoException(int statusCode, KakaoErrorDto dto) {
        super("카카오 API 오류 - HttpStatus: " + statusCode + ", Error: " + dto.getError() +
                ", ErrorCode: " + dto.getError_code() + ", Description: " + dto.getError_description());
    }

}
