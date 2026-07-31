package com.diligent.expense;

import com.diligent.expense.service.ExpenseService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService.clearExpenses();
    }

    @Test
    void shouldCreateExpense() throws Exception {

        String request = """
                {
                    "title": "Grocery Shopping",
                    "amount": 1250.50,
                    "category": "Food",
                    "date": "2026-07-31"
                }
                """;

        mockMvc.perform(
                post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.title").value("Grocery Shopping"))
        .andExpect(jsonPath("$.amount").value(1250.50))
        .andExpect(jsonPath("$.category").value("Food"))
        .andExpect(jsonPath("$.date").value("2026-07-31"));
    }

    @Test
    void shouldGetAllExpenses() throws Exception {

        createExpense(
                "Grocery",
                "100.00",
                "Food",
                "2026-07-31"
        );

        createExpense(
                "Bus Ticket",
                "50.00",
                "Transport",
                "2026-07-30"
        );

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldFilterExpensesByCategory() throws Exception {

        createExpense(
                "Grocery",
                "100.00",
                "Food",
                "2026-07-31"
        );

        createExpense(
                "Dinner",
                "200.00",
                "Food",
                "2026-07-30"
        );

        createExpense(
                "Bus Ticket",
                "50.00",
                "Transport",
                "2026-07-29"
        );

        mockMvc.perform(
                get("/api/expenses")
                        .param("category", "Food")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldCalculateOverallTotal() throws Exception {

        createExpense(
                "Grocery",
                "100.00",
                "Food",
                "2026-07-31"
        );

        createExpense(
                "Bus",
                "50.00",
                "Transport",
                "2026-07-30"
        );

        createExpense(
                "Dinner",
                "150.00",
                "Food",
                "2026-07-29"
        );

        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(300.00));
    }

    @Test
    void shouldCalculateCategoryTotal() throws Exception {

        createExpense(
                "Grocery",
                "100.00",
                "Food",
                "2026-07-31"
        );

        createExpense(
                "Dinner",
                "150.00",
                "Food",
                "2026-07-30"
        );

        createExpense(
                "Bus",
                "50.00",
                "Transport",
                "2026-07-29"
        );

        mockMvc.perform(
                get("/api/expenses/summary")
                        .param("category", "Food")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.category").value("Food"))
        .andExpect(jsonPath("$.totalExpenses").value(250.00));
    }

    @Test
    void shouldDeleteExpense() throws Exception {

        createExpense(
                "Grocery",
                "100.00",
                "Food",
                "2026-07-31"
        );

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingExpense()
            throws Exception {

        mockMvc.perform(delete("/api/expenses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidExpense() throws Exception {

        String request = """
                {
                    "title": "",
                    "amount": -100,
                    "category": "",
                    "date": null
                }
                """;

        mockMvc.perform(
                post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors.title")
                .value("Title is required"))
        .andExpect(jsonPath("$.errors.amount")
                .value("Amount must be greater than zero"))
        .andExpect(jsonPath("$.errors.category")
                .value("Category is required"))
        .andExpect(jsonPath("$.errors.date")
                .value("Date is required"));
    }

    private void createExpense(
            String title,
            String amount,
            String category,
            String date) throws Exception {

        String request = """
                {
                    "title": "%s",
                    "amount": %s,
                    "category": "%s",
                    "date": "%s"
                }
                """.formatted(
                title,
                amount,
                category,
                date
        );

        mockMvc.perform(
                post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isCreated());
    }
}
