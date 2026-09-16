/**
 * Root component and router-free screen switch for FlowBite.
 *
 * Exports the default App component, which shows the login screen until
 * someone signs in, then renders the admin or customer workspace based on the
 * signed-in role - the same branch the Java Main.java switch statement made.
 */

import { useCafeteria } from './context/CafeteriaContext.jsx';
import LoginScreen from './components/LoginScreen.jsx';
import AppHeader from './components/AppHeader.jsx';
import CustomerView from './components/CustomerView.jsx';
import AdminView from './components/AdminView.jsx';

export default function App() {
  const { currentUser, menuLoadError } = useCafeteria();

  if (!currentUser) {
    return <LoginScreen />;
  }

  return (
    <div className="min-h-screen">
      <AppHeader />

      <main className="mx-auto w-full max-w-7xl px-4 pb-32 pt-8 sm:px-6 lg:px-8 lg:pb-16">
        {menuLoadError && (
          <p className="mb-6 rounded-2xl border border-ember/30 bg-ember-soft px-4 py-3 text-sm text-ember">
            {menuLoadError}
          </p>
        )}

        {currentUser.role === 'admin' ? <AdminView /> : <CustomerView />}
      </main>
    </div>
  );
}
