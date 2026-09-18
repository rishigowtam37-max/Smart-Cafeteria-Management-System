/**
 * The application store - now a cache of what the server said, not a rulebook.
 *
 * Exports: CafeteriaProvider and the useCafeteria hook.
 *
 * This file used to be the whole domain: it validated credentials, capped cart
 * quantities against stock, summed totals and generated FB-0001 order IDs. All
 * of that now lives in the Spring Boot application, in the Java classes the
 * console app has always used. What is left here is the part that genuinely
 * belongs to a browser: hold the last answer, know when a request is in flight,
 * and show what went wrong.
 *
 * The rule this file follows: **never compute, only display.** Every total and
 * every stock number below arrived over the wire.
 */

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ApiError } from '../api/client.js';
import * as api from '../api/flowbiteApi.js';

const CafeteriaContext = createContext(null);

/** An empty cart, used before the first fetch and after signing out. */
const EMPTY_CART = { lines: [], total: 0, itemCount: 0, notice: null };

export function CafeteriaProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [cart, setCart] = useState(EMPTY_CART);
  const [orders, setOrders] = useState([]);
  const [adminStats, setAdminStats] = useState(null);

  // Which optional features this server has configured, from /api/config.
  const [serverConfig, setServerConfig] = useState({ smartCravingEnabled: false });

  // True only while the app is working out whether a session already exists.
  const [isStartingUp, setIsStartingUp] = useState(true);
  const [isMenuLoading, setIsMenuLoading] = useState(false);
  const [pendingActions, setPendingActions] = useState(0);

  const [errorMessage, setErrorMessage] = useState('');
  const [notice, setNotice] = useState('');

  const isBusy = pendingActions > 0;

  /**
   * Runs an API call with the shared error handling every action needs.
   *
   * A 401 means the session is gone - expired, or signed out in another tab -
   * so the user is dropped back to the login screen rather than left clicking
   * buttons that will all fail.
   *
   * @param {() => Promise<T>} call - The API call to run.
   * @returns {Promise<T | undefined>} The result, or undefined if it failed.
   * @template T
   */
  const run = useCallback(async (call) => {
    setPendingActions((count) => count + 1);
    setErrorMessage('');

    try {
      return await call();
    } catch (error) {
      if (!(error instanceof ApiError)) {
        throw error;
      }

      if (error.isUnauthorized) {
        setCurrentUser(null);
        setCart(EMPTY_CART);
        setMenuItems([]);
        setOrders([]);
        setAdminStats(null);
      }

      setErrorMessage(error.message);
      return undefined;
    } finally {
      setPendingActions((count) => count - 1);
    }
  }, []);

  /**
   * Stores a cart response, surfacing the server's notice if it sent one.
   *
   * The notice appears when an admin deleted something that was sitting in this
   * cart - the server drops the line and says so.
   *
   * @param {object | undefined} cartDto - A cart from the API.
   * @returns {void}
   */
  const applyCart = useCallback((cartDto) => {
    if (!cartDto) {
      return;
    }

    setCart(cartDto);

    if (cartDto.notice) {
      setNotice(cartDto.notice);
    }
  }, []);

  /** Refetches the menu. Called after anything that can move stock. */
  const refreshMenu = useCallback(async () => {
    const items = await run(api.fetchMenu);

    if (items) {
      setMenuItems(items);
    }
  }, [run]);

  /** Refetches just the dashboard totals (admin only). */
  const refreshStats = useCallback(async () => {
    const stats = await run(api.fetchAdminStats);

    if (stats) {
      setAdminStats(stats);
    }
  }, [run]);

  /** Refetches the order list and the dashboard totals (admin only). */
  const refreshOrders = useCallback(async () => {
    const [placedOrders, stats] = await Promise.all([
      run(api.fetchOrders),
      run(api.fetchAdminStats),
    ]);

    if (placedOrders) {
      setOrders(placedOrders);
    }

    if (stats) {
      setAdminStats(stats);
    }
  }, [run]);

  /**
   * Loads everything a signed-in user needs.
   *
   * @param {{role: string}} user - The signed-in user.
   * @returns {Promise<void>}
   */
  const loadWorkspace = useCallback(
    async (user) => {
      setIsMenuLoading(true);

      try {
        const items = await run(api.fetchMenu);

        if (items) {
          setMenuItems(items);
        }

        if (user.role === 'customer') {
          applyCart(await run(api.fetchCart));
        } else {
          await refreshOrders();
        }
      } finally {
        setIsMenuLoading(false);
      }
    },
    [run, applyCart, refreshOrders],
  );

  // On load, ask the server whether this browser already has a session. A 401
  // here is the normal "not signed in" answer, not an error worth showing.
  useEffect(() => {
    let isStillMounted = true;

    (async () => {
      // The feature flags are fetched first and separately: they are readable
      // without a session, and a failure here must not stop the app loading.
      try {
        const config = await api.fetchConfig();

        if (isStillMounted && config) {
          setServerConfig(config);
        }
      } catch {
        // Leave the defaults - features stay hidden rather than half-working.
      }

      try {
        const user = await api.fetchCurrentUser();

        if (isStillMounted && user) {
          setCurrentUser(user);
          await loadWorkspace(user);
        }
      } catch (error) {
        if (isStillMounted && error instanceof ApiError && !error.isUnauthorized) {
          setErrorMessage(error.message);
        }
      } finally {
        if (isStillMounted) {
          setIsStartingUp(false);
        }
      }
    })();

    return () => {
      isStillMounted = false;
    };
  }, [loadWorkspace]);

  /* ------------------------------------------------------------------ auth */

  /**
   * Signs in against the server.
   *
   * The credentials are checked by the Java LoginService - this function has no
   * idea what a valid password looks like.
   *
   * @param {'admin' | 'customer'} role
   * @param {string} username
   * @param {string} password
   * @returns {Promise<{ok: boolean, message?: string}>}
   */
  const signIn = useCallback(
    async (role, username, password) => {
      try {
        const user = await api.signIn(role, username, password);

        setCurrentUser(user);
        setErrorMessage('');
        await loadWorkspace(user);

        return { ok: true };
      } catch (error) {
        if (error instanceof ApiError) {
          return { ok: false, message: error.message };
        }

        throw error;
      }
    },
    [loadWorkspace],
  );

  /** Signs out, which also returns any stock held in the cart. */
  const signOut = useCallback(async () => {
    await run(api.signOut);

    setCurrentUser(null);
    setCart(EMPTY_CART);
    setMenuItems([]);
    setOrders([]);
    setAdminStats(null);
    setNotice('');
  }, [run]);

  /* ------------------------------------------------------------------ cart */

  /**
   * Adds units to the cart.
   *
   * The menu is refetched afterwards because adding reserves stock, so every
   * other item's availability may now read differently - including for other
   * customers.
   *
   * @param {object} foodItem - The menu item to order.
   * @param {number} [quantity] - Units to add, default 1.
   * @returns {Promise<void>}
   */
  const addToCart = useCallback(
    async (foodItem, quantity = 1) => {
      const updatedCart = await run(() => api.addToCart(foodItem.id, quantity));

      if (updatedCart) {
        applyCart(updatedCart);
        await refreshMenu();
      }
    },
    [run, applyCart, refreshMenu],
  );

  /**
   * Sets a cart line to an absolute quantity. Zero removes the line.
   *
   * @param {number} foodId
   * @param {number} quantity
   * @returns {Promise<void>}
   */
  const setCartQuantity = useCallback(
    async (foodId, quantity) => {
      const updatedCart = await run(() => api.changeCartQuantity(foodId, quantity));

      if (updatedCart) {
        applyCart(updatedCart);
        await refreshMenu();
      }
    },
    [run, applyCart, refreshMenu],
  );

  const removeFromCart = useCallback(
    async (foodId) => {
      const updatedCart = await run(() => api.removeFromCart(foodId));

      if (updatedCart) {
        applyCart(updatedCart);
        await refreshMenu();
      }
    },
    [run, applyCart, refreshMenu],
  );

  const clearCart = useCallback(async () => {
    const updatedCart = await run(api.clearCart);

    if (updatedCart) {
      applyCart(updatedCart);
      await refreshMenu();
    }
  }, [run, applyCart, refreshMenu]);

  /**
   * Places the order.
   *
   * @returns {Promise<object | null>} The placed order, or null if refused -
   *   an empty cart, most likely, which the server reports as a 409.
   */
  const placeOrder = useCallback(async () => {
    const placedOrder = await run(api.placeOrder);

    if (!placedOrder) {
      return null;
    }

    setCart(EMPTY_CART);
    await refreshMenu();

    return placedOrder;
  }, [run, refreshMenu]);

  /* ----------------------------------------------------------------- admin */

  const addFoodItem = useCallback(
    async (foodItem) => {
      const created = await run(() => api.createMenuItem(foodItem));

      if (created) {
        await Promise.all([refreshMenu(), refreshStats()]);
      }

      return created ?? null;
    },
    [run, refreshMenu, refreshStats],
  );

  const updateFoodItem = useCallback(
    async (id, foodItem) => {
      const updated = await run(() => api.updateMenuItem(id, foodItem));

      if (updated) {
        await Promise.all([refreshMenu(), refreshStats()]);
      }

      return updated ?? null;
    },
    [run, refreshMenu, refreshStats],
  );

  const deleteFoodItem = useCallback(
    async (id) => {
      await run(() => api.deleteMenuItem(id));
      await Promise.all([refreshMenu(), refreshStats()]);
    },
    [run, refreshMenu, refreshStats],
  );

  /* ----------------------------------------------------------------- misc */

  const dismissError = useCallback(() => setErrorMessage(''), []);
  const dismissNotice = useCallback(() => setNotice(''), []);

  const contextValue = useMemo(
    () => ({
      currentUser,
      menuItems,
      cart,
      orders,
      adminStats,
      serverConfig,
      isStartingUp,
      isMenuLoading,
      isBusy,
      errorMessage,
      notice,
      signIn,
      signOut,
      addToCart,
      setCartQuantity,
      removeFromCart,
      clearCart,
      placeOrder,
      addFoodItem,
      updateFoodItem,
      deleteFoodItem,
      refreshMenu,
      refreshOrders,
      refreshStats,
      dismissError,
      dismissNotice,
    }),
    [
      currentUser,
      menuItems,
      cart,
      orders,
      adminStats,
      serverConfig,
      isStartingUp,
      isMenuLoading,
      isBusy,
      errorMessage,
      notice,
      signIn,
      signOut,
      addToCart,
      setCartQuantity,
      removeFromCart,
      clearCart,
      placeOrder,
      addFoodItem,
      updateFoodItem,
      deleteFoodItem,
      refreshMenu,
      refreshOrders,
      refreshStats,
      dismissError,
      dismissNotice,
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
