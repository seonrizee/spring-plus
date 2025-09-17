package org.example.expert.domain.todo.repository;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.springframework.util.StringUtils.hasText;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.QTodoSearchProjection;
import org.example.expert.domain.todo.dto.TodoSearchProjection;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;


@RequiredArgsConstructor
public class TodoRepositoryCustomImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        return Optional.ofNullable(queryFactory.selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne());
    }

    @Override
    public Page<TodoSearchProjection> findAllBySearch(String titleKeyword,
                                                      LocalDate startDate,
                                                      LocalDate endDate,
                                                      String nicknameKeyword,
                                                      Pageable pageable) {

        JPAQuery<TodoSearchProjection> query = queryFactory
                .select(new QTodoSearchProjection(
                        todo.title,
                        todo.managers.size().intValue(),
                        todo.comments.size().intValue()
                ))
                .from(todo);

        applyJoinsForNickname(query, nicknameKeyword);

        List<TodoSearchProjection> content = query
                .where(
                        titleCIC(titleKeyword),
                        startDateGoe(startDate),
                        endDateLoe(endDate),
                        nicknameCic(nicknameKeyword)
                )
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(todo.id.countDistinct())
                .from(todo);

        applyJoinsForNickname(countQuery, nicknameKeyword);

        countQuery.where(
                titleCIC(titleKeyword),
                startDateGoe(startDate),
                endDateLoe(endDate),
                nicknameCic(nicknameKeyword));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression titleCIC(String title) {
        return hasText(title) ? todo.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression startDateGoe(LocalDate startDate) {
        return startDate != null ? todo.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression endDateLoe(LocalDate endDate) {
        return endDate != null ? todo.createdAt.lt(endDate.plusDays(1).atStartOfDay()) : null;
    }

    private BooleanExpression nicknameCic(String nickname) {
        return hasText(nickname) ? todo.managers.any().user.nickname.containsIgnoreCase(nickname) : null;
    }

    private <T> JPAQuery<T> applyJoinsForNickname(JPAQuery<T> query, String nickname) {
        if (hasText(nickname)) {
            query.leftJoin(todo.managers, QManager.manager)
                    .leftJoin(QManager.manager.user, QUser.user);
        }
        return query;
    }
}
