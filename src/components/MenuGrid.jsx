/**
 * Responsive grid of food cards for the customer view.
 *
 * Exports the default MenuGrid component, which lays out one FoodCard per menu
 * item and shows an empty state when a filter matches nothing.
 */

import FoodCard from './FoodCard.jsx';

/**
 * @param {{ menuItems: Array<object> }} props - Items to display, already filtered.
 * @returns {JSX.Element} The card grid, or an empty-state message.
 */
export default function MenuGrid({ menuItems }) {
  if (menuItems.length === 0) {
    return (
      <div className="surface-card px-6 py-16 text-center">
        <p className="text-3xl">🍽️</p>
        <p className="mt-3 text-sm text-bark">Nothing on the counter in this category yet.</p>
      </div>
    );
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
      {menuItems.map((foodItem) => (
        <FoodCard key={foodItem.id} foodItem={foodItem} />
      ))}
    </div>
  );
}
