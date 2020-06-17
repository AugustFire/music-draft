package com.young.music.draft.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 验证码拦截
 *
 * @author yzx
 * create_time 2020/5/14
 */
@Slf4j
@Component
public class ValidateCodeFilter extends OncePerRequestFilter {

    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String targetUrl = "/login/phone";
        String targetMethod = "POST";
        AntPathRequestMatcher antPathRequestMatcher = new AntPathRequestMatcher(targetUrl);
        if (antPathRequestMatcher.matches(request)
                && targetMethod.equalsIgnoreCase(request.getMethod())) {
            log.debug("ValidateCodeFilter....check");
            try {
                validate(request);
            } catch (ValidateCodeException codeException) {
                authenticationFailureHandler.onAuthenticationFailure(request, response, codeException);
                return;
            }

        }
        filterChain.doFilter(request, response);

    }

    private void validate(HttpServletRequest request) throws ServletRequestBindingException {
        String rawCode = ServletRequestUtils.getStringParameter(request, "code");
        String code = "1234";
        if (!code.equals(rawCode)) {
            throw new ValidateCodeException("验证码肯定是错了!");
        }

    }
}
