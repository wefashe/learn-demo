package org.example.service.impl;

import org.example.dao.UserRepository;
import org.example.domain.dos.UserDO;
import org.example.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserRepository, UserDO, Long> implements UserService {

    @Override
    public UserDO login(UserDO userDO) {
        return repository.findByUsernameAndPassword(userDO.getUsername(), userDO.getPassword());
    }

}
