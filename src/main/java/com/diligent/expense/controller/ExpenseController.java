package com.diligent.expense.controller;

import com.diligent.expense.model.Expense;
import com.diligent.expense.service.ExpenseService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @Valid @RequestBody Expense expense,
            Authentication authentication) {

        Expense createdExpense =
                expenseService.createExpense(expense, authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdExpense);
    }

    @GetMapping
    public ResponseEntity<PageResponse<Expense>> getExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        int pageSize = size < 1 ? DEFAULT_PAGE_SIZE : size;
        int pageNumber = Math.max(page, 0);

        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, "date", "id")
        );

        Page<Expense> expensePage;

        if (category != null && !category.isBlank()) {
            expensePage = expenseService.getExpensesByCategory(
                    authentication.getName(), category, pageable);
        } else {
            expensePage = expenseService.getAllExpenses(
                    authentication.getName(), pageable);
        }

        return ResponseEntity.ok(PageResponse.from(expensePage));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(Authentication authentication) {
        return ResponseEntity.ok(expenseService.getCategories(authentication.getName()));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam(required = false) String category,
            Authentication authentication) {

        String username = authentication.getName();

        if (category != null && !category.isBlank()) {

            BigDecimal total =
                    expenseService.getTotalExpensesByCategory(username, category);

            return ResponseEntity.ok(
                    new SummaryResponse(category, total)
            );
        }

        BigDecimal total = expenseService.getTotalExpenses(username);

        return ResponseEntity.ok(
                new SummaryResponse(null, total)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody Expense expense,
            Authentication authentication) {

        Expense updatedExpense =
                expenseService.updateExpense(id, expense, authentication.getName());

        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            Authentication authentication) {

        expenseService.deleteExpense(id, authentication.getName());

        return ResponseEntity.noContent().build();
    }

    public record SummaryResponse(
            String category,
            BigDecimal totalExpenses
    ) {
    }

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
        public static <T> PageResponse<T> from(Page<T> page) {
            return new PageResponse<>(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast()
            );
        }
    }
}
