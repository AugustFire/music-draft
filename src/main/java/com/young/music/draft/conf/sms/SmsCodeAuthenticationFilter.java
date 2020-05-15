package com.young.music.draft.conf.sms;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yzx
 * create_time 2020/5/15
 */
public class SmsCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String CUSTOM_SECURITY_FORM_PHONE_KEY = "phoneNum";

    private String phoneParameter = CUSTOM_SECURITY_FORM_PHONE_KEY;
    private boolean postOnly = true;


    public SmsCodeAuthenticationFilter() {
        super(new AntPathRequestMatcher("/login/phone", "POST"));
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        if (postOnly && !HttpMethod.POST.toString().equals(request.getMethod())) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
        String phoneNum = obtainPhoneNum(request);
        if (phoneNum == null) {
            phoneNum = "";
        }

        phoneNum = phoneNum.trim();

        SmsCodeAuthenticationToken authRequest = new SmsCodeAuthenticationToken(phoneNum);

        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }


    protected String obtainPhoneNum(HttpServletRequest request) {
        return request.getParameter(phoneParameter);
    }

    protected void setDetails(HttpServletRequest request,
                              SmsCodeAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }


    public void setPhoneNumParameter(String phoneNumParameter) {
        Assert.hasText(phoneNumParameter, "PhoneNum parameter must not be empty or null");
        this.phoneParameter = phoneNumParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getPhoneNumParameter() {
        return this.phoneParameter;
    }


}
