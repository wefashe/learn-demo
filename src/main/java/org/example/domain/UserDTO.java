package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class UserDTO extends BaseDTO {

    @Column
    @Comment("昵称")
    private String nickName;

    @Column
    @Comment("账户")
    private String username;

    @Column
    @Comment("密码")
    private String password;

    @Column
    @Comment("邮箱")
    private String email;
}
