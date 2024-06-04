package org.example.domain.dos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity(name = "t_auth")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AuthDO extends BaseDO<Long> implements Serializable {

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

}
