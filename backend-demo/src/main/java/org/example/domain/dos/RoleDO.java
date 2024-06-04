package org.example.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity(name = "t_role")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RoleDO extends BaseDO<Long> implements Serializable {

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "t_role_auth", joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "auth_id"))
    private Set<AuthDO> auths;

}
