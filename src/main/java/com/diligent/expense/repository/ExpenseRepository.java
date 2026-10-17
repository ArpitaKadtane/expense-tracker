package com.diligent.expense.repository;

import com.diligent.expense.model.Expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByCategoryIgnoreCase(String category, Pageable pageable);

    @Query("SELECT DISTINCT e.category FROM Expense e ORDER BY e.category ASC")
    List<String> findDistinctCategories();

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumAmount();

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE LOWER(e.category) = LOWER(:category)
            """)
    BigDecimal sumAmountByCategory(@Param("category") String category);
}
