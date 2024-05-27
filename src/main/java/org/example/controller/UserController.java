package org.example.controller;

import org.example.domain.AuthLogReqVO;
import org.example.domain.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/user")
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/test")
    public Result test(){
        return Result.success(String.valueOf(LocalDateTime.now()));
    }

    @PostMapping("/login")
    public Result login(@RequestBody @Valid AuthLogReqVO reqVO){
        return Result.success(reqVO);
    }

}
