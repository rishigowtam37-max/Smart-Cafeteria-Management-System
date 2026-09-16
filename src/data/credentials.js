/**
 * Demo login credentials, ported from the Java `LoginService` constructor.
 *
 * Exports: DEMO_ACCOUNTS, FOOD_CATEGORIES.
 *
 * NOTE: these are hard-coded on the client on purpose - this is a classroom
 * demo with no backend, exactly like the console app. Nothing here is secret
 * and no real authentication is performed.
 */

export const DEMO_ACCOUNTS = {
  admin: {
    username: 'admin',
    password: 'admin123',
    displayName: 'Cafeteria Admin',
  },
  customer: {
    username: 'customer',
    password: 'cust123',
    displayName: 'Rishi',
  },
};

/** Categories offered in the admin food form and the customer filter bar. */
export const FOOD_CATEGORIES = [
  'Snacks',
  'Main Course',
  'South Indian',
  'Beverages',
  'Desserts',
];
