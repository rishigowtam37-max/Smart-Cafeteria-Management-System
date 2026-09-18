/**
 * Sticky top bar shown on every signed-in screen.
 *
 * Exports the default AppHeader component: brand mark, the signed-in user's
 * name and role badge, and the sign-out button (the Java "Logout" menu option).
 */

import { useCafeteria } from '../context/CafeteriaContext.jsx';

export default function AppHeader() {
  const { currentUser, signOut, isBusy } = useCafeteria();
  const isAdmin = currentUser.role === 'admin';

  return (
    <header className="sticky top-0 z-30 border-b border-clay/60 bg-cream/85 backdrop-blur">
      <div className="mx-auto flex w-full max-w-7xl items-center justify-between gap-4 px-4 py-3.5 sm:px-6 lg:px-8">
        <div className="flex items-center gap-3">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-saffron text-lg">
            🍲
          </span>
          <div className="leading-tight">
            <p className="text-lg font-semibold text-cocoa">FlowBite</p>
            <p className="hidden text-xs text-bark sm:block">Campus Cafeteria</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className="text-right leading-tight">
            <p className="text-sm font-semibold text-cocoa">{currentUser.name}</p>
            <span
              className={`text-xs font-medium uppercase tracking-wide ${
                isAdmin ? 'text-saffron' : 'text-bark'
              }`}
            >
              {isAdmin ? 'Admin' : 'Customer'}
            </span>
          </div>

          {/* Signing out also releases any stock this cart was holding. */}
          <button type="button" onClick={signOut} disabled={isBusy} className="button-quiet">
            Sign out
          </button>
        </div>
      </div>
    </header>
  );
}
