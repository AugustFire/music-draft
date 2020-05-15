package com.young.music.draft.conf;

import org.springframework.security.core.AuthenticationException;

/**
 * 验证码校验失败的异常
 *
 * @author yzx
 * create_time 2020/5/14
 */
public class ValidateCodeException extends AuthenticationException {

    public ValidateCodeException(String msg, Throwable t) {
        super(msg, t);
    }

    public ValidateCodeException(String msg) {
        super(msg);
    }
}
