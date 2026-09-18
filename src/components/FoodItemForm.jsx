/**
 * Modal form for adding a new food item or editing an existing one.
 *
 * Exports the default FoodItemForm component, which replaces the prompt-by-
 * prompt input loops in the Java admin menu ("Food Name:", "Price:", ...).
 * It validates locally and hands a ready-to-store object to its parent; the
 * parent assigns the id and decides whether this is an add or an update.
 */

import { useState } from 'react';
import { FOOD_CATEGORIES } from '../data/categories.js';

/**
 * Empty form values. Every field is defined so each input stays controlled,
 * even when an item is missing an optional one. No id: the server assigns it.
 */
const BLANK_FOOD_ITEM = {
  name: '',
  price: '',
  quantity: '',
  category: 'Snacks',
  description: '',
  emoji: '\u{1F37D}\uFE0F',
};

/**
 * @param {{
 *   existingItem: object | null,
 *   onSubmit: (foodItem: object) => void,
 *   onCancel: () => void,
 * }} props - The item being edited (null when adding), plus form handlers.
 * @returns {JSX.Element} A centred modal dialog.
 */
export default function FoodItemForm({ existingItem, onSubmit, onCancel }) {
  // Spreading over the blank item guarantees every field is defined, so each
  // input stays controlled even if an item is missing an optional field.
  const [formValues, setFormValues] = useState(() => ({
    ...BLANK_FOOD_ITEM,
    ...(existingItem ?? {}),
  }));
  const [validationError, setValidationError] = useState('');

  const isEditing = Boolean(existingItem);

  /**
   * Updates one field of the form.
   *
   * @param {string} fieldName - Key on the food item, e.g. "price".
   * @param {string} fieldValue - Raw input value, kept as a string until submit.
   * @returns {void}
   */
  function handleFieldChange(fieldName, fieldValue) {
    setFormValues((previousValues) => ({ ...previousValues, [fieldName]: fieldValue }));
  }

  /**
   * Validates the form and, when it passes, hands numeric values to the parent.
   *
   * @param {React.FormEvent<HTMLFormElement>} event - Form submit event.
   * @returns {void}
   */
  function handleSubmit(event) {
    event.preventDefault();

    const trimmedName = String(formValues.name).trim();
    const parsedPrice = Number(formValues.price);
    const parsedQuantity = Number(formValues.quantity);

    if (trimmedName === '') {
      setValidationError('Give the item a name.');
      return;
    }

    if (!Number.isFinite(parsedPrice) || parsedPrice <= 0) {
      setValidationError('Price must be a number greater than zero.');
      return;
    }

    if (!Number.isInteger(parsedQuantity) || parsedQuantity < 0) {
      setValidationError('Stock must be a whole number, zero or more.');
      return;
    }

    onSubmit({
      ...formValues,
      name: trimmedName,
      price: parsedPrice,
      quantity: parsedQuantity,
      // Older items ported from the Java seed data may lack these optional
      // fields, so fall back rather than trimming undefined.
      description: String(formValues.description ?? '').trim(),
      emoji: String(formValues.emoji ?? '').trim() || '🍽️',
    });
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button
        type="button"
        aria-label="Cancel"
        onClick={onCancel}
        className="absolute inset-0 bg-cocoa/50"
      />

      <form
        onSubmit={handleSubmit}
        aria-labelledby="food-form-heading"
        className="animate-rise relative max-h-[88vh] w-full max-w-lg overflow-y-auto rounded-3xl bg-shell p-6 shadow-lift sm:p-7"
      >
        <h2 id="food-form-heading" className="text-2xl font-semibold text-cocoa">
          {isEditing ? 'Edit food item' : 'Add food item'}
        </h2>
        <p className="mt-1 mb-6 text-sm text-bark">
          {isEditing
            ? `Updating "${existingItem.name}" (ID ${existingItem.id}). Price changes reach carts that already hold it.`
            : 'This item appears on the customer menu immediately. The server assigns its ID.'}
        </p>

        <div className="grid gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <label className="field-label" htmlFor="food-name">
              Name
            </label>
            <input
              id="food-name"
              className="field-input"
              value={formValues.name}
              onChange={(event) => handleFieldChange('name', event.target.value)}
              placeholder="Masala Dosa"
              required
            />
          </div>

          <div>
            <label className="field-label" htmlFor="food-price">
              Price (₹)
            </label>
            <input
              id="food-price"
              type="number"
              min="1"
              step="0.5"
              className="field-input"
              value={formValues.price}
              onChange={(event) => handleFieldChange('price', event.target.value)}
              placeholder="70"
              required
            />
          </div>

          <div>
            <label className="field-label" htmlFor="food-quantity">
              Available quantity
            </label>
            <input
              id="food-quantity"
              type="number"
              min="0"
              step="1"
              className="field-input"
              value={formValues.quantity}
              onChange={(event) => handleFieldChange('quantity', event.target.value)}
              aria-describedby="food-quantity-help"
              placeholder="20"
              required
            />
            <p id="food-quantity-help" className="mt-1.5 text-xs leading-snug text-bark">
              Units that can still be ordered. Anything already sitting in a
              customer&rsquo;s cart is reserved and is not counted here.
            </p>
          </div>

          <div>
            <label className="field-label" htmlFor="food-category">
              Category
            </label>
            <select
              id="food-category"
              className="field-input"
              value={formValues.category}
              onChange={(event) => handleFieldChange('category', event.target.value)}
            >
              {FOOD_CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="field-label" htmlFor="food-emoji">
              Icon
            </label>
            <input
              id="food-emoji"
              className="field-input"
              value={formValues.emoji}
              onChange={(event) => handleFieldChange('emoji', event.target.value)}
              maxLength={4}
              placeholder="🥞"
            />
          </div>

          <div className="sm:col-span-2">
            <label className="field-label" htmlFor="food-description">
              Description
            </label>
            <textarea
              id="food-description"
              rows={2}
              className="field-input resize-none"
              value={formValues.description}
              onChange={(event) => handleFieldChange('description', event.target.value)}
              placeholder="Golden rice crepe with potato masala."
            />
          </div>
        </div>

        {validationError && (
          <p
            role="alert"
            className="mt-4 rounded-xl border border-ember/30 bg-ember-soft px-4 py-2.5 text-sm text-ember"
          >
            {validationError}
          </p>
        )}

        <div className="mt-7 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <button type="button" onClick={onCancel} className="button-secondary">
            Cancel
          </button>
          <button type="submit" className="button-primary">
            {isEditing ? 'Save changes' : 'Add to menu'}
          </button>
        </div>
      </form>
    </div>
  );
}
