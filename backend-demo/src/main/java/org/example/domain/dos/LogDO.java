package org.example.domain.dos;

import lombok.Data;

import java.util.Date;

@Data
public class LogDO {

    /**
     * 操作描述
     */
    private String description;

    /**
     * 操作用户
     */
    private String username;

    /**
     * 操作时间
     */
    private Date startTime;

    /**
     * 消耗时间
     */
    private long spendTime;

    /**
     * URL
     */
    private String url;

    /**
     * 请求类型
     */
    private String method;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 请求参数
     */
    private Object parameter;

    /**
     * 请求返回的结果
     */
    private Object result;

    /**
     * 城市
     */
    private String city;

    /**
     * 请求设备信息
     */
    private String device;
}
