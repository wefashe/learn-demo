package org.example.controller.rest;

import org.example.domain.dos.LogDO;
import org.example.service.LogService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("log")
public class LogRestController  extends BaseRestController<LogService, LogDO, Long>{
}
