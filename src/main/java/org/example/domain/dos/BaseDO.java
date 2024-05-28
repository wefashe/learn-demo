package org.example.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.example.domain.groups.Update;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@DynamicInsert
@DynamicUpdate // 只更新插入存在值的字段
@MappedSuperclass//这个注解的意思是这个类jpa不会为它创建数据库表，
@EntityListeners(AuditingEntityListener.class)//对实体属性变化的跟踪，它提供了保存前，保存后，更新前，
// 更新后，删除前，删除后等状态，就像是拦截器一样，你可以在拦截方法里重写你的个性化逻辑。
public abstract class BaseDO implements Serializable {

    @NotNull(message = "id不能为空", groups = Update.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    protected Long id;

    @JsonIgnore
    @CreatedBy
    @Column(nullable = false, updatable = false)
    protected Long createBy;

    @JsonIgnore
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP) // Java的Date对象转换为指定格式的数据库Date类型
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 将Java的Date对象转换为指定格式的Json数据
    // @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 指定格式的Json字符串数据转换为Java的Date对象
    @Column(nullable = false, updatable = false)
    protected Date createTime;

    @JsonIgnore
    @LastModifiedBy
    @Column(nullable = false)
    protected Long updateBy;

    @JsonIgnore
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    protected Date updateTime;
}
