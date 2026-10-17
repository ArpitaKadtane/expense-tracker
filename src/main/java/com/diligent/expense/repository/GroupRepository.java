package com.diligent.expense.repository;

import com.diligent.expense.model.ExpenseGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<ExpenseGroup, Long> {

    @Query("""
            SELECT g
            FROM ExpenseGroup g
            WHERE g.id IN (
                SELECT m.group.id FROM GroupMembership m WHERE m.user.username = :username
            )
            ORDER BY g.name ASC
            """)
    List<ExpenseGroup> findAllForUser(@Param("username") String username);
}
