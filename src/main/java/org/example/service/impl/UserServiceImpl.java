package org.example.service.impl;

import org.example.dao.UserRepository;
import org.example.domain.UserDTO;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserDTO, Long> implements UserService {

    @Autowired
    private UserRepository repository;

}
