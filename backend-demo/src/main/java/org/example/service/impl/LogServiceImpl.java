package org.example.service.impl;

import org.example.dao.LogRepository;
import org.example.domain.dos.LogDO;
import org.example.service.LogService;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl extends BaseServiceImpl<LogRepository, LogDO, Long> implements LogService {
}
