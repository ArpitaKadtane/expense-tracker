package com.diligent.expense.service;

import com.diligent.expense.model.Expense;
import com.diligent.expense.model.User;
import com.diligent.expense.repository.ExpenseRepository;
import com.diligent.expense.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenses;
    private final UserRepository users;

    public ExpenseService(ExpenseRepository expenses, UserRepository users) {
        this.expenses = expenses;
        this.users = users;
    }

    @Transactional
    public Expense createExpense(Expense expense, String username) {
        User owner = users.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
        expense.setUser(owner);
        return expenses.save(expense);
    }

    @Transactional(readOnly = true)
    public Page<Expense> getAllExpenses(String username, Pageable pageable) {
        return expenses.findByUserUsername(username, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Expense> getExpensesByCategory(String username, String category, Pageable pageable) {
        return expenses.findByUserUsernameAndCategoryIgnoreCase(username, category, pageable);
    }

    @Transactional(readOnly = true)
    public List<String> getCategories(String username) {
        return expenses.findDistinctCategoriesByUsername(username);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenses(String username) {
        BigDecimal total = expenses.sumAmountByUsername(username);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesByCategory(String username, String category) {
        BigDecimal total = expenses.sumAmountByUsernameAndCategory(username, category);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Transactional
    public Expense updateExpense(Long id, Expense expense, String username) {
        Expense existing = expenses.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new ExpenseNotFoundException(id));

        existing.setTitle(expense.getTitle());
        existing.setAmount(expense.getAmount());
        existing.setCategory(expense.getCategory());
        existing.setDate(expense.getDate());

        return expenses.save(existing);
    }

    @Transactional
    public void deleteExpense(Long id, String username) {
        if (!expenses.findByIdAndUserUsername(id, username).isPresent()) {
            throw new ExpenseNotFoundException(id);
        }
        expenses.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean expenseExists(Long id, String username) {
        return expenses.findByIdAndUserUsername(id, username).isPresent();
    }

    @Transactional
    public void clearExpenses() {
        expenses.deleteAll();
    }
}
