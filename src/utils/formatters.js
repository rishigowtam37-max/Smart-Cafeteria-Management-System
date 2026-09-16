/**
 * Display formatting helpers shared across the UI.
 *
 * Exports: formatRupees, formatOrderTime.
 * These mirror the `₹%.2f` output used by the Java console app's toString()
 * methods, so prices read the same way in the web port.
 */

/**
 * Formats a number as an Indian-rupee price string.
 *
 * @param {number} amount - Amount in rupees, e.g. 180 or 87.5.
 * @returns {string} Localised price such as "₹180.00".
 */
export function formatRupees(amount) {
  return `₹${Number(amount).toFixed(2)}`;
}

/**
 * Formats an order timestamp as a short, human-readable time of day.
 *
 * @param {number} timestamp - Milliseconds since the epoch (Date.now()).
 * @returns {string} Time such as "1:45 PM".
 */
export function formatOrderTime(timestamp) {
  return new Date(timestamp).toLocaleTimeString('en-IN', {
    hour: 'numeric',
    minute: '2-digit',
  });
}
