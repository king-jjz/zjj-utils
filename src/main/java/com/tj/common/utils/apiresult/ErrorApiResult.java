package com.tj.common.utils.apiresult;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 错误返回.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class ErrorApiResult extends AbstractApiResult {

    private String msg;

    ErrorApiResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
