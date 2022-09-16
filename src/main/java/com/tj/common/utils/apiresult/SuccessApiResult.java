package com.tj.common.utils.apiresult;

import lombok.Data;

/**
 * 正确返回体
 */
@Data
public class SuccessApiResult extends AbstractApiResult {

    private Object data;

    SuccessApiResult(Object data) {
        this.code = "0";
        this.data = data;
    }

}
