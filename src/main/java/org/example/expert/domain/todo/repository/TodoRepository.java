package org.example.expert.domain.todo.repository;

import java.time.LocalDate;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    @Query("""
                    SELECT t FROM Todo t
                    LEFT JOIN FETCH t.user u
                    WHERE (:weather IS NULL OR LOWER(t.weather) LIKE LOWER(CONCAT('%', :weather, '%')))
                    AND (:startDate IS NULL OR t.modifiedAt >= :startDate)
                    AND (:endDate IS NULL OR t.modifiedAt <= :endDate)
                    ORDER BY t.modifiedAt DESC
            """)
    Page<Todo> findAllWithConditions(String weather, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
