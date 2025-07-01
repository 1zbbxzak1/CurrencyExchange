package ru.julia.currencyexchange.infrastructure.logging.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.infrastructure.logging.aspects.abstracts.AbstractLoggingAspect;

import java.util.Arrays;

@Aspect
@Component
public class GlobalExceptionLoggingAspect extends AbstractLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionLoggingAspect.class);

    @AfterThrowing(pointcut = "@within(org.springframework.web.bind.annotation.RestControllerAdvice)", throwing = "ex")
    public void logGlobalException(JoinPoint joinPoint, Throwable ex) {
        String traceId = getOrCreateTraceId();
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().toShortString();
        String argsString = Arrays.toString(joinPoint.getArgs());
        logger.error("[traceId={}] [user={}] Exception in global handler {} with arguments: {}. Exception: {}", traceId, username, methodName, argsString, ex.getMessage(), ex);
    }
}