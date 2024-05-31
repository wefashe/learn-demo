package org.example.exception;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.domain.dos.LogDO;
import org.example.util.IpUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Order
@Aspect
@Component
public class LogAspectSupport extends BaseAspectSupport{

    @Pointcut("execution(public * cn.soboys.mallapi.controller.*.*(..))")
    public void log() {

    }

    @Around("log()")

    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        LogDO logSubject = new LogDO();
        //记录时间定时器
        long start = System.currentTimeMillis();
        //执行结果
        Object result = joinPoint.proceed();
        logSubject.setResult(result);
        //执行消耗时间
        long end = System.currentTimeMillis();
        logSubject.setSpendTime(end - start);
        //执行参数
        Method method = resolveMethod(joinPoint);
        logSubject.setParameter(getParameter(method, joinPoint.getArgs()));
        // 接口请求时间
        logSubject.setStartTime(new Date());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request =  Objects.requireNonNull(attributes).getRequest();
        //请求链接
        logSubject.setUrl(request.getRequestURL().toString());
        //请求方法GET,POST等
        logSubject.setMethod(request.getMethod());

        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        //获取浏览器对象
        Browser browser = userAgent.getBrowser();
        //获取操作系统对象
        OperatingSystem operatingSystem = userAgent.getOperatingSystem();
        System.out.println("浏览器名:"+browser.getName());
        System.out.println("浏览器类型:"+browser.getBrowserType());
        System.out.println("浏览器家族:"+browser.getGroup());
        System.out.println("浏览器生产厂商:"+browser.getManufacturer());
        System.out.println("浏览器使用的渲染引擎:"+browser.getRenderingEngine());
        System.out.println("浏览器版本:"+userAgent.getBrowserVersion());

        System.out.println("操作系统名:"+operatingSystem.getName());
        System.out.println("访问设备类型:"+operatingSystem.getDeviceType());
        System.out.println("操作系统家族:"+operatingSystem.getGroup());
        System.out.println("操作系统生产厂商:"+operatingSystem.getManufacturer());
        //请求设备信息
        logSubject.setDevice(operatingSystem.getDeviceType().getName());
        //请求地址
        logSubject.setIp(IpUtil.getIpAddr(request));
        //接口描述
        // if (method.isAnnotationPresent(ApiOperation.class)) {
        //     ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        //     logSubject.setDescription(apiOperation.value());
        // }
        return result;
    }


    /**

     * 根据方法和传入的参数获取请求参数

     */

    private Object getParameter(Method method, Object[] args) {

        List<Object> argList = new ArrayList<>();

        Parameter[] parameters = method.getParameters();

        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < parameters.length; i++) {

            //将RequestBody注解修饰的参数作为请求参数

            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);

            //将RequestParam注解修饰的参数作为请求参数

            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);

            String key = parameters[i].getName();

            if (requestBody != null) {

                argList.add(args[i]);

            } else if (requestParam != null) {

                map.put(key, args[i]);

            } else {

                map.put(key, args[i]);

            }

        }

        if (map.size() > 0) {

            argList.add(map);

        }

        if (argList.size() == 0) {

            return null;

        } else if (argList.size() == 1) {

            return argList.get(0);

        } else {

            return argList;

        }

    }


}
