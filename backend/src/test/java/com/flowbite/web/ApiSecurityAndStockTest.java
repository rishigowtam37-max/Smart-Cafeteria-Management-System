package com.flowbite.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The rules that only exist once the app is on a network: who may call what, and
 * what a refusal looks like on the wire.
 *
 * <p>These run against the real application context, so a route left unguarded
 * would show up here rather than in production.
 */
@SpringBootTest
class ApiSecurityAndStockTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    // Built by hand rather than with @AutoConfigureMockMvc: Spring Boot 4 moved
    // that annotation out of spring-boot-test-autoconfigure, and this needs no
    // extra dependency to get the same fully wired application.
    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Signs in and returns the session carrying that identity.
     *
     * <p>The session comes back off the login request rather than being created
     * here, because {@code AuthController} deliberately invalidates whatever
     * session existed and starts a fresh one. A browser follows that via the new
     * cookie; a test has to pick it up the same way.
     */
    private MockHttpSession signIn(String role, String username, String password) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"%s","username":"%s","password":"%s"}
                                """.formatted(role, username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    @Test
    @DisplayName("a customer calling an admin endpoint gets 403, not a hidden button")
    void customerIsForbiddenFromAdminEndpoints() throws Exception {
        MockHttpSession customer = signIn("customer", "customer", "cust123");

        mockMvc.perform(get("/api/admin/orders").session(customer))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This area is for cafeteria staff only."));

        mockMvc.perform(delete("/api/admin/menu/1").session(customer))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("an admin cannot use the cart or place orders")
    void adminIsForbiddenFromCustomerEndpoints() throws Exception {
        MockHttpSession admin = signIn("admin", "admin", "admin123");

        mockMvc.perform(get("/api/cart").session(admin))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/orders").session(admin))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("no session at all gets 401")
    void anonymousIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/menu")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/orders")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("bad credentials are refused with 401 and a message")
    void badCredentialsAreRefused() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"admin","username":"admin","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("ordering more than the counter has returns 409 with the domain's message")
    void overStockAddReturnsConflict() throws Exception {
        MockHttpSession customer = signIn("customer", "customer", "cust123");

        // Gulab Jamun (id 12) is seeded sold out.
        mockMvc.perform(post("/api/cart")
                        .session(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"foodId":12,"quantity":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Not enough stock available."))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("checking out an empty cart returns 409")
    void emptyCartCheckoutReturnsConflict() throws Exception {
        MockHttpSession customer = signIn("customer", "customer", "cust123");

        mockMvc.perform(post("/api/orders").session(customer))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Your cart is empty."));
    }

    @Test
    @DisplayName("an unknown food id returns 404")
    void unknownFoodReturnsNotFound() throws Exception {
        MockHttpSession customer = signIn("customer", "customer", "cust123");

        mockMvc.perform(post("/api/cart")
                        .session(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"foodId":9999,"quantity":1}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Food item not found."));
    }

    @Test
    @DisplayName("a zero quantity is rejected by validation before the domain sees it")
    void invalidQuantityIsRejected() throws Exception {
        MockHttpSession customer = signIn("customer", "customer", "cust123");

        mockMvc.perform(post("/api/cart")
                        .session(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"foodId":1,"quantity":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid quantity."));
    }

    @Test
    @DisplayName("the login response never carries the password")
    void loginResponseHasNoPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"customer","username":"customer","password":"cust123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rishi"))
                .andExpect(jsonPath("$.role").value("customer"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
