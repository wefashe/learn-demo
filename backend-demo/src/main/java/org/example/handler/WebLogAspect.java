package org.example.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.domain.dos.UserDO;
import org.example.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Objects;

@Order
@Slf4j
@Aspect
@Component
public class WebLogAspect extends BaseAspectSupport {

    @Autowired
    private ObjectMapper mapper;

    @Pointcut("execution(public * org.example.controller..*.*(..))")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String requestURL = request.getRequestURL().toString();
        String requestMethod = request.getMethod();
        String requestParam = mapper.writeValueAsString(request.getParameterMap());
        UserDO userDO = (UserDO) request.getSession().getAttribute("user");
        String userId = Objects.nonNull(userDO) ? String.valueOf(userDO.getId()) : "";
        String ip = IpUtil.getIpAddr(request);
        String responseData =  mapper.writeValueAsString(result);
        if (log.isDebugEnabled()) {
            log.debug("--------------------start--------------------");
            log.debug("请求用户: [{}]", userId);
            log.debug("请求地址：[{}]", requestURL);
            log.debug("请求方式: [{}]" , requestMethod);
            log.debug("请求参数: [{}]" , requestParam);
            log.debug("返回结果: [{}]" , responseData);
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.debug("开始时间: [{}]" , dateformat.format(start));
            log.debug("耗费时间: [{}]" , (end - start) + "ms");
            log.debug("IP ADDR：[{}]", ip);
            log.debug("--------------------end----------------------");
        }
        return result;
    }

}
