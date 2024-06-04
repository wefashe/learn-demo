package org.example.service.impl;

import org.example.dao.AuthRepository;
import org.example.domain.dos.AuthDO;
import org.example.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl extends BaseServiceImpl<AuthRepository, AuthDO, Long> implements AuthService {
}
