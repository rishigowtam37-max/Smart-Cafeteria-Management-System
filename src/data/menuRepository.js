/**
 * Loads the cafeteria menu that was ported out of the Java `FoodService`
 * constructor into `public/data/menu.json`.
 *
 * Exports: loadMenuItems, createBlankFoodItem, getNextFoodItemId.
 * The JSON file is fetched at runtime (rather than imported) so the menu can be
 * edited without touching application code, exactly as the seed list in
 * FoodService.java was the single source of truth for the console app.
 */

/**
 * Fetches and normalises the seed menu.
 *
 * @returns {Promise<Array<object>>} Food items with id, name, price, quantity,
 *   category, description and emoji fields.
 * @throws {Error} If the menu file cannot be fetched or is not a JSON array.
 */
export async function loadMenuItems() {
  // BASE_URL keeps the path correct if the app is ever served from a sub-path.
  const response = await fetch(`${import.meta.env.BASE_URL}data/menu.json`);

  if (!response.ok) {
    throw new Error(`Could not load the menu (HTTP ${response.status}).`);
  }

  const parsedMenu = await response.json();

  if (!Array.isArray(parsedMenu)) {
    throw new Error('menu.json must contain an array of food items.');
  }

  return parsedMenu.map((item) => ({
    ...item,
    price: Number(item.price),
    quantity: Number(item.quantity),
  }));
}

/**
 * Produces the empty form values used when an admin adds a new food item.
 *
 * @returns {object} A food item shaped like the menu.json entries, without an id.
 */
export function createBlankFoodItem() {
  return {
    name: '',
    price: '',
    quantity: '',
    category: 'Snacks',
    description: '',
    emoji: '🍽️',
  };
}

/**
 * Picks the next free food ID, mirroring the manual ID entry in the Java admin
 * menu but without letting the admin collide with an existing item.
 *
 * @param {Array<{ id: number }>} menuItems - Current menu.
 * @returns {number} One higher than the largest existing id, or 1 if empty.
 */
export function getNextFoodItemId(menuItems) {
  if (menuItems.length === 0) {
    return 1;
  }

  return Math.max(...menuItems.map((item) => item.id)) + 1;
}
