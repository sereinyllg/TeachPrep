package com.service.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.result.Result;
import com.service.domain.dto.UserLoginDTO;
import com.service.domain.dto.UserUpdateDTO;
import com.service.domain.dto.UserUpdatepwdDTO;
import com.service.domain.entity.User;
import com.service.domain.vo.CaptchaVO;
import com.service.domain.vo.UserLoginVO;

public interface IUserService extends IService<User> {
    // 用户登录
    UserLoginVO login(UserLoginDTO userLoginDTO);
    // 用户注册
    void register(UserLoginDTO userLoginDTO);
    // 验证码校验
    void verifyCaptcha(String sessionId, String userInput);
    //修改用户信息
    Result update(UserUpdateDTO userUpdateDTO);
    //修改用户密码
    Result updatepwd( UserUpdatepwdDTO userUpdatepwdDTO);
    //生成验证码
    CaptchaVO genCaptcha();
    //验证码校验
    void verify(UserLoginDTO userLoginDTO);
}
