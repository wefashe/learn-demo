package org.example.controller;

import org.example.domain.dos.UserDO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
public class UserController extends BaseController<UserDO,Long> {

    /**
     * 注册
     * @return
     */
    @PostMapping("register")
    public UserDO register(){
        return new UserDO();
    }


    /**
     * 登录
     * @return
     */
    @PostMapping("login")
    public UserDO login(){
        return new UserDO();
    }

}
