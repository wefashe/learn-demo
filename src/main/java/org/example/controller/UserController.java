package org.example.controller;

import org.example.domain.AuthLogReqVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/test")
    public String test(){
        return "成功";
    }

    @PostMapping("/login")
    public String login(@RequestBody @Valid AuthLogReqVO reqVO){
        return "成功";
    }

}
