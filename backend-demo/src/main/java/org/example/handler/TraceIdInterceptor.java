package org.example.handler;

import org.example.constant.Constants;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.UUID;


@Component
public class TraceIdInterceptor  implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId = request.getHeader(Constants.TRACE_ID);
        if (Objects.isNull(traceId)) {
            traceId = String.valueOf(UUID.randomUUID());
        }
        MDC.put(Constants.TRACE_ID, traceId);
        response.addHeader(Constants.TRACE_ID,traceId);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove("TRACE_ID");
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
