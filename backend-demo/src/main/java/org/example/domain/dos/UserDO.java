package org.example.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.groups.Login;
import org.example.domain.groups.Update;
import org.example.domain.groups.register;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate // 只更新插入存在值的字段
@Entity(name = "t_user") // user表名在h2中是关键字
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserDO extends BaseDO<Long> implements Serializable {

    @Column(nullable = false)
    private String nickName;

    @NotEmpty(message = "登录账号不能为空")
    @Length(min = 4, max = 16, message = "账号长度为 4-16 位")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "账号格式为数字以及字母")
    @Column(unique = true, nullable = false)
    private String username;

    @NotEmpty(message = "密码不能为空" , groups = {register.class, Login.class, Update.class})
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位" , groups = {register.class, Login.class, Update.class})
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "联系邮箱不能为空", groups = {register.class, Login.class, Update.class})
    @Email(message = "邮箱格式不对", groups = {register.class, Login.class, Update.class})
    @Column(nullable = false)
    private String email;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "t_user_role", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleDO> roles;
}
