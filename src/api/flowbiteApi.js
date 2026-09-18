/**
 * One function per backend endpoint - the full surface the UI is allowed to use.
 *
 * Nothing here computes anything. Totals, stock limits, order codes and role
 * checks are all decided by the Spring Boot application; these functions only
 * carry the question there and the answer back.
 *
 * See `backend/src/main/java/com/flowbite/web/` for the other side of each call.
 */

import { apiDelete, apiGet, apiPatch, apiPost, apiPut } from './client.js';

/* ------------------------------------------------------------------ auth */

/**
 * Signs in.
 *
 * @param {'admin' | 'customer'} role - Which counter to sign in at.
 * @param {string} username
 * @param {string} password
 * @returns {Promise<{role: string, name: string, username: string}>}
 */
export const signIn = (role, username, password) =>
  apiPost('/auth/login', { role, username, password });

/** Ends the session, releasing any stock held in the cart. */
export const signOut = () => apiPost('/auth/logout');

/**
 * Asks who is signed in. Throws a 401 ApiError when nobody is, which is how the
 * app decides whether to show the login screen on load.
 */
export const fetchCurrentUser = () => apiGet('/auth/me');

/**
 * Which optional features this server has configured.
 *
 * Callable before signing in, since the app needs to know what it can offer
 * while the login screen is still up. It reports only on/off - never the key
 * that decides it.
 *
 * @returns {Promise<{smartCravingEnabled: boolean}>}
 */
export const fetchConfig = () => apiGet('/config');

/* ------------------------------------------------------------------ menu */

/** @returns {Promise<Array<object>>} Every menu item, sold-out ones included. */
export const fetchMenu = () => apiGet('/menu');

/* ------------------------------------------------------------------ cart */

/** @returns {Promise<object>} The whole cart: lines, total and item count. */
export const fetchCart = () => apiGet('/cart');

/**
 * Adds units of an item, reserving the stock server-side.
 *
 * @param {number} foodId
 * @param {number} quantity
 * @returns {Promise<object>} The updated cart.
 */
export const addToCart = (foodId, quantity) => apiPost('/cart', { foodId, quantity });

/**
 * Sets a line to an absolute quantity; zero removes it.
 *
 * Absolute rather than a +1/-1 delta because two quick taps would otherwise race
 * and the server would have no way to tell a lost update from a real one.
 *
 * @param {number} foodId
 * @param {number} quantity
 * @returns {Promise<object>} The updated cart.
 */
export const changeCartQuantity = (foodId, quantity) =>
  apiPatch(`/cart/${foodId}`, { quantity });

/** Removes a line, returning its units to the counter. */
export const removeFromCart = (foodId) => apiDelete(`/cart/${foodId}`);

/** Empties the cart, returning everything. */
export const clearCart = () => apiDelete('/cart');

/* ---------------------------------------------------------------- orders */

/** @returns {Promise<object>} The placed order, including its FB-0001 code. */
export const placeOrder = () => apiPost('/orders');

/** @returns {Promise<Array<object>>} Every order, newest first. Admin only. */
export const fetchOrders = () => apiGet('/admin/orders');

/**
 * @returns {Promise<{itemsOnMenu: number, soldOut: number, ordersPlaced: number, revenue: number}>}
 *   The dashboard tiles, totalled server-side. Admin only.
 */
export const fetchAdminStats = () => apiGet('/admin/stats');

/* --------------------------------------------------------- smart craving */

/**
 * Asks the server to suggest menu items for a craving.
 *
 * The prompt, the Gemini call, the retry on a busy model and the check that
 * every suggested id is a real in-stock item all happen in Java. The API key is
 * never sent here, which is the whole reason this moved server-side.
 *
 * @param {string} craving - What the customer typed, max 200 characters.
 * @returns {Promise<{headline: string, picks: Array<{item: object, reason: string}>}>}
 */
export const suggestFromCraving = (craving) => apiPost('/craving', { craving });

/* ------------------------------------------------------------ admin menu */

/** Creates a menu item. The server assigns the id. */
export const createMenuItem = (foodItem) => apiPost('/admin/menu', foodItem);

/** Replaces a menu item. */
export const updateMenuItem = (id, foodItem) => apiPut(`/admin/menu/${id}`, foodItem);

/** Removes a menu item from the menu. */
export const deleteMenuItem = (id) => apiDelete(`/admin/menu/${id}`);
