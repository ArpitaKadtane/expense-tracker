package com.diligent.expense.service;

import com.diligent.expense.model.Expense;
import com.diligent.expense.repository.ExpenseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenses;

    public ExpenseService(ExpenseRepository expenses) {
        this.expenses = expenses;
    }

    @Transactional
    public Expense createExpense(Expense expense) {
        return expenses.save(expense);
    }

    @Transactional(readOnly = true)
    public Page<Expense> getAllExpenses(Pageable pageable) {
        return expenses.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Expense> getExpensesByCategory(String category, Pageable pageable) {
        return expenses.findByCategoryIgnoreCase(category, pageable);
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return expenses.findDistinctCategories();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenses() {
        BigDecimal total = expenses.sumAmount();
        return total == null ? BigDecimal.ZERO : total;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesByCategory(String category) {
        BigDecimal total = expenses.sumAmountByCategory(category);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Transactional
    public Expense updateExpense(Long id, Expense expense) {
        Expense existing = expenses.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));

        existing.setTitle(expense.getTitle());
        existing.setAmount(expense.getAmount());
        existing.setCategory(expense.getCategory());
        existing.setDate(expense.getDate());

        return expenses.save(existing);
    }

    @Transactional
    public void deleteExpense(Long id) {
        if (!expenses.existsById(id)) {
            throw new ExpenseNotFoundException(id);
        }
        expenses.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean expenseExists(Long id) {
        return expenses.existsById(id);
    }

    @Transactional
    public void clearExpenses() {
        expenses.deleteAll();
    }
}
