package org.example.expert.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.log.service.LogService;
import org.example.expert.domain.log.type.OperationType;
import org.example.expert.domain.log.type.ResultType;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ManagerRegisterLoggingAspect {

    private final LogService logService;
    private final ObjectMapper objectMapper;

    @Pointcut("execution(* org.example.expert.domain.manager.service.ManagerService.saveManager(..))")
    private void saveManagerPointcut() {
    }

    @Around(value = "saveManagerPointcut() && args(authUser, todoId, managerSaveRequest)",
            argNames = "pjp,authUser,todoId,managerSaveRequest")
    public Object logManagerRegister(ProceedingJoinPoint pjp,
                                     AuthUser authUser,
                                     long todoId,
                                     ManagerSaveRequest managerSaveRequest) throws Throwable {

        String uri = null;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            if (request != null && request.getRequestURI() != null) {
                uri = request.getRequestURI();
            }
        }

        String bodyJson = saveJson(managerSaveRequest);
        Long actorUserId = authUser != null ? authUser.id() : null;
        Long targetUserId = managerSaveRequest != null ? managerSaveRequest.getManagerUserId() : null;

        try {
            Object result = pjp.proceed();
            callSaveLog(OperationType.MANAGER_REGISTER, actorUserId, uri, bodyJson, todoId, targetUserId, null);
            return result;
        } catch (Throwable t) {
            callSaveLog(OperationType.MANAGER_REGISTER, actorUserId, uri, bodyJson, todoId, targetUserId, t);
            throw t;
        }
    }

    private String saveJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }

    private void callSaveLog(OperationType operationType, Long actorUserId, String uri, String bodyJson, Long todoId,
                             Long targetUserId, Throwable t) {
        try {
            log.info(
                    "Admin Access Logging start: operationType={}, actorUserId={}, uri={}, body={}, todoId={}, targetUserId={}, result={}, error={}",
                    operationType,
                    actorUserId,
                    uri,
                    bodyJson,
                    todoId,
                    targetUserId,
                    t == null ? ResultType.SUCCESS : ResultType.FAILURE,
                    t != null ? t.getClass().getSimpleName() : null
            );
            logService.save(
                    operationType,
                    actorUserId,
                    uri,
                    bodyJson,
                    todoId,
                    targetUserId,
                    t == null ? ResultType.SUCCESS : ResultType.FAILURE,
                    t != null ? t.getClass().getSimpleName() : null
            );
        } catch (Exception e) {
            log.warn("Admin Access Logging failed: {}", e.getMessage());
        }
    }
}
