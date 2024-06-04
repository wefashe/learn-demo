package org.example.controller.rest;

import org.example.domain.dos.AuthDO;
import org.example.service.AuthService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthRestController  extends BaseRestController<AuthService, AuthDO, Long>{
}
