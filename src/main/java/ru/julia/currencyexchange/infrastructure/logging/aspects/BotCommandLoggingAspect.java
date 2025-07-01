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
public class BotCommandLoggingAspect extends AbstractLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(BotCommandLoggingAspect.class);
    private static final long SLOW_THRESHOLD_MS = 800;

    private static final String SLOW_MSG = "[traceId={}] [user={}] Slow bot command execution: {} took {} ms";
    private static final String NULL_MSG = "[traceId={}] [user={}] Bot command method {} returned null";
    private static final String ERROR_MSG = "[traceId={}] [user={}] Exception in bot command {} after {} ms: {}";
    private static final String COMPLETED_MSG = "[traceId={}] [user={}] Completed: {} in {} ms, result: {}";
    private static final String DEBUG_EXIT_MSG = "[traceId={}] [user={}] Exiting: {} with result type: {}, value: {}";

    @Around("within(ru.julia.currencyexchange.infrastructure.bot.command..*) || " +
            "within(ru.julia.currencyexchange.infrastructure.bot.command.handler..*) || " +
            "within(ru.julia.currencyexchange.infrastructure.bot.command.builder..*)")
    public Object logBotCommandMethod(ProceedingJoinPoint joinPoint) throws Throwable {
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