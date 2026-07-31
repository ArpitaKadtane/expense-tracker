// package com.diligent.expense.service;

// public class ExpenseService {
    
// }

package com.diligent.expense.service;

import com.diligent.expense.model.Expense;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ExpenseService {

    private final Map<Long, Expense> expenses = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(0);

    public Expense createExpense(Expense expense) {

        Long id = idGenerator.incrementAndGet();

        expense.setId(id);

        expenses.put(id, expense);

        return expense;
    }

    public List<Expense> getAllExpenses() {

        return new ArrayList<>(expenses.values());
    }

    public List<Expense> getExpensesByCategory(String category) {

        return expenses.values()
                .stream()
                .filter(expense ->
                        expense.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    public BigDecimal getTotalExpenses() {

        return expenses.values()
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpensesByCategory(String category) {

        return expenses.values()
                .stream()
                .filter(expense ->
                        expense.getCategory().equalsIgnoreCase(category))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void deleteExpense(Long id) {

        expenses.remove(id);
    }

    public boolean expenseExists(Long id) {

        return expenses.containsKey(id);
    }

public void clearExpenses() {
    expenses.clear();
    idGenerator.set(0);
}
}