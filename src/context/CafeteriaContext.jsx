/**
 * Single application store for FlowBite, held entirely in React state.
 *
 * Exports: CafeteriaProvider (context provider component) and useCafeteria
 * (hook that reads the store).
 *
 * This file is the web equivalent of the four Java service classes combined:
 *   LoginService -> signIn / signOut / currentUser
 *   FoodService  -> menuItems / addFoodItem / updateFoodItem / deleteFoodItem
 *   CartService  -> cartItems / addToCart / changeCartQuantity / removeFromCart
 *   OrderService -> orders / placeOrder
 *
 * Stock rule (ported from CartService.addToCart): a customer can never hold
 * more units of an item than the menu has in stock. The Java app decremented
 * stock the moment an item entered the cart and restored it on removal; the web
 * app instead caps the cart against stock and decrements once the order is
 * actually placed, which is the same guarantee with less bookkeeping.
 */

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { DEMO_ACCOUNTS } from '../data/credentials.js';
import { loadMenuItems } from '../data/menuRepository.js';
import { calculateCartTotal, generateOrderId } from '../utils/orderUtils.js';

const CafeteriaContext = createContext(null);

/**
 * Wraps the app and supplies every piece of shared state plus the actions that
 * change it.
 *
 * @param {{ children: React.ReactNode }} props - Component props.
 * @returns {JSX.Element} Provider element.
 */
export function CafeteriaProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [cartItems, setCartItems] = useState([]);
  const [orders, setOrders] = useState([]);
  const [isMenuLoading, setIsMenuLoading] = useState(true);
  const [menuLoadError, setMenuLoadError] = useState('');

  // Load the seed menu once, on first mount.
  useEffect(() => {
    let isStillMounted = true;

    loadMenuItems()
      .then((items) => {
        if (isStillMounted) {
          setMenuItems(items);
        }
      })
      .catch((error) => {
        if (isStillMounted) {
          setMenuLoadError(error.message);
        }
      })
      .finally(() => {
        if (isStillMounted) {
          setIsMenuLoading(false);
        }
      });

    return () => {
      isStillMounted = false;
    };
  }, []);

  /**
   * Validates demo credentials for the chosen role.
   *
   * @param {'admin' | 'customer'} role - Role picked on the login screen.
   * @param {string} username - Typed username.
   * @param {string} password - Typed password.
   * @returns {{ ok: boolean, message?: string }} Success flag, plus an error
   *   message to show under the form when the credentials do not match.
   */
  const signIn = useCallback((role, username, password) => {
    const account = DEMO_ACCOUNTS[role];

    if (!account || account.username !== username.trim() || account.password !== password) {
      return { ok: false, message: `Invalid ${role} credentials. Check the demo hints below.` };
    }

    setCurrentUser({ role, name: account.displayName, username: account.username });
    return { ok: true };
  }, []);

  /** Logs the current user out and empties their in-progress cart. */
  const signOut = useCallback(() => {
    setCurrentUser(null);
    setCartItems([]);
  }, []);

  /**
   * Adds one unit of a food item to the cart, or increments it if already there.
   *
   * @param {object} foodItem - The menu item being ordered.
   * @returns {void}
   */
  const addToCart = useCallback((foodItem) => {
    setCartItems((previousCart) => {
      const existingLine = previousCart.find((line) => line.id === foodItem.id);

      if (!existingLine) {
        return foodItem.quantity > 0
          ? [...previousCart, { ...foodItem, quantity: 1, availableQuantity: foodItem.quantity }]
          : previousCart;
      }

      // Refuse to exceed stock, matching "Not enough stock available." in Java.
      if (existingLine.quantity >= foodItem.quantity) {
        return previousCart;
      }

      return previousCart.map((line) =>
        line.id === foodItem.id ? { ...line, quantity: line.quantity + 1 } : line,
      );
    });
  }, []);

  /**
   * Applies a relative change to a cart line's quantity, removing the line when
   * it drops to zero.
   *
   * @param {number} foodItemId - Which cart line to change.
   * @param {number} delta - +1 or -1.
   * @returns {void}
   */
  const changeCartQuantity = useCallback((foodItemId, delta) => {
    setCartItems((previousCart) =>
      previousCart
        .map((line) => {
          if (line.id !== foodItemId) {
            return line;
          }

          const requestedQuantity = line.quantity + delta;
          const cappedQuantity = Math.min(requestedQuantity, line.availableQuantity);
          return { ...line, quantity: cappedQuantity };
        })
        .filter((line) => line.quantity > 0),
    );
  }, []);

  /**
   * Drops a line from the cart entirely.
   *
   * @param {number} foodItemId - Which cart line to remove.
   * @returns {void}
   */
  const removeFromCart = useCallback((foodItemId) => {
    setCartItems((previousCart) => previousCart.filter((line) => line.id !== foodItemId));
  }, []);

  /** Empties the cart without placing an order. */
  const clearCart = useCallback(() => setCartItems([]), []);

  /**
   * Turns the current cart into an order, decrements menu stock and empties the
   * cart. Mirrors OrderService.placeOrder.
   *
   * @returns {object | null} The placed order, or null when the cart is empty.
   */
  const placeOrder = useCallback(() => {
    if (cartItems.length === 0) {
      return null;
    }

    const placedOrder = {
      orderId: generateOrderId(orders.length + 1),
      customerName: currentUser?.name ?? 'Guest',
      items: cartItems.map((line) => ({ ...line })),
      totalAmount: calculateCartTotal(cartItems),
      placedAt: Date.now(),
    };

    setOrders((previousOrders) => [placedOrder, ...previousOrders]);

    setMenuItems((previousMenu) =>
      previousMenu.map((menuItem) => {
        const orderedLine = cartItems.find((line) => line.id === menuItem.id);
        return orderedLine
          ? { ...menuItem, quantity: Math.max(0, menuItem.quantity - orderedLine.quantity) }
          : menuItem;
      }),
    );

    setCartItems([]);
    return placedOrder;
  }, [cartItems, currentUser, orders.length]);

  /**
   * Appends a new food item to the menu (admin only).
   *
   * @param {object} newFoodItem - Item already assigned an id by the caller.
   * @returns {void}
   */
  const addFoodItem = useCallback((newFoodItem) => {
    setMenuItems((previousMenu) => [...previousMenu, newFoodItem]);
  }, []);

  /**
   * Overwrites an existing food item, matched by id (admin only).
   *
   * @param {object} updatedFoodItem - Full replacement item, same id.
   * @returns {void}
   */
  const updateFoodItem = useCallback((updatedFoodItem) => {
    setMenuItems((previousMenu) =>
      previousMenu.map((item) => (item.id === updatedFoodItem.id ? updatedFoodItem : item)),
    );

    // Keep any cart line in sync so a customer never checks out a stale price.
    setCartItems((previousCart) =>
      previousCart.map((line) =>
        line.id === updatedFoodItem.id
          ? {
              ...line,
              name: updatedFoodItem.name,
              price: updatedFoodItem.price,
              category: updatedFoodItem.category,
              emoji: updatedFoodItem.emoji,
              availableQuantity: updatedFoodItem.quantity,
              quantity: Math.min(line.quantity, updatedFoodItem.quantity),
            }
          : line,
      ).filter((line) => line.quantity > 0),
    );
  }, []);

  /**
   * Removes a food item from the menu and from any cart holding it.
   *
   * @param {number} foodItemId - Item to delete.
   * @returns {void}
   */
  const deleteFoodItem = useCallback((foodItemId) => {
    setMenuItems((previousMenu) => previousMenu.filter((item) => item.id !== foodItemId));
    setCartItems((previousCart) => previousCart.filter((line) => line.id !== foodItemId));
  }, []);

  const contextValue = useMemo(
    () => ({
      currentUser,
      menuItems,
      cartItems,
      orders,
      isMenuLoading,
      menuLoadError,
      signIn,
      signOut,
      addToCart,
      changeCartQuantity,
      removeFromCart,
      clearCart,
      placeOrder,
      addFoodItem,
      updateFoodItem,
      deleteFoodItem,
    }),
    [
      currentUser,
      menuItems,
      cartItems,
      orders,
      isMenuLoading,
      menuLoadError,
      signIn,
      signOut,
      addToCart,
      changeCartQuantity,
      removeFromCart,
      clearCart,
      placeOrder,
      addFoodItem,
      updateFoodItem,
      deleteFoodItem,
    ],
  );

  return <CafeteriaContext.Provider value={contextValue}>{children}</CafeteriaContext.Provider>;
}

/**
 * Reads the cafeteria store.
 *
 * @returns {object} The context value supplied by CafeteriaProvider.
 * @throws {Error} If called from a component outside the provider.
 */
export function useCafeteria() {
  const contextValue = useContext(CafeteriaContext);

  if (contextValue === null) {
    throw new Error('useCafeteria must be used inside a <CafeteriaProvider>.');
  }

  return contextValue;
}
