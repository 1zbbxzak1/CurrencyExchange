package ru.julia.currencyexchange.infrastructure.logging.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.infrastructure.logging.aspects.abstracts.AbstractLoggingAspect;

@Aspect
@Component
public class ControllerLoggingAspect extends AbstractLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ControllerLoggingAspect.class);
    private static final long SLOW_THRESHOLD_MS = 1000;

    private static final String SLOW_MSG = "[traceId={}] [user={}] Slow controller execution: {} took {} ms";
    private static final String NULL_MSG = "[traceId={}] [user={}] Controller method {} returned null";
    private static final String ERROR_MSG = "[traceId={}] [user={}] Exception in controller {} after {} ms: {}";
    private static final String COMPLETED_MSG = "[traceId={}] [user={}] Completed: {} in {} ms, result: {}";
    private static final String DEBUG_EXIT_MSG = "[traceId={}] [user={}] Exiting: {} with result type: {}, value: {}";

    @Around("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = getOrCreateTraceId();
        boolean isTraceIdNew = !traceId.equals(MDC.get("traceId"));
        long start = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().toShortString();
        String username = getCurrentUsername();
        String argsString = formatArguments(joinPoint);

        if (logger.isDebugEnabled()) {
            logger.debug("[traceId={}] [user={}] Entering: {} with arguments: {}", traceId, username, methodName, argsString);
        }
        logger.info("[traceId={}] [user={}] Invoked: {} with arguments: {}", traceId, username, methodName, argsString);

        Object result = null;
        Throwable throwable = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            logCompletion(logger, traceId, username, methodName, argsString, result, throwable, duration,
                    SLOW_THRESHOLD_MS, SLOW_MSG, NULL_MSG, ERROR_MSG, COMPLETED_MSG, DEBUG_EXIT_MSG);
            if (isTraceIdNew) {
                MDC.remove("traceId");
            }
        }
    }
} 