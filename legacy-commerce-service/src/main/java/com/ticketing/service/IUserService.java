package com.ticketing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketing.domain.dto.LoginFormDTO;
import com.ticketing.domain.po.User;
import com.ticketing.domain.vo.UserLoginVO;

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
