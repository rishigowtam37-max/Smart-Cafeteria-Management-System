/**
 * Admin workspace with two tabs: menu management and placed orders.
 *
 * Exports the default AdminView component. It covers the Java admin menu
 * options - view menu, add food, update food, delete food and view orders -
 * and owns the tab selection plus which item (if any) is being edited.
 */

import { useState } from 'react';
import { useCafeteria } from '../context/CafeteriaContext.jsx';
import { getNextFoodItemId } from '../data/menuRepository.js';
import { formatRupees } from '../utils/formatters.js';
import MenuTable from './MenuTable.jsx';
import FoodItemForm from './FoodItemForm.jsx';
import OrdersList from './OrdersList.jsx';

const TABS = [
  { id: 'menu', label: 'Menu' },
  { id: 'orders', label: 'Orders' },
];

export default function AdminView() {
  const { menuItems, orders, addFoodItem, updateFoodItem, deleteFoodItem } = useCafeteria();
  const [activeTab, setActiveTab] = useState('menu');

  // null = form closed, 'new' = adding, an object = editing that item.
  const [formTarget, setFormTarget] = useState(null);

  const totalRevenue = orders.reduce((runningTotal, order) => runningTotal + order.totalAmount, 0);
  const soldOutCount = menuItems.filter((item) => item.quantity === 0).length;

  /**
   * Saves the form, either appending a new item or replacing an existing one.
   *
   * @param {object} submittedItem - Values from FoodItemForm, already numeric.
   * @returns {void}
   */
  function handleFormSubmit(submittedItem) {
    if (formTarget === 'new') {
      addFoodItem({ ...submittedItem, id: getNextFoodItemId(menuItems) });
    } else {
      updateFoodItem({ ...submittedItem, id: formTarget.id });
    }

    setFormTarget(null);
  }

  /**
   * Deletes a food item after an explicit confirmation, since the change cannot
   * be undone from the UI.
   *
   * @param {object} foodItem - Item the admin chose to delete.
   * @returns {void}
   */
  function handleDelete(foodItem) {
    const isConfirmed = window.confirm(`Delete "${foodItem.name}" from the menu?`);

    if (isConfirmed) {
      deleteFoodItem(foodItem.id);
    }
  }

  const summaryStats = [
    { label: 'Items on menu', value: menuItems.length },
    { label: 'Sold out', value: soldOutCount },
    { label: 'Orders placed', value: orders.length },
    { label: 'Revenue', value: formatRupees(totalRevenue) },
  ];

  return (
    <>
      <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-semibold text-cocoa sm:text-4xl">Admin Panel</h1>
          <p className="mt-2 text-sm text-bark">
            Manage the counter menu and keep an eye on incoming orders.
          </p>
        </div>

        {activeTab === 'menu' && (
          <button type="button" onClick={() => setFormTarget('new')} className="button-primary">
            + Add food item
          </button>
        )}
      </div>

      <div className="mb-8 grid grid-cols-2 gap-3 lg:grid-cols-4">
        {summaryStats.map((stat) => (
          <div key={stat.label} className="surface-card px-5 py-4">
            <p className="text-xs font-medium uppercase tracking-wide text-bark">{stat.label}</p>
            <p className="mt-1.5 text-2xl font-semibold text-cocoa">{stat.value}</p>
          </div>
        ))}
      </div>

      <div className="mb-6 inline-flex rounded-full border border-clay bg-shell p-1">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => setActiveTab(tab.id)}
            aria-pressed={activeTab === tab.id}
            className={`rounded-full px-5 py-2 text-sm font-semibold transition ${
              activeTab === tab.id ? 'bg-saffron text-white' : 'text-bark hover:text-cocoa'
            }`}
          >
            {tab.label}
            {tab.id === 'orders' && orders.length > 0 && (
              <span className="ml-1.5 text-xs opacity-80">({orders.length})</span>
            )}
          </button>
        ))}
      </div>

      {activeTab === 'menu' ? (
        <MenuTable menuItems={menuItems} onEdit={setFormTarget} onDelete={handleDelete} />
      ) : (
        <OrdersList orders={orders} />
      )}

      {formTarget && (
        <FoodItemForm
          existingItem={formTarget === 'new' ? null : formTarget}
          onSubmit={handleFormSubmit}
          onCancel={() => setFormTarget(null)}
        />
      )}
    </>
  );
}
