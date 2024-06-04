package org.example.service.impl;

import org.example.dao.RoleRepository;
import org.example.domain.dos.RoleDO;
import org.example.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends BaseServiceImpl<RoleRepository, RoleDO, Long> implements RoleService {
}
