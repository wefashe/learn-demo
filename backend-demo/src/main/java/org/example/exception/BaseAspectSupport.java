package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Slf4j
public abstract class BaseAspectSupport {

    // @Aspect	把当前类标识为一个切面供容器读取
    // @Pointcut	(切入点):就是带有通知的连接点，在程序中主要体现为书写切入点表达式
    // @Before	标识一个前置增强方法，其不能阻止业务模块的执行，除非抛出异常；
    // @AfterReturning	后置增强，相当于AfterReturningAdvice，方法退出时执行
    // @AfterThrowing	异常抛出增强，相当于ThrowsAdvice
    // @After	方法之后执行，不管是抛出异常或者正常退出都会执行，类似于finally的作用；
    // @Around	环绕增强，方法执行之前，与执行之后均会执行
    // @Around -> @Before -> Method -> @Around -> @After -> @AfterReturning


    public Method resolveMethod(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature)point.getSignature();
        Class<?> targetClass = point.getTarget().getClass();
        Method method = getDeclaredMethod(targetClass, signature.getName(),
                signature.getMethod().getParameterTypes());
        // point.getSignature().getDeclaringTypeName()
        if (method == null) {
            throw new IllegalStateException("无法解析目标方法: " + signature.getMethod().getName());
        }
        return method;
    }

    private Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getDeclaredMethod(superClass, name, parameterTypes);
            }
        }
        return null;
    }


    /**
     * [@Pointcut]：统一切点,对com.example.controller及其子包中所有的类的所有方法切面
     */
    @Pointcut("execution(public * org.example.controller..*.*(..))")
    public void pointcut() {
        log.info("[切面处理] >> 使用注解 @Pointcut 定义切点位置");
    }

    /**
     * [@Before]：前置通知
     */
    @Before("pointcut()")
    public void beforeMethod(JoinPoint joinPoint) {
        log.info("[切面处理] >> 使用注解 @Before 调用了方法前置通知 ");

    }

    /**
     * [@After]：后置通知
     */
    @After("pointcut()")
    public void afterMethod(JoinPoint joinPoint) {
        log.info("[切面处理] >> 使用注解 @After 调用了方法后置通知 ");
    }

    /**
     * [@AfterRunning]：@AfterRunning: 返回通知 rsult为返回内容
     */
    @AfterReturning(value = "pointcut()", returning = "result")
    public void afterReturningMethod(JoinPoint joinPoint, Object result) {
        log.info("[切面处理] >> 使用注解 @AfterReturning 调用了方法返回后通知 ");
    }

    /**
     * [@AfterThrowing]：异常通知
     */
    @AfterThrowing(value = "pointcut()", throwing = "e")
    public void afterThrowingMethod(JoinPoint joinPoint, Exception e) {
        log.info("[切面处理] >> 使用注解 @AfterThrowing 调用了方法异常通知 ");
    }


    /**
     * [@Around]：环绕通知
     */
    @Around("pointcut()")
    public Object aroundMethod(ProceedingJoinPoint pjp) throws Throwable {
        //获取方法名称
        String methodName = pjp.getSignature().getName();
        log.info("[切面处理] >> 使用注解 @Around  方法 ：{} 执行之前 ", methodName);
        Object result = pjp.proceed();
        log.info("[切面处理] >> 使用注解 @Around  方法 ：{} 执行之后 ,返回值:{}", result);
        return result;
    }
}
