package org.example.service;

import org.example.domain.dos.UserDO;

public interface UserService extends BaseService<UserDO, Long> {

    /**
     * 登录
     * @param userDO
     * @return
     */
    UserDO login(UserDO userDO);
}
