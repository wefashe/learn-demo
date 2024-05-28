package org.example.config;

import org.example.domain.dos.UserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.Optional;

@Configuration //使用jpa审计功能，保存数据时自动插入创建人id和更新人id
public class UserAuditorAware implements AuditorAware<Long> {

    @Autowired
    private HttpSession session;

    @Override
    public Optional<Long> getCurrentAuditor() {
        //从session中获取当前登录用户的id
        UserDO userDO = (UserDO) session.getAttribute("user");
        if (Objects.nonNull(userDO)){
            return Optional.of(userDO.getId());
        } else {
            return Optional.empty();
        }
    }
}
