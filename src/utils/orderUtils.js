/**
 * Pure helpers for order identity and cart arithmetic.
 *
 * Exports: generateOrderId, calculateCartTotal, calculateCartItemCount,
 * calculateLineTotal.
 * These replace the Java `OrderService.nextOrderId` counter and the
 * `CartItem.getTotalPrice()` / `CartService.calculateTotal()` methods.
 */

/**
 * Builds a human-friendly order ID from a sequence number.
 *
 * The Java app used a bare incrementing integer; the web app keeps the same
 * sequence but prefixes it so order IDs are recognisable on screen.
 *
 * @param {number} sequenceNumber - 1-based counter of orders placed this session.
 * @returns {string} Order ID such as "FB-0001".
 */
export function generateOrderId(sequenceNumber) {
  return `FB-${String(sequenceNumber).padStart(4, '0')}`;
}

/**
 * Calculates the price of a single cart line (unit price × quantity).
 *
 * @param {{ price: number, quantity: number }} cartItem - One cart line.
 * @returns {number} Line total in rupees.
 */
export function calculateLineTotal(cartItem) {
  return cartItem.price * cartItem.quantity;
}

/**
 * Sums every line in the cart.
 *
 * @param {Array<{ price: number, quantity: number }>} cartItems - Cart contents.
 * @returns {number} Total bill in rupees.
 */
export function calculateCartTotal(cartItems) {
  return cartItems.reduce((runningTotal, item) => runningTotal + calculateLineTotal(item), 0);
}

/**
 * Counts how many individual units sit in the cart, not how many lines.
 *
 * @param {Array<{ quantity: number }>} cartItems - Cart contents.
 * @returns {number} Total number of units, used for the header cart badge.
 */
export function calculateCartItemCount(cartItems) {
  return cartItems.reduce((runningCount, item) => runningCount + item.quantity, 0);
}
