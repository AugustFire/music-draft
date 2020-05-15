package com.young.music.draft.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author yzx
 * create_time 2020/5/13
 */

@Slf4j
@Component
@SuppressWarnings("all")
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {


    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("CustomAuthenticationFailureHandler...登录失败");
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", 401);
        map.put("msg", exception.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(map));
    }
}
