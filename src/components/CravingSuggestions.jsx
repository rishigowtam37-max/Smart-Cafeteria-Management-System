/**
 * The result of one Smart Craving lookup: a headline plus up to three menu
 * picks, each with the model's one-line reason and an add-to-cart button.
 *
 * Exports the default CravingSuggestions component. Presentational apart from
 * the cart action - the parent owns the request, the loading state and the
 * error, this file only draws what came back.
 *
 * Every pick here is a real item off the live menu (cravingClient discards ids
 * that do not resolve), so the stock rules are the same ones FoodCard applies.
 */

import { useCafeteria } from '../context/CafeteriaContext.jsx';
import { formatRupees } from '../utils/formatters.js';

/**
 * @param {{
 *   headline: string,
 *   picks: Array<{ item: object, reason: string }>,
 *   onDismiss: () => void,
 * }} props - The suggestion to render and a way to clear it.
 * @returns {JSX.Element} The suggestion panel, or an empty state when the model
 *   found nothing suitable on the menu.
 */
export default function CravingSuggestions({ headline, picks, onDismiss }) {
  const { cartItems, addToCart } = useCafeteria();

  if (picks.length === 0) {
    return (
      <div className="mt-4 rounded-2xl border border-clay/60 bg-cream/60 px-4 py-5 text-center">
        <p className="text-sm text-bark">
          {headline || 'Nothing on today’s counter quite matches that. Try another craving?'}
        </p>
        <button type="button" onClick={onDismiss} className="button-quiet mt-2">
          Clear
        </button>
      </div>
    );
  }

  return (
    <div className="mt-4">
      <div className="mb-3 flex items-start justify-between gap-3">
        <p className="text-sm font-medium text-cocoa">
          {headline || 'Here’s what we’d send out:'}
        </p>
        <button type="button" onClick={onDismiss} className="button-quiet shrink-0">
          Clear
        </button>
      </div>

      <ul className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {picks.map(({ item, reason }, pickIndex) => {
          const quantityInCart = cartItems.find((line) => line.id === item.id)?.quantity ?? 0;
          const isStockExhausted = quantityInCart >= item.quantity;

          return (
            <li
              key={item.id}
              className="flex flex-col rounded-2xl border border-saffron/30 bg-saffron-soft/50 p-4"
            >
              <div className="flex items-start gap-3">
                <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-shell text-xl">
                  {item.emoji ?? '🍽️'}
                </span>

                <div className="min-w-0 flex-1">
                  {pickIndex === 0 && (
                    <p className="text-[11px] font-semibold uppercase tracking-wide text-saffron">
                      Top pick
                    </p>
                  )}
                  <h3 className="truncate text-base font-semibold text-cocoa">{item.name}</h3>
                  <p className="text-xs text-bark">
                    {item.category} · {formatRupees(item.price)}
                  </p>
                </div>
              </div>

              {reason && <p className="mt-3 text-sm leading-relaxed text-bark">{reason}</p>}

              <div className="mt-auto pt-4">
                <button
                  type="button"
                  onClick={() => addToCart(item)}
                  disabled={isStockExhausted}
                  className="button-primary w-full px-4"
                >
                  {quantityInCart > 0 ? `In cart · ${quantityInCart}` : 'Add'}
                </button>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
