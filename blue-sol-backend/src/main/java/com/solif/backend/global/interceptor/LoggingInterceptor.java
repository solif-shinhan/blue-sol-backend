package com.solif.backend.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.StringJoiner;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());

        log.info("[REQUEST] {} {}", request.getMethod(), request.getRequestURI());

        Map<String, String[]> params = request.getParameterMap();
        if (!params.isEmpty()) {
            StringJoiner joiner = new StringJoiner(",");
            params.keySet().forEach(joiner::add);
            log.info("[QUERY_KEYS] {}", joiner);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME);
        if (startTime != null) {
            long executeTime = System.currentTimeMillis() - startTime;
            log.info("[RESPONSE] status={} time={}ms", response.getStatus(), executeTime);
        } else {
            log.info("[RESPONSE] status={}", response.getStatus());
        }

        if (ex != null) {
            log.error("[EXCEPTION]", ex);
        }
    }
}
