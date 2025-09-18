package org.example.expert.domain.log.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.entity.Timestamped;
import org.example.expert.domain.log.type.OperationType;
import org.example.expert.domain.log.type.ResultType;

@Entity
@Table(name = "logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Log extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OperationType operation;

    @Column
    private Long actorUserId;

    @Column(length = 500)
    private String requestUri;

    @Lob
    private String requestBody;

    @Column
    private Long targetTodoId;

    @Column
    private Long targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultType result;

    @Column(length = 200)
    private String failureException;

    public static Log create(OperationType operation,
                             Long actorUserId,
                             String requestUri,
                             String requestBody,
                             Long targetTodoId,
                             Long targetUserId,
                             ResultType result,
                             String failureException) {
        return new Log(null, operation, actorUserId, requestUri, requestBody, targetTodoId, targetUserId, result,
                failureException);
    }
}