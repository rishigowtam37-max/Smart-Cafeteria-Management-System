/**
 * Admin menu table listing every food item with edit and delete actions.
 *
 * Exports the default MenuTable component. On small screens the same data is
 * rendered as stacked cards instead of a table, so nothing scrolls sideways.
 */

import { formatRupees } from '../utils/formatters.js';
import StockBadge from './StockBadge.jsx';

/**
 * @param {{
 *   menuItems: Array<object>,
 *   onEdit: (foodItem: object) => void,
 *   onDelete: (foodItem: object) => void,
 * }} props - Menu data and the two row actions.
 * @returns {JSX.Element} A table on `sm`+ screens, stacked cards below.
 */
export default function MenuTable({ menuItems, onEdit, onDelete }) {
  if (menuItems.length === 0) {
    return (
      <div className="surface-card px-6 py-16 text-center">
        <p className="text-3xl">📋</p>
        <p className="mt-3 text-sm text-bark">
          No food items yet. Add the first one to open the counter.
        </p>
      </div>
    );
  }

  return (
    <>
      {/* Mobile: one card per item */}
      <div className="space-y-3 sm:hidden">
        {menuItems.map((foodItem) => (
          <div key={foodItem.id} className="surface-card p-4">
            <div className="flex items-start gap-3">
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-sand text-lg">
                {foodItem.emoji ?? '🍽️'}
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate font-semibold text-cocoa">{foodItem.name}</p>
                <p className="text-xs uppercase tracking-wide text-bark">{foodItem.category}</p>
              </div>
              <p className="shrink-0 font-semibold text-cocoa">{formatRupees(foodItem.price)}</p>
            </div>

            <div className="mt-3 flex items-center justify-between border-t border-clay/50 pt-3">
              <StockBadge quantity={foodItem.quantity} />
              <div className="flex gap-1">
                <button type="button" onClick={() => onEdit(foodItem)} className="button-quiet">
                  Edit
                </button>
                <button
                  type="button"
                  onClick={() => onDelete(foodItem)}
                  className="button-quiet hover:bg-ember-soft hover:text-ember"
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Desktop: full table */}
      <div className="surface-card hidden overflow-hidden sm:block">
        <table className="w-full text-left text-sm">
          <thead className="border-b border-clay/60 bg-cream/60">
            <tr className="text-xs uppercase tracking-wider text-bark">
              <th className="px-5 py-3.5 font-semibold">ID</th>
              <th className="px-5 py-3.5 font-semibold">Item</th>
              <th className="px-5 py-3.5 font-semibold">Category</th>
              <th className="px-5 py-3.5 text-right font-semibold">Price</th>
              <th className="px-5 py-3.5 text-right font-semibold">Available</th>
              <th className="px-5 py-3.5 text-right font-semibold">Actions</th>
            </tr>
          </thead>

          <tbody className="divide-y divide-clay/50">
            {menuItems.map((foodItem) => (
              <tr key={foodItem.id} className="transition hover:bg-cream/50">
                <td className="px-5 py-4 text-bark tabular-nums">{foodItem.id}</td>

                <td className="px-5 py-4">
                  <div className="flex items-center gap-3">
                    <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-sand">
                      {foodItem.emoji ?? '🍽️'}
                    </span>
                    <span className="font-medium text-cocoa">{foodItem.name}</span>
                  </div>
                </td>

                <td className="px-5 py-4 text-bark">{foodItem.category}</td>

                <td className="px-5 py-4 text-right font-medium text-cocoa tabular-nums">
                  {formatRupees(foodItem.price)}
                </td>

                <td className="px-5 py-4 text-right">
                  <StockBadge quantity={foodItem.quantity} />
                </td>

                <td className="px-5 py-4">
                  <div className="flex justify-end gap-1">
                    <button type="button" onClick={() => onEdit(foodItem)} className="button-quiet">
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => onDelete(foodItem)}
                      className="button-quiet hover:bg-ember-soft hover:text-ember"
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
