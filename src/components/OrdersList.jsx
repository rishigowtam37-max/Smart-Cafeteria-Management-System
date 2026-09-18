/**
 * Admin view of every order placed during the session, newest first.
 *
 * Exports the default OrdersList component - the web version of
 * OrderService.displayOrders(), which printed each Order's ASCII bill.
 */

import { formatOrderTime, formatRupees } from '../utils/formatters.js';

/**
 * @param {{
 *   orders: Array<object>,
 *   onRefresh?: () => void,
 *   isRefreshing?: boolean,
 * }} props - Orders from the server, newest first, plus a manual refresh.
 * @returns {JSX.Element} One card per order, or an empty state.
 */
export default function OrdersList({
  orders,
  onRefresh,
  isRefreshing = false,
}) {
  // Orders are placed by customers in other browsers, so this list is a snapshot
  // taken when the tab was opened rather than a live feed.
  const refreshButton = onRefresh && (
    <div className="mb-4 flex justify-end">
      <button
        type="button"
        onClick={onRefresh}
        disabled={isRefreshing}
        className="button-secondary"
      >
        {isRefreshing ? 'Refreshing…' : 'Refresh'}
      </button>
    </div>
  );

  if (orders.length === 0) {
    return (
      <>
        {refreshButton}
        <div className="surface-card px-6 py-16 text-center">
          <p className="text-3xl">🧾</p>
          <p className="mt-3 text-sm text-bark">
            No orders placed yet. Sign in as a customer in another browser to
            try it out.
          </p>
        </div>
      </>
    );
  }

  return (
    <>
      {refreshButton}
      <div className="grid gap-4 lg:grid-cols-2">
        {orders.map((order) => (
          <article key={order.orderCode} className="surface-card p-5">
            <header className="flex items-start justify-between gap-3 border-b border-clay/50 pb-4">
              <div>
                <p className="text-sm font-semibold tracking-wide text-saffron">
                  {order.orderCode}
                </p>
                <p className="mt-1 text-base font-semibold text-cocoa">
                  {order.customerName}
                </p>
              </div>

              <div className="text-right">
                <p className="text-xs text-bark">
                  {formatOrderTime(order.placedAt)}
                </p>
                <p className="mt-1 text-xs text-bark">
                  {order.itemCount} items
                </p>
              </div>
            </header>

            <ul className="divide-y divide-clay/40 py-1">
              {order.items.map((item) => (
                <li
                  key={item.foodId}
                  className="flex items-center justify-between gap-3 py-2.5 text-sm"
                >
                  <span className="min-w-0 truncate text-cocoa">
                    <span className="mr-2 text-bark tabular-nums">
                      {item.quantity}×
                    </span>
                    {item.name}
                  </span>
                  <span className="shrink-0 text-bark tabular-nums">
                    {formatRupees(item.lineTotal)}
                  </span>
                </li>
              ))}
            </ul>

            <footer className="flex items-baseline justify-between border-t border-clay/50 pt-4">
              <span className="text-sm font-medium text-bark">Total</span>
              <span className="text-lg font-semibold text-cocoa">
                {formatRupees(order.totalAmount)}
              </span>
            </footer>
          </article>
        ))}
      </div>
    </>
  );
}
