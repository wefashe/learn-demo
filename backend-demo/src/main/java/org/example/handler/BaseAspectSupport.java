package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
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
    // @Around -> @Before -> Method -> @AfterReturning -> @After -> @Around


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
}
