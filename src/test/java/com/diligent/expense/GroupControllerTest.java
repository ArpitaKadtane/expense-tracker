package com.diligent.expense;

import com.diligent.expense.repository.ExpenseShareRepository;
import com.diligent.expense.repository.GroupExpenseRepository;
import com.diligent.expense.repository.GroupMembershipRepository;
import com.diligent.expense.repository.GroupRepository;
import com.diligent.expense.repository.UserRepository;
import com.diligent.expense.service.ExpenseService;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GroupControllerTest {

    private static final String AUTHORIZATION = "Authorization";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private GroupExpenseRepository groupExpenseRepository;

    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseService expenseService;

    private String aliceToken;

    @BeforeEach
    void setUp() throws Exception {
        expenseShareRepository.deleteAll();
        groupExpenseRepository.deleteAll();
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        expenseService.clearExpenses();
        userRepository.deleteAll();

        aliceToken = register("alice");
    }

    // ------------------------------------------------------------------
    // Security
    // ------------------------------------------------------------------

    @Test
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // Groups & members
    // ------------------------------------------------------------------

    @Test
    void shouldCreateGroupWithMembers() throws Exception {
        register("bob");
        register("carol");

        long groupId = createGroup(aliceToken, "Trip", "bob", "carol");

        mockMvc.perform(get("/api/groups/" + groupId)
                        .header(AUTHORIZATION, bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Trip"))
                .andExpect(jsonPath("$.ownerUsername").value("alice"))
                .andExpect(jsonPath("$.members", hasSize(3)));
    }

    @Test
    void shouldOnlyListGroupsTheUserBelongsTo() throws Exception {
        String bobToken = register("bob");

        createGroup(aliceToken, "Alice Trip", "bob");
        createGroup(bobToken, "Bob Only");

        mockMvc.perform(get("/api/groups").header(AUTHORIZATION, bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Alice Trip"))
                .andExpect(jsonPath("$[0].memberCount").value(2));
    }

    @Test
    void shouldAddMemberToGroup() throws Exception {
        register("bob");
        long groupId = createGroup(aliceToken, "Trip");

        mockMvc.perform(post("/api/groups/" + groupId + "/members")
                        .header(AUTHORIZATION, bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bob\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    void shouldReturnNotFoundForNonMember() throws Exception {
        String bobToken = register("bob");
        register("carol");

        long groupId = createGroup(aliceToken, "Private", "carol");

        mockMvc.perform(get("/api/groups/" + groupId)
                        .header(AUTHORIZATION, bearer(bobToken)))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Splits, balances & settle-up
    // ------------------------------------------------------------------

    @Test
    void shouldSplitEquallyAndComputeBalances() throws Exception {
        register("bob");
        long groupId = createGroup(aliceToken, "Trip", "bob");

        addExpense(aliceToken, groupId, """
                {"title":"Dinner","amount":30,"category":"Food",
                 "date":"2026-10-17","splitType":"EQUAL"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paidBy").value("alice"))
                .andExpect(jsonPath("$.shares", hasSize(2)));

        mockMvc.perform(get("/api/groups/" + groupId + "/balances")
                        .header(AUTHORIZATION, bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].paid").value(30.00))
                .andExpect(jsonPath("$[0].owed").value(15.00))
                .andExpect(jsonPath("$[0].net").value(15.00))
                .andExpect(jsonPath("$[1].username").value("bob"))
                .andExpect(jsonPath("$[1].net").value(-15.00));
    }

    @Test
    void shouldSuggestSettleUpTransfer() throws Exception {
        register("bob");
        long groupId = createGroup(aliceToken, "Trip", "bob");

        addExpense(aliceToken, groupId, """
                {"title":"Dinner","amount":30,"category":"Food",
                 "date":"2026-10-17","splitType":"EQUAL"}
                """).andExpect(status().isCreated());

        mockMvc.perform(get("/api/groups/" + groupId + "/settle-up")
                        .header(AUTHORIZATION, bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].from").value("bob"))
                .andExpect(jsonPath("$[0].to").value("alice"))
                .andExpect(jsonPath("$[0].amount").value(15.00));
    }

    @Test
    void shouldHandleExactSplit() throws Exception {
        register("bob");
        register("carol");
        long groupId = createGroup(aliceToken, "Trip", "bob", "carol");

        addExpense(aliceToken, groupId, """
                {"title":"Hotel","amount":60,"category":"Travel","date":"2026-10-17",
                 "splitType":"EXACT",
                 "participants":[
                    {"username":"alice","value":10},
                    {"username":"bob","value":20},
                    {"username":"carol","value":30}]}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shares", hasSize(3)));

        mockMvc.perform(get("/api/groups/" + groupId + "/balances")
                        .header(AUTHORIZATION, bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].net").value(50.00))
                .andExpect(jsonPath("$[2].username").value("carol"))
                .andExpect(jsonPath("$[2].net").value(-30.00));
    }

    @Test
    void shouldHandlePercentageSplit() throws Exception {
        register("bob");
        long groupId = createGroup(aliceToken, "Trip", "bob");

        addExpense(aliceToken, groupId, """
                {"title":"Rent","amount":200,"category":"Home","date":"2026-10-17",
                 "splitType":"PERCENTAGE",
                 "participants":[
                    {"username":"alice","value":25},
                    {"username":"bob","value":75}]}
                """).andExpect(status().isCreated());

        mockMvc.perform(get("/api/groups/" + groupId + "/balances")
                        .header(AUTHORIZATION, bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].owed").value(50.00))
                .andExpect(jsonPath("$[1].username").value("bob"))
                .andExpect(jsonPath("$[1].owed").value(150.00));
    }

    @Test
    void shouldDefaultToAllMembersWhenNoParticipantsGiven() throws Exception {
        register("bob");
        long groupId = createGroup(aliceToken, "Trip", "bob");

        addExpense(aliceToken, groupId, """
                {"title":"Snacks","amount":30,"category":"Food",
                 "date":"2026-10-17","splitType":"EQUAL"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shares", hasSize(2)));
    }

    @Test
    void shouldRejectExpenseWithNonMemberParticipant() throws Exception {
        register("bob");
        register("carol");
        long groupId = createGroup(aliceToken, "Trip", "bob");

        addExpense(aliceToken, groupId, """
                {"title":"Dinner","amount":30,"category":"Food","date":"2026-10-17",
                 "splitType":"EXACT",
                 "participants":[
                    {"username":"alice","value":15},
                    {"username":"carol","value":15}]}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectExactSplitThatDoesNotMatchTotal() throws Exception {
        register("bob");
        long groupId = createGroup(aliceToken, "Trip", "bob");

        addExpense(aliceToken, groupId, """
                {"title":"Dinner","amount":30,"category":"Food","date":"2026-10-17",
                 "splitType":"EXACT",
                 "participants":[
                    {"username":"alice","value":10},
                    {"username":"bob","value":10}]}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotLetNonMemberReadGroupExpenses() throws Exception {
        String bobToken = register("bob");
        register("carol");
        long groupId = createGroup(aliceToken, "Private", "carol");

        mockMvc.perform(get("/api/groups/" + groupId + "/expenses")
                        .header(AUTHORIZATION, bearer(bobToken)))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String authBody(String username) {
        return """
                {"username":"%s","password":"password123"}
                """.formatted(username);
    }

    private String register(String username) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authBody(username)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.token");
    }

    private long createGroup(String token, String name, String... members) throws Exception {
        String memberJson = Arrays.stream(members)
                .map(member -> "\"" + member + "\"")
                .collect(Collectors.joining(","));

        String request = """
                {"name":"%s","memberUsernames":[%s]}
                """.formatted(name, memberJson);

        String response = mockMvc.perform(post("/api/groups")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private ResultActions addExpense(String token, long groupId, String body) throws Exception {
        return mockMvc.perform(post("/api/groups/" + groupId + "/expenses")
                .header(AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
