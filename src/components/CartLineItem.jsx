/**
 * A single row inside the cart: item name, unit price, quantity stepper,
 * line total and a remove button.
 *
 * Exports the default CartLineItem component - the UI equivalent of the Java
 * CartItem class, whose toString() printed "name | Qty: n | Total: ₹x".
 *
 * The stepper sends the quantity it wants, not a +1/-1 step. Two quick taps
 * would otherwise race each other to the server, and the second could be
 * applied to a cart the first had already changed.
 */

import { useCafeteria } from '../context/CafeteriaContext.jsx';
import { formatRupees } from '../utils/formatters.js';

/**
 * @param {{ cartLine: object }} props - One line of the cart the server sent,
 *   including lineTotal and how many units are still available beyond it.
 * @returns {JSX.Element} A cart row.
 */
export default function CartLineItem({ cartLine }) {
  const { setCartQuantity, removeFromCart, isBusy } = useCafeteria();

  // availableQuantity is what is left on the counter, which already excludes
  // the units this line is holding - so anything above zero can still be added.
  const hasReachedStockLimit = cartLine.availableQuantity <= 0;

  return (
    <li className="flex items-start gap-3 py-4">
      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-sand text-lg">
        {cartLine.emoji ?? '🍽️'}
      </span>

      <div className="min-w-0 flex-1">
        <div className="flex items-start justify-between gap-2">
          <p className="truncate text-sm font-semibold text-cocoa">{cartLine.name}</p>
          <p className="shrink-0 text-sm font-semibold text-cocoa">
            {formatRupees(cartLine.lineTotal)}
          </p>
        </div>

        <p className="mt-0.5 text-xs text-bark">{formatRupees(cartLine.price)} each</p>

        <div className="mt-2.5 flex items-center gap-3">
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setCartQuantity(cartLine.foodId, cartLine.quantity - 1)}
              disabled={isBusy}
              aria-label={`Reduce ${cartLine.name} quantity`}
              className="stepper-button"
            >
              −
            </button>

            <span className="w-5 text-center text-sm font-semibold text-cocoa">
              {cartLine.quantity}
            </span>

            <button
              type="button"
              onClick={() => setCartQuantity(cartLine.foodId, cartLine.quantity + 1)}
              disabled={isBusy || hasReachedStockLimit}
              aria-label={`Increase ${cartLine.name} quantity`}
              className="stepper-button"
            >
              +
            </button>
          </div>

          <button
            type="button"
            onClick={() => removeFromCart(cartLine.foodId)}
            disabled={isBusy}
            className="text-xs font-medium text-bark underline-offset-2 transition hover:text-ember hover:underline disabled:opacity-50"
          >
            Remove
          </button>
        </div>
      </div>
    </li>
  );
}
