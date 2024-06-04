package org.example.controller.rest;

import org.example.domain.dos.RoleDO;
import org.example.service.RoleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("role")
public class RoleRestController  extends BaseRestController<RoleService, RoleDO, Long>{
}
