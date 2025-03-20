package kr.co.soymilk.dycord_api.common.exception;

import kr.co.soymilk.dycord_api.member.dto.auth.NaverErrorDto;

public class ErrorFromNaverException extends RuntimeException {

    public ErrorFromNaverException(int statusCode, NaverErrorDto dto) {
        super("네이버 API 오류 - HttpStatus: " + statusCode + ", Error: " + dto.getError() +
                ", ErrorDescription: " + dto.getError_description());
    }

}
