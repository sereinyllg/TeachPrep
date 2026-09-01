package com.service.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.enums.UserStatus;
import com.common.exception.ForbiddenException;
import com.common.exception.UnauthorizedException;
import com.common.properties.JwtProperties;
import com.common.result.Result;
import com.common.utils.CaptchaUtils;
import com.common.utils.JwtUtil;
import com.service.domain.dto.UserLoginDTO;
import com.service.domain.dto.UserUpdateDTO;
import com.service.domain.dto.UserUpdatepwdDTO;
import com.service.domain.entity.User;
import com.service.domain.vo.CaptchaVO;
import com.service.domain.vo.UserLoginVO;
import com.service.mapper.UserMapper;
import com.service.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import com.common.exception.BadRequestException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;

    // 用户登录
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        // 1.数据校验
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        // 2.根据用户名
        User user = lambdaQuery().eq(User::getUsername, username).one();
        Assert.notNull(user, "用户名错误");
        // 3.校验是否禁用
        if (user.getStatus() == UserStatus.FROZEN ) {
            throw new ForbiddenException("用户被冻结");
        }
        // 4.校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }
        // 5.生成TOKEN
        //登录成功后，生成jwt令牌
        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        //  生成令牌
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTTL(),
                claims);
        // 6.封装VO返回
        UserLoginVO vo = new UserLoginVO();
        BeanUtils.copyProperties(user, vo);
        vo.setId(String.valueOf(user.getId()));
        vo.setToken(token);
        return vo;
    }

    // 注册
    @Override
    public void register(UserLoginDTO userLoginDTO) {
        // 1. 校验用户名是否已存在
        if (lambdaQuery().eq(User::getUsername, userLoginDTO.getUsername()).count()>0)  {
            throw new BadRequestException("用户名已存在");
        }

        // 2. 密码加密
        User user = new User();
        BeanUtils.copyProperties(userLoginDTO,  user);
        user.setPassword(passwordEncoder.encode(userLoginDTO.getPassword()));
        user.setStatus(UserStatus.NORMAL);

        // 3. 保存用户
        userMapper.insert(user);
    }

    // 验证码校验
    @Override
    public void verifyCaptcha(String sessionId, String userInput) {
        // 1. 从Redis获取存储的验证码文本
        String redisKey = "captcha:" + sessionId;
        String correctCaptcha = redisTemplate.opsForValue().get(redisKey);

        // 2. 检查验证码是否存在（可能已过期）
        if (correctCaptcha == null) {
            throw new BadRequestException(" 验证码已过期，请刷新重试");
        }

        // 3. 比较用户输入与正确验证码（忽略大小写）
        if (!correctCaptcha.equalsIgnoreCase(userInput))  {
            throw  new BadRequestException(" 验证码错误");
        }

        // 4. 验证成功后删除Redis中的验证码（防止重复使用）
        redisTemplate.delete(redisKey);

        // 5. 返回验证成功
    }


    //生成验证码
    @Override
    public CaptchaVO genCaptcha() {
        // 1. 生成验证码图片和文本
        Map<String, String> captcha = CaptchaUtils.generateCaptcha();
        String captchaText = captcha.get("text");
        String base64Image = captcha.get("image");

        log.info("生成验证码:{}",  captchaText);
        //uuid
        //雪花算法
        // 2. 存储验证码文本到Redis（有效期5分钟）
        String sessionId = String.valueOf(IdUtil.getSnowflakeNextId()); // 使用雪花算法生成唯一 sessionId
        log.info("sessionId:{}", sessionId);
        redisTemplate.opsForValue().set(
                "captcha:" + sessionId,
                captchaText,
                5*60*1000,
                java.util.concurrent.TimeUnit.MILLISECONDS
        );

        // 3. 返回
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setImageData(base64Image);
        captchaVO.setSessionId(sessionId);
        return captchaVO;
    }

    // 校验验证码
    public void verify(UserLoginDTO userLoginDTO) {
        // 1. 验证码校验
        try {
            verifyCaptcha(
                    String.valueOf(userLoginDTO.getSessionId()),
                    userLoginDTO.getCode()
            );
            log.info(" 验证码校验通过, sessionId: {}", userLoginDTO.getSessionId());
        } catch (BadRequestException e) {
            log.warn(" 验证码校验失败: {}", e.getMessage());
            throw e; // 交给全局异常处理器处理
        }
    }

    // 修改用户信息
    @Override
    public Result update(UserUpdateDTO userUpdateDTO) {
        // 创建用户对象并设置修改项
        User user = new User();
        BeanUtils.copyProperties(userUpdateDTO, user);
        user.setId(Long.valueOf(userUpdateDTO.getId()));

        // 修改个人信息
        updateById(user);
        return Result.success("修改成功");
    }

    // 修改用户密码
    @Override
    public Result updatepwd(UserUpdatepwdDTO userUpdatepwdDTO) {
        // 验证旧密码
        if (!passwordEncoder.matches(userUpdatepwdDTO.getOldPassword(), (getById(userUpdatepwdDTO.getId())).getPassword())) {
            throw new UnauthorizedException("旧密码不正确");
        }

        // 设置新密码并加密
        User user = new User();
        user.setId(Long.valueOf(userUpdatepwdDTO.getId()));
        user.setPassword(passwordEncoder.encode(userUpdatepwdDTO.getNewPassword()));

        // 修改个人信息
        updateById(user);
        return Result.success("修改成功");
    }

}
