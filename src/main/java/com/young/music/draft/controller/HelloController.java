package com.young.music.draft.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * @author yzx
 * create_time 2020/5/8
 */
@RestController
@Slf4j
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("authentication    --->{}",authentication.toString());
        Object principal = authentication.getPrincipal();
        log.info("principal         --->{}",authentication.toString());
        return LocalDateTime.now().toString();
    }
}
