package com.diligent.expense.controller;

import com.diligent.expense.model.Expense;
import com.diligent.expense.service.ExpenseService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @Valid @RequestBody Expense expense) {

        Expense createdExpense = expenseService.createExpense(expense);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdExpense);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestParam(required = false) String category) {

        if (category != null && !category.isBlank()) {

            return ResponseEntity.ok(
                    expenseService.getExpensesByCategory(category)
            );
        }

        return ResponseEntity.ok(
                expenseService.getAllExpenses()
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam(required = false) String category) {

        if (category != null && !category.isBlank()) {

            BigDecimal total =
                    expenseService.getTotalExpensesByCategory(category);

            return ResponseEntity.ok(
                    new SummaryResponse(category, total)
            );
        }

        BigDecimal total = expenseService.getTotalExpenses();

        return ResponseEntity.ok(
                new SummaryResponse(null, total)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {

        if (!expenseService.expenseExists(id)) {

            return ResponseEntity.notFound().build();
        }

        expenseService.deleteExpense(id);

        return ResponseEntity.noContent().build();
    }

    public record SummaryResponse(
            String category,
            BigDecimal totalExpenses
    ) {
    }
}