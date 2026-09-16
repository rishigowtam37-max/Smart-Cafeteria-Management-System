/**
 * Horizontal pill bar for filtering the customer menu by category.
 *
 * Exports the default CategoryFilter component. Purely presentational - the
 * parent owns the selected category and the filtering itself.
 */

/**
 * @param {{
 *   categories: string[],
 *   activeCategory: string,
 *   onCategoryChange: (category: string) => void,
 * }} props - Category list, the selected one, and the change handler.
 * @returns {JSX.Element} A scrollable row of filter pills.
 */
export default function CategoryFilter({ categories, activeCategory, onCategoryChange }) {
  return (
    <div className="mb-6 flex gap-2 overflow-x-auto pb-1">
      {categories.map((category) => {
        const isActive = category === activeCategory;

        return (
          <button
            key={category}
            type="button"
            onClick={() => onCategoryChange(category)}
            aria-pressed={isActive}
            className={`shrink-0 rounded-full border px-4 py-2 text-sm font-medium transition ${
              isActive
                ? 'border-saffron bg-saffron text-white'
                : 'border-clay bg-shell text-bark hover:border-bark hover:text-cocoa'
            }`}
          >
            {category}
          </button>
        );
      })}
    </div>
  );
}
