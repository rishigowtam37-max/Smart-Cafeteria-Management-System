/**
 * One food item presented as a card: emoji tile, name, category, description,
 * availability badge, price and an add-to-cart control.
 *
 * Exports the default FoodCard component. It reads the cart from context so the
 * button can flip to "in cart" state and stop at the stock limit, which is the
 * web version of the "Not enough stock available." guard in CartService.
 */

import { useCafeteria } from '../context/CafeteriaContext.jsx';
import { formatRupees } from '../utils/formatters.js';

/**
 * @param {{ foodItem: object }} props - The menu item to render.
 * @returns {JSX.Element} A menu card.
 */
export default function FoodCard({ foodItem }) {
  const { cartItems, addToCart } = useCafeteria();

  const quantityInCart = cartItems.find((line) => line.id === foodItem.id)?.quantity ?? 0;
  const isAvailable = foodItem.quantity > 0;
  const isStockExhausted = quantityInCart >= foodItem.quantity;
  const isRunningLow = isAvailable && foodItem.quantity <= 5;

  return (
    <article
      className={`surface-card flex flex-col p-5 transition duration-200 ${
        isAvailable ? 'hover:-translate-y-0.5 hover:shadow-lift' : 'opacity-70'
      }`}
    >
      <div className="flex items-start gap-4">
        <span className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-sand text-2xl">
          {foodItem.emoji ?? '🍽️'}
        </span>

        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <h3 className="truncate text-lg font-semibold text-cocoa">{foodItem.name}</h3>
            <span
              className={`shrink-0 rounded-full px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide ${
                isAvailable ? 'bg-leaf-soft text-leaf' : 'bg-ember-soft text-ember'
              }`}
            >
              {isAvailable ? 'Available' : 'Sold out'}
            </span>
          </div>

          <p className="mt-0.5 text-xs font-medium uppercase tracking-wide text-bark">
            {foodItem.category}
          </p>
        </div>
      </div>

      {foodItem.description && (
        <p className="mt-3 text-sm leading-relaxed text-bark">{foodItem.description}</p>
      )}

      <div className="mt-auto flex items-end justify-between gap-3 pt-5">
        <div>
          <p className="text-xl font-semibold text-cocoa">{formatRupees(foodItem.price)}</p>
          <p className="mt-0.5 text-xs text-bark">
            {isAvailable ? (
              <>
                {foodItem.quantity} left
                {isRunningLow && <span className="ml-1 text-saffron">· going fast</span>}
              </>
            ) : (
              'Back tomorrow'
            )}
          </p>
        </div>

        <button
          type="button"
          onClick={() => addToCart(foodItem)}
          disabled={!isAvailable || isStockExhausted}
          className="button-primary px-4"
        >
          {quantityInCart > 0 ? `In cart · ${quantityInCart}` : 'Add'}
        </button>
      </div>
    </article>
  );
}
