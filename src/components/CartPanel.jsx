/**
 * The cart itself: every line with quantity controls, the total and the
 * "Place Order" action.
 *
 * Exports the default CartPanel component. The same component is rendered as a
 * sticky sidebar on desktop and inside the mobile bottom sheet, so it stays
 * layout-agnostic and lets the parent position it.
 *
 * The total is not calculated here. It arrives on the cart the server sent,
 * computed by CartService.calculateTotal() - the same method the console app
 * printed after "Total Bill :".
 */

import { useCafeteria } from '../context/CafeteriaContext.jsx';
import { formatRupees } from '../utils/formatters.js';
import CartLineItem from './CartLineItem.jsx';

/**
 * @param {{
 *   onPlaceOrder: () => void,
 *   onClose?: () => void,
 *   isPlacingOrder?: boolean,
 * }} props - Checkout handler, an optional close handler shown only in the
 *   mobile sheet, and whether checkout is in flight.
 * @returns {JSX.Element} The cart panel.
 */
export default function CartPanel({ onPlaceOrder, onClose, isPlacingOrder = false }) {
  const { cart, clearCart, isBusy } = useCafeteria();

  const isCartEmpty = cart.lines.length === 0;

  return (
    <section className="surface-card flex max-h-[calc(100vh-8rem)] flex-col overflow-hidden lg:max-h-[calc(100vh-9rem)]">
      <header className="flex items-center justify-between gap-3 border-b border-clay/60 px-5 py-4">
        <div>
          <h2 className="text-lg font-semibold text-cocoa">Your Cart</h2>
          <p className="text-xs text-bark">
            {cart.itemCount} {cart.itemCount === 1 ? 'item' : 'items'}
          </p>
        </div>

        {onClose ? (
          <button type="button" onClick={onClose} className="button-quiet">
            Close
          </button>
        ) : (
          !isCartEmpty && (
            <button type="button" onClick={clearCart} disabled={isBusy} className="button-quiet">
              Clear
            </button>
          )
        )}
      </header>

      {isCartEmpty ? (
        <div className="px-5 py-12 text-center">
          <p className="text-3xl">🧺</p>
          <p className="mt-3 text-sm text-bark">
            Your cart is empty. Tap <span className="font-semibold text-cocoa">Add</span> on
            anything that looks good.
          </p>
        </div>
      ) : (
        <>
          <ul className="flex-1 divide-y divide-clay/50 overflow-y-auto px-5">
            {cart.lines.map((cartLine) => (
              <CartLineItem key={cartLine.foodId} cartLine={cartLine} />
            ))}
          </ul>

          <footer className="border-t border-clay/60 bg-cream/60 px-5 py-4">
            <div className="mb-4 flex items-baseline justify-between">
              <span className="text-sm font-medium text-bark">Total</span>
              <span className="text-2xl font-semibold text-cocoa">{formatRupees(cart.total)}</span>
            </div>

            <button
              type="button"
              onClick={onPlaceOrder}
              disabled={isBusy || isPlacingOrder}
              className="button-primary w-full"
            >
              {isPlacingOrder ? 'Placing order…' : 'Place Order'}
            </button>

            <p className="mt-3 text-center text-xs text-bark">
              Pay at the counter when you collect.
            </p>
          </footer>
        </>
      )}
    </section>
  );
}
