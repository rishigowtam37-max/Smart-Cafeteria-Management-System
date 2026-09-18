package com.flowbite.service;

import com.flowbite.model.Admin;
import com.flowbite.model.Customer;
import com.flowbite.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Sign-in, which is the one service that crossed over completely unchanged.
 */
class LoginServiceTest {

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService();
    }

    @Test
    @DisplayName("the admin account signs in")
    void adminSignsIn() {
        Admin admin = loginService.adminLogin("admin", "admin123");

        assertNotNull(admin);
        assertEquals("Cafeteria Admin", admin.getName());
    }

    @Test
    @DisplayName("the customer account signs in")
    void customerSignsIn() {
        Customer customer = loginService.customerLogin("customer", "cust123");

        assertNotNull(customer);
        assertEquals("Rishi", customer.getName());
    }

    @Test
    @DisplayName("a wrong password is refused")
    void wrongPasswordIsRefused() {
        assertNull(loginService.adminLogin("admin", "wrong"));
        assertNull(loginService.customerLogin("customer", "wrong"));
    }

    @Test
    @DisplayName("a wrong username is refused")
    void wrongUsernameIsRefused() {
        assertNull(loginService.adminLogin("root", "admin123"));
    }

    @Test
    @DisplayName("the generic login returns the right User subclass for each role")
    void genericLoginIsPolymorphic() {
        User asAdmin = loginService.login("admin", "admin", "admin123");
        User asCustomer = loginService.login("customer", "customer", "cust123");

        assertInstanceOf(Admin.class, asAdmin);
        assertInstanceOf(Customer.class, asCustomer);
    }

    @Test
    @DisplayName("the role name is matched case-insensitively")
    void roleIsCaseInsensitive() {
        assertNotNull(loginService.login("ADMIN", "admin", "admin123"));
    }

    @Test
    @DisplayName("an unknown role is refused")
    void unknownRoleIsRefused() {
        assertNull(loginService.login("chef", "admin", "admin123"));
    }

    @Test
    @DisplayName("customer credentials cannot sign in as admin")
    void credentialsDoNotCrossRoles() {
        assertNull(loginService.login("admin", "customer", "cust123"));
        assertNull(loginService.login("customer", "admin", "admin123"));
    }
}
