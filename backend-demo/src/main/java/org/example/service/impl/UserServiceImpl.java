package org.example.service.impl;

import org.example.dao.UserRepository;
import org.example.domain.dos.UserDO;
import org.example.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserRepository, UserDO, Long> implements UserService {

    @Override
    public UserDO findById(Long aLong) {
        UserDO userDO = super.findById(aLong);
        userDO.setPassword(null);
        return userDO;
    }

    @Override
    public Iterable<UserDO> findById(Iterable<Long> ids) {
        List<UserDO> userDOS = (List<UserDO>) super.findById(ids);
        for (UserDO userDO : userDOS) {
            userDO.setPassword(null);
        }
        return userDOS;
    }

    @Override
    public List<UserDO> findAll() {
        List<UserDO> userDOS = (List<UserDO>) super.findAll();
        for (UserDO userDO : userDOS) {
            userDO.setPassword(null);
        }
        return userDOS;
    }

    @Override
    public UserDO add(UserDO entity) {
        if (Objects.nonNull(entity) && Objects.nonNull(entity.getPassword())
                && entity.getPassword().length() > 0) {
            // 密码加密
            entity.setPassword(DigestUtils.md5DigestAsHex(entity.getPassword().getBytes()));
        }
        return super.add(entity);
    }

    @Override
    public UserDO update(UserDO entity) {
        if (Objects.nonNull(entity) && Objects.nonNull(entity.getPassword())
                && entity.getPassword().length() > 0) {
            // 密码加密
            entity.setPassword(DigestUtils.md5DigestAsHex(entity.getPassword().getBytes()));
        }
        return super.update(entity);
    }

    @Override
    public <S extends UserDO> Iterable<S> update(Iterable<S> entities) {
        List<UserDO> userDOS = (List<UserDO>) entities;
        for (UserDO userDO : userDOS) {
            if (Objects.nonNull(userDO) && Objects.nonNull(userDO.getPassword())
                    && userDO.getPassword().length() > 0) {
                // 密码加密
                userDO.setPassword(DigestUtils.md5DigestAsHex(userDO.getPassword().getBytes()));
            }
        }
        return super.update(entities);
    }

    @Override
    public <S extends UserDO> Iterable<S> add(Iterable<S> entities) {
        List<UserDO> userDOS = (List<UserDO>) entities;
        for (UserDO userDO : userDOS) {
            if (Objects.nonNull(userDO) && Objects.nonNull(userDO.getPassword())
                    && userDO.getPassword().length() > 0) {
                // 密码加密
                userDO.setPassword(DigestUtils.md5DigestAsHex(userDO.getPassword().getBytes()));
            }
        }
        return super.add(entities);
    }

    @Override
    public UserDO login(UserDO userVO) {
        if (userVO != null && Objects.nonNull(userVO.getPassword())) {
            userVO.setPassword(DigestUtils.md5DigestAsHex(userVO.getPassword().getBytes()));
        }
        UserDO userDO = repository.findByUsernameAndPassword(userVO.getUsername(), userVO.getPassword());
        if (Objects.nonNull(userDO)) {
            userDO.setPassword(null);
        }
        return userDO;
    }

    // 重置密码

    // 修改密码
}
