package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.example.expert.domain.log.type.OperationType;
import org.example.expert.domain.log.type.ResultType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(OperationType operation,
                     Long actorUserId,
                     String requestUri,
                     String requestBody,
                     Long targetTodoId,
                     Long targetUserId,
                     ResultType result,
                     String failureException) {
        Log log = Log.create(
                operation,
                actorUserId,
                requestUri,
                requestBody,
                targetTodoId,
                targetUserId,
                result,
                failureException
        );
        logRepository.save(log);
    }
}