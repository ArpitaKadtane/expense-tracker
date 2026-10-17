package com.diligent.expense.repository;

import com.diligent.expense.model.ExpenseShare;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {

    List<ExpenseShare> findByGroupExpenseId(Long groupExpenseId);

    void deleteByGroupExpenseId(Long groupExpenseId);

    @Query("""
            SELECT s.user.username, COALESCE(SUM(s.amount), 0)
            FROM ExpenseShare s
            WHERE s.groupExpense.group.id = :groupId
            GROUP BY s.user.username
            """)
    List<Object[]> sumOwedByUser(@Param("groupId") Long groupId);
}
