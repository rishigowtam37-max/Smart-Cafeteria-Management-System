/**
 * Customer workspace: menu browsing on the left, cart on the right.
 *
 * Exports the default CustomerView component. It owns the three pieces of
 * screen-local state that no other view needs - the active category filter,
 * whether the mobile cart drawer is open, and the most recently placed order -
 * and composes SmartCravingBar, CategoryFilter, MenuGrid, CartPanel and
 * OrderConfirmation. SmartCravingBar renders itself away when the server has
 * no Gemini key configured, so this layout is unchanged for anyone without one.
 *
 * Layout: on large screens the cart is a sticky sidebar; below `lg` it
 * collapses into a bottom sheet opened from a fixed summary bar.
 */

import { useMemo, useState } from 'react';
import { useCafeteria } from '../context/CafeteriaContext.jsx';
import { formatRupees } from '../utils/formatters.js';
import SmartCravingBar from './SmartCravingBar.jsx';
import CategoryFilter from './CategoryFilter.jsx';
import MenuGrid from './MenuGrid.jsx';
import CartPanel from './CartPanel.jsx';
import OrderConfirmation from './OrderConfirmation.jsx';

const ALL_CATEGORIES = 'All';

export default function CustomerView() {
  const { menuItems, cart, isMenuLoading, placeOrder, serverConfig } = useCafeteria();
  const [activeCategory, setActiveCategory] = useState(ALL_CATEGORIES);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [confirmedOrder, setConfirmedOrder] = useState(null);
  const [isPlacingOrder, setIsPlacingOrder] = useState(false);

  // Categories come from the live menu, so admin-added ones appear here too.
  const availableCategories = useMemo(() => {
    const uniqueCategories = [...new Set(menuItems.map((item) => item.category))].sort();
    return [ALL_CATEGORIES, ...uniqueCategories];
  }, [menuItems]);

  const visibleMenuItems = useMemo(
    () =>
      activeCategory === ALL_CATEGORIES
        ? menuItems
        : menuItems.filter((item) => item.category === activeCategory),
    [menuItems, activeCategory],
  );

  // Both come from the server's cart - nothing is summed here.
  const cartTotal = cart.total;
  const cartItemCount = cart.itemCount;

  /**
   * Asks the server to turn the cart into an order, then shows the receipt.
   *
   * A null result means the server refused - an empty cart, most likely - and
   * the reason is already on screen in the error banner.
   *
   * @returns {Promise<void>}
   */
  async function handlePlaceOrder() {
    setIsPlacingOrder(true);

    try {
      const placedOrder = await placeOrder();

      if (placedOrder) {
        setConfirmedOrder(placedOrder);
        setIsCartOpen(false);
      }
    } finally {
      setIsPlacingOrder(false);
    }
  }

  return (
    <>
      <div className="mb-8">
        <h1 className="text-3xl font-semibold text-cocoa sm:text-4xl">Today&rsquo;s Menu</h1>
        <p className="mt-2 max-w-xl text-sm leading-relaxed text-bark">
          Freshly prepared, served hot from the counter. Add what you like and place the order -
          we&rsquo;ll call your order ID when it&rsquo;s ready.
        </p>
      </div>

      <div className="grid gap-8 lg:grid-cols-[minmax(0,1fr)_22rem] lg:items-start">
        <div>
          <SmartCravingBar enabled={serverConfig.smartCravingEnabled} />

          <CategoryFilter
            categories={availableCategories}
            activeCategory={activeCategory}
            onCategoryChange={setActiveCategory}
          />

          {isMenuLoading ? (
            <p className="py-16 text-center text-sm text-bark">Loading the menu…</p>
          ) : (
            <MenuGrid menuItems={visibleMenuItems} />
          )}
        </div>

        {/* Desktop: sticky sidebar cart */}
        <aside className="hidden lg:sticky lg:top-24 lg:block">
          <CartPanel onPlaceOrder={handlePlaceOrder} isPlacingOrder={isPlacingOrder} />
        </aside>
      </div>

      {/* Mobile: fixed summary bar that opens the cart as a bottom sheet */}
      {cartItemCount > 0 && !isCartOpen && (
        <div className="fixed inset-x-0 bottom-0 z-30 border-t border-clay/60 bg-shell/95 p-4 backdrop-blur lg:hidden">
          <button
            type="button"
            onClick={() => setIsCartOpen(true)}
            className="button-primary w-full justify-between"
          >
            <span>
              View cart · {cartItemCount} {cartItemCount === 1 ? 'item' : 'items'}
            </span>
            <span>{formatRupees(cartTotal)}</span>
          </button>
        </div>
      )}

      {isCartOpen && (
        <div className="fixed inset-0 z-40 flex items-end lg:hidden">
          <button
            type="button"
            aria-label="Close cart"
            onClick={() => setIsCartOpen(false)}
            className="absolute inset-0 bg-cocoa/40"
          />
          <div className="animate-rise relative max-h-[85vh] w-full overflow-y-auto rounded-t-3xl bg-shell p-4 shadow-lift">
            <CartPanel
              onPlaceOrder={handlePlaceOrder}
              onClose={() => setIsCartOpen(false)}
              isPlacingOrder={isPlacingOrder}
            />
          </div>
        </div>
      )}

      {confirmedOrder && (
        <OrderConfirmation order={confirmedOrder} onDismiss={() => setConfirmedOrder(null)} />
      )}
    </>
  );
}
