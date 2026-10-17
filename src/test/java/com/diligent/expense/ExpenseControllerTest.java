package com.diligent.expense;

import com.diligent.expense.repository.UserRepository;
import com.diligent.expense.service.ExpenseService;

import com.jayway.jsonpath.JsonPath;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    private static final String AUTHORIZATION = "Authorization";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        expenseService.clearExpenses();
        userRepository.deleteAll();
        token = registerAndGetToken("tester");
    }

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authBody("tester", "password123"))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.username").value("tester"));
    }

    @Test
    void shouldRejectLoginWithWrongPassword() throws Exception {
        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authBody("tester", "wrong-password"))
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectDuplicateUsername() throws Exception {
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authBody("tester", "password123"))
        )
        .andExpect(status().isConflict());
    }

    // ------------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------------

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
                        .header(AUTHORIZATION, bearer())
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

        createExpense("Grocery", "100.00", "Food", "2026-07-31");
        createExpense("Bus Ticket", "50.00", "Transport", "2026-07-30");

        mockMvc.perform(get("/api/expenses").header(AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldFilterExpensesByCategory() throws Exception {

        createExpense("Grocery", "100.00", "Food", "2026-07-31");
        createExpense("Dinner", "200.00", "Food", "2026-07-30");
        createExpense("Bus Ticket", "50.00", "Transport", "2026-07-29");

        mockMvc.perform(
                get("/api/expenses")
                        .header(AUTHORIZATION, bearer())
                        .param("category", "Food")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldPaginateExpenses() throws Exception {

        for (int i = 1; i <= 5; i++) {
            createExpense(
                    "Expense " + i,
                    String.valueOf(i * 10) + ".00",
                    "Food",
                    String.format("2026-07-%02d", i)
            );
        }

        mockMvc.perform(
                get("/api/expenses")
                        .header(AUTHORIZATION, bearer())
                        .param("page", "0")
                        .param("size", "2")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(5))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.first").value(true))
        .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(
                get("/api/expenses")
                        .header(AUTHORIZATION, bearer())
                        .param("page", "2")
                        .param("size", "2")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldReturnDistinctCategories() throws Exception {

        createExpense("Grocery", "100.00", "Food", "2026-07-31");
        createExpense("Dinner", "150.00", "Food", "2026-07-30");
        createExpense("Bus", "50.00", "Transport", "2026-07-29");

        mockMvc.perform(get("/api/expenses/categories").header(AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value("Food"))
                .andExpect(jsonPath("$[1]").value("Transport"));
    }

    @Test
    void shouldCalculateOverallTotal() throws Exception {

        createExpense("Grocery", "100.00", "Food", "2026-07-31");
        createExpense("Bus", "50.00", "Transport", "2026-07-30");
        createExpense("Dinner", "150.00", "Food", "2026-07-29");

        mockMvc.perform(get("/api/expenses/summary").header(AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(300.00));
    }

    @Test
    void shouldCalculateCategoryTotal() throws Exception {

        createExpense("Grocery", "100.00", "Food", "2026-07-31");
        createExpense("Dinner", "150.00", "Food", "2026-07-30");
        createExpense("Bus", "50.00", "Transport", "2026-07-29");

        mockMvc.perform(
                get("/api/expenses/summary")
                        .header(AUTHORIZATION, bearer())
                        .param("category", "Food")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.category").value("Food"))
        .andExpect(jsonPath("$.totalExpenses").value(250.00));
    }

    @Test
    void shouldUpdateExpense() throws Exception {

        long id = createExpense(
                "Grocery", "100.00", "Food", "2026-07-31"
        );

        String updateRequest = """
                {
                    "title": "Groceries Updated",
                    "amount": "250.00",
                    "category": "Groceries",
                    "date": "2026-08-01"
                }
                """;

        mockMvc.perform(
                put("/api/expenses/" + id)
                        .header(AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.title").value("Groceries Updated"))
        .andExpect(jsonPath("$.amount").value(250.00))
        .andExpect(jsonPath("$.category").value("Groceries"))
        .andExpect(jsonPath("$.date").value("2026-08-01"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingExpense()
            throws Exception {

        String updateRequest = """
                {
                    "title": "Any",
                    "amount": "10.00",
                    "category": "Any",
                    "date": "2026-07-31"
                }
                """;

        mockMvc.perform(
                put("/api/expenses/999")
                        .header(AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteExpense() throws Exception {

        long id = createExpense("Grocery", "100.00", "Food", "2026-07-31");

        mockMvc.perform(delete("/api/expenses/" + id).header(AUTHORIZATION, bearer()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses").header(AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingExpense()
            throws Exception {

        mockMvc.perform(delete("/api/expenses/999").header(AUTHORIZATION, bearer()))
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
                        .header(AUTHORIZATION, bearer())
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

    // ------------------------------------------------------------------
    // Per-user data isolation
    // ------------------------------------------------------------------

    @Test
    void shouldIsolateExpensesBetweenUsers() throws Exception {

        createExpense("Mine", "100.00", "Food", "2026-07-31");

        String otherToken = registerAndGetToken("other-user");

        mockMvc.perform(get("/api/expenses").header(AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/expenses/summary").header(AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(0));

        mockMvc.perform(get("/api/expenses").header(AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldNotLetUserModifyAnotherUsersExpense() throws Exception {

        long id = createExpense("Mine", "100.00", "Food", "2026-07-31");

        String otherToken = registerAndGetToken("other-user");

        mockMvc.perform(delete("/api/expenses/" + id).header(AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound());

        String updateRequest = """
                {
                    "title": "Hacked",
                    "amount": "1.00",
                    "category": "Food",
                    "date": "2026-07-31"
                }
                """;

        mockMvc.perform(
                put("/api/expenses/" + id)
                        .header(AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest)
        )
        .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String bearer() {
        return "Bearer " + token;
    }

    private String bearer(String value) {
        return "Bearer " + value;
    }

    private String authBody(String username, String password) {
        return """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);
    }

    private String registerAndGetToken(String username) throws Exception {
        String response = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authBody(username, "password123"))
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

        return JsonPath.read(response, "$.token");
    }

    private long createExpense(
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

        String response = mockMvc.perform(
                post("/api/expenses")
                        .header(AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }
}
