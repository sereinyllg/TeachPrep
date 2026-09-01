package com.service.controller;

import com.common.context.BaseContext;
import com.common.result.Result;
import com.service.domain.dto.UserLoginDTO;
import com.service.domain.dto.UserUpdateDTO;
import com.service.domain.dto.UserUpdatepwdDTO;
import com.service.domain.vo.CaptchaVO;
import com.service.domain.vo.UserLoginVO;
import com.service.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "用户管理")
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 生成验证码
     */
    @GetMapping("/captcha")
    @ApiOperation(value = "生成验证码")
    public Result<CaptchaVO> getCaptcha() {
        CaptchaVO captchaVO = userService.genCaptcha();
        return Result.success(captchaVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info(" 用户登录成功, username: {}", userLoginDTO.getUsername());
        // 1. 验证码校验
        userService.verify(userLoginDTO);
        // 2. 执行登录
        UserLoginVO userLoginVO = userService.login(userLoginDTO);

        return Result.success(userLoginVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public Result register(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户注册:{}" , userLoginDTO.getCode());
        // 1. 验证码校验
        userService.verify(userLoginDTO);
        userService.register(userLoginDTO);
        return Result.success("注册成功");
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改个人信息")
    public Result update( @RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("修改个人信息" );
        userUpdateDTO.setId(String.valueOf(BaseContext.getCurrentId()));
        return userService.update(userUpdateDTO);
    }

    /**
     * 修改登录密码
     */
    @PutMapping("/updatepwd")
    @ApiOperation(value = "修改用户密码")
    public Result updatepwd(@RequestBody UserUpdatepwdDTO userUpdatepwdDTO) {
        log.info("修改用户密码" );
        userUpdatepwdDTO.setId(String.valueOf(BaseContext.getCurrentId()));
        return userService.updatepwd(userUpdatepwdDTO);
    }
}
