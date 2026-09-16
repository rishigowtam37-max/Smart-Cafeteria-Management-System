/**
 * Small pill showing an item's remaining stock, coloured by availability.
 *
 * Exports the default StockBadge component, used by the admin menu table in
 * both its table and mobile-card layouts.
 */

/**
 * @param {{ quantity: number }} props - Remaining units of the food item.
 * @returns {JSX.Element} A green "N in stock" pill, or a red "Sold out" pill.
 */
export default function StockBadge({ quantity }) {
  const isAvailable = quantity > 0;

  return (
    <span
      className={`inline-block rounded-full px-2.5 py-1 text-xs font-semibold tabular-nums ${
        isAvailable ? 'bg-leaf-soft text-leaf' : 'bg-ember-soft text-ember'
      }`}
    >
      {isAvailable ? `${quantity} in stock` : 'Sold out'}
    </span>
  );
}
