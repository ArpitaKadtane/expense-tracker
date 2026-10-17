package com.diligent.expense.repository;

import com.diligent.expense.model.GroupExpense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupExpenseRepository extends JpaRepository<GroupExpense, Long> {

    List<GroupExpense> findByGroupIdOrderByDateDescIdDesc(Long groupId);

    Optional<GroupExpense> findByIdAndGroupId(Long id, Long groupId);

    @Query("""
            SELECT e.paidBy.username, COALESCE(SUM(e.amount), 0)
            FROM GroupExpense e
            WHERE e.group.id = :groupId
            GROUP BY e.paidBy.username
            """)
    List<Object[]> sumPaidByUser(@Param("groupId") Long groupId);
}
