package com.xxxx.server.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CaptchaController {
    @ApiOperation(value="验证码")
    @GetMapping("/captcha")
    public void captcha(){

    }
}
