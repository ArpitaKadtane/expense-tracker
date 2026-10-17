package com.diligent.expense.repository;

import com.diligent.expense.model.Expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByUserUsername(String username, Pageable pageable);

    Page<Expense> findByUserUsernameAndCategoryIgnoreCase(
            String username, String category, Pageable pageable);

    Optional<Expense> findByIdAndUserUsername(Long id, String username);

    @Query("""
            SELECT DISTINCT e.category
            FROM Expense e
            WHERE e.user.username = :username
            ORDER BY e.category ASC
            """)
    List<String> findDistinctCategoriesByUsername(@Param("username") String username);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.username = :username")
    BigDecimal sumAmountByUsername(@Param("username") String username);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.user.username = :username
              AND LOWER(e.category) = LOWER(:category)
            """)
    BigDecimal sumAmountByUsernameAndCategory(
            @Param("username") String username,
            @Param("category") String category);
}
