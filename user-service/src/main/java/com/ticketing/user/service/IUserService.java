package com.ticketing.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketing.user.domain.dto.LoginFormDTO;
import com.ticketing.user.domain.po.User;
import com.ticketing.user.domain.vo.UserLoginVO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Ticketing Team
 * @since 2023-05-05
 */
public interface IUserService extends IService<User> {

    UserLoginVO login(LoginFormDTO loginFormDTO);

    void deductMoney(String pw, Integer totalFee);
}
