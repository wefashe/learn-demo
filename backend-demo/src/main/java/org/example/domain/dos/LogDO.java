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
@Entity(name = "t_log")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LogDO extends BaseDO<Long> implements Serializable {

    @Column(nullable = false)
    private Long userId;

}
