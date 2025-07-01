package ru.julia.currencyexchange.infrastructure.logging.aspects.abstracts;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.julia.currencyexchange.infrastructure.logging.tracing.TraceIdGenerator;

import java.util.Arrays;
import java.util.stream.IntStream;

public abstract class AbstractLoggingAspect {
    protected String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getName();
            }
        } catch (Exception ignored) {
        }
        return "anonymous";
    }

    protected String formatArguments(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        if (paramNames == null || args.length != paramNames.length) {
            return Arrays.toString(args);
        }
        return IntStream.range(0, args.length)
                .mapToObj(i -> paramNames[i] + "=" + args[i])
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    protected void logCompletion(Logger logger, String traceId, String username, String methodName, String argsString, Object result, Throwable throwable, long duration, long slowThresholdMs, String slowMsg, String nullMsg, String errorMsg, String completedMsg, String debugExitMsg) {
        if (duration > slowThresholdMs) {
            logger.warn(slowMsg, traceId, username, methodName, duration);
        }
        if (result == null && throwable == null) {
            logger.warn(nullMsg, traceId, username, methodName);
        }
        if (throwable != null) {
            logger.error(errorMsg, traceId, username, methodName, duration, throwable.getMessage(), throwable);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(debugExitMsg, traceId, username, methodName, (result != null ? result.getClass().getName() : "null"), formatResult(result));
            }
            logger.info(completedMsg, traceId, username, methodName, duration, formatResult(result));
        }
    }

    protected String formatResult(Object result) {
        if (result == null) return "null";
        String str = result.toString();
        return str.length() > 500 ? str.substring(0, 500) + "..." : str;
    }

    protected String getOrCreateTraceId() {
        return TraceIdGenerator.getOrCreateTraceId();
    }
} 