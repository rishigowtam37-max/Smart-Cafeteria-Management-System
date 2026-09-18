/**
 * Root component and router-free screen switch for FlowBite.
 *
 * Exports the default App component. It shows the login screen until the server
 * says somebody is signed in, then renders the admin or customer workspace for
 * the role the server reported - the same branch Main.java's switch made.
 *
 * The role comes from the session, not from anything the browser decides, and
 * the backend enforces it independently: a customer who somehow reached the
 * admin screen would get 403s from every request it made.
 */

import { useCafeteria } from './context/CafeteriaContext.jsx';
import LoginScreen from './components/LoginScreen.jsx';
import AppHeader from './components/AppHeader.jsx';
import CustomerView from './components/CustomerView.jsx';
import AdminView from './components/AdminView.jsx';

export default function App() {
  const { currentUser, isStartingUp, errorMessage, notice, dismissError, dismissNotice } =
    useCafeteria();

  // Briefly, on load, we do not yet know whether a session exists. Showing the
  // login screen during that moment would make a signed-in user flicker through
  // a screen they do not need.
  if (isStartingUp) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <p className="text-sm text-bark">Opening the cafeteria…</p>
      </div>
    );
  }

  if (!currentUser) {
    return <LoginScreen />;
  }

  return (
    <div className="min-h-screen">
      <AppHeader />

      <main className="mx-auto w-full max-w-7xl px-4 pb-32 pt-8 sm:px-6 lg:px-8 lg:pb-16">
        {errorMessage && (
          <div
            role="alert"
            className="mb-6 flex items-start justify-between gap-4 rounded-2xl border border-ember/30 bg-ember-soft px-4 py-3 text-sm text-ember"
          >
            <span>{errorMessage}</span>
            <button
              type="button"
              onClick={dismissError}
              aria-label="Dismiss error"
              className="shrink-0 font-semibold"
            >
              ×
            </button>
          </div>
        )}

        {notice && (
          <div
            role="status"
            className="mb-6 flex items-start justify-between gap-4 rounded-2xl border border-clay bg-sand/70 px-4 py-3 text-sm text-cocoa"
          >
            <span>{notice}</span>
            <button
              type="button"
              onClick={dismissNotice}
              aria-label="Dismiss notice"
              className="shrink-0 font-semibold"
            >
              ×
            </button>
          </div>
        )}

        {currentUser.role === 'admin' ? <AdminView /> : <CustomerView />}
      </main>
    </div>
  );
}
