/**
 * A single row inside the cart: item name, unit price, quantity stepper,
 * line total and a remove button.
 *
 * Exports the default CartLineItem component - the UI equivalent of the Java
 * CartItem class, whose toString() printed "name | Qty: n | Total: ₹x".
 */

import { useCafeteria } from '../context/CafeteriaContext.jsx';
import { calculateLineTotal } from '../utils/orderUtils.js';
import { formatRupees } from '../utils/formatters.js';

/**
 * @param {{ cartItem: object }} props - One cart line, including the
 *   availableQuantity snapshot used to cap the stepper.
 * @returns {JSX.Element} A cart row.
 */
export default function CartLineItem({ cartItem }) {
  const { changeCartQuantity, removeFromCart } = useCafeteria();
  const hasReachedStockLimit = cartItem.quantity >= cartItem.availableQuantity;

  return (
    <li className="flex items-start gap-3 py-4">
      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-sand text-lg">
        {cartItem.emoji ?? '🍽️'}
      </span>

      <div className="min-w-0 flex-1">
        <div className="flex items-start justify-between gap-2">
          <p className="truncate text-sm font-semibold text-cocoa">{cartItem.name}</p>
          <p className="shrink-0 text-sm font-semibold text-cocoa">
            {formatRupees(calculateLineTotal(cartItem))}
          </p>
        </div>

        <p className="mt-0.5 text-xs text-bark">{formatRupees(cartItem.price)} each</p>

        <div className="mt-2.5 flex items-center gap-3">
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => changeCartQuantity(cartItem.id, -1)}
              aria-label={`Reduce ${cartItem.name} quantity`}
              className="stepper-button"
            >
              −
            </button>

            <span className="w-5 text-center text-sm font-semibold text-cocoa">
              {cartItem.quantity}
            </span>

            <button
              type="button"
              onClick={() => changeCartQuantity(cartItem.id, 1)}
              disabled={hasReachedStockLimit}
              aria-label={`Increase ${cartItem.name} quantity`}
              className="stepper-button"
            >
              +
            </button>
          </div>

          <button
            type="button"
            onClick={() => removeFromCart(cartItem.id)}
            className="text-xs font-medium text-bark underline-offset-2 transition hover:text-ember hover:underline"
          >
            Remove
          </button>
        </div>
      </div>
    </li>
  );
}
