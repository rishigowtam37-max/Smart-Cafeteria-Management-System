/**
 * Full-page sign-in screen with role selection.
 *
 * Exports the default LoginScreen component. It replaces the "1. Admin Login /
 * 2. Customer Login" branch of the Java main menu: pick a role, type the demo
 * credentials, and the server decides.
 *
 * Nothing here knows what a valid password is. The form posts to
 * /api/auth/login, the Java LoginService checks it, and a refusal comes back as
 * a 401 whose message is shown below the form.
 */

import { useState } from 'react';
import { useCafeteria } from '../context/CafeteriaContext.jsx';

/**
 * Placeholder and hint text for the two demo accounts.
 *
 * These are display hints only - the real credentials live in the Java
 * LoginService constructor and are checked there. Changing a value here changes
 * what the hint says, not what the server accepts.
 */
const ROLE_OPTIONS = [
  {
    role: 'customer',
    label: 'Customer',
    caption: 'Browse the menu and order',
    hintUsername: 'customer',
    hintPassword: 'cust123',
  },
  {
    role: 'admin',
    label: 'Admin',
    caption: 'Manage menu and orders',
    hintUsername: 'admin',
    hintPassword: 'admin123',
  },
];

export default function LoginScreen() {
  const { signIn } = useCafeteria();
  const [selectedRole, setSelectedRole] = useState('customer');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isSigningIn, setIsSigningIn] = useState(false);

  /**
   * Sends the typed credentials to the server.
   *
   * @param {React.FormEvent<HTMLFormElement>} event - Form submit event.
   * @returns {Promise<void>}
   */
  async function handleSubmit(event) {
    event.preventDefault();
    setIsSigningIn(true);
    setErrorMessage('');

    try {
      const result = await signIn(selectedRole, username, password);

      if (!result.ok) {
        setErrorMessage(result.message);
      }
    } finally {
      setIsSigningIn(false);
    }
  }

  /**
   * Switches role, clearing any typed credentials so the two demo logins never
   * get mixed up.
   *
   * @param {'admin' | 'customer'} role - Newly selected role.
   * @returns {void}
   */
  function handleRoleChange(role) {
    setSelectedRole(role);
    setUsername('');
    setPassword('');
    setErrorMessage('');
  }

  const demoAccount = ROLE_OPTIONS.find((option) => option.role === selectedRole);

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <span className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-saffron text-2xl shadow-lift">
            🍲
          </span>
          <h1 className="mt-5 text-4xl font-semibold text-cocoa">FlowBite</h1>
          <p className="mt-2 text-sm leading-relaxed text-bark">
            Seamless food ordering with smart menu suggestions
            <br className="hidden sm:block" /> and order coordination.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="surface-card p-6 sm:p-8">
          <fieldset className="mb-6">
            <legend className="field-label">Sign in as</legend>
            <div className="grid grid-cols-2 gap-3">
              {ROLE_OPTIONS.map((option) => {
                const isSelected = selectedRole === option.role;

                return (
                  <button
                    key={option.role}
                    type="button"
                    onClick={() => handleRoleChange(option.role)}
                    aria-pressed={isSelected}
                    className={`rounded-2xl border px-4 py-3 text-left transition ${
                      isSelected
                        ? 'border-saffron bg-saffron-soft'
                        : 'border-clay bg-cream/50 hover:border-bark'
                    }`}
                  >
                    <span className="block text-sm font-semibold text-cocoa">{option.label}</span>
                    <span className="mt-0.5 block text-xs leading-snug text-bark">
                      {option.caption}
                    </span>
                  </button>
                );
              })}
            </div>
          </fieldset>

          <div className="mb-4">
            <label className="field-label" htmlFor="username">
              Username
            </label>
            <input
              id="username"
              className="field-input"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder={demoAccount.hintUsername}
              autoComplete="username"
              disabled={isSigningIn}
              required
            />
          </div>

          <div className="mb-6">
            <label className="field-label" htmlFor="password">
              Password
            </label>
            <input
              id="password"
              type="password"
              className="field-input"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="••••••••"
              autoComplete="current-password"
              required
            />
          </div>

          {errorMessage && (
            <p
              role="alert"
              className="mb-4 rounded-xl border border-ember/30 bg-ember-soft px-4 py-2.5 text-sm text-ember"
            >
              {errorMessage}
            </p>
          )}

          <button type="submit" disabled={isSigningIn} className="button-primary w-full">
            {isSigningIn ? 'Signing in…' : 'Sign in'}
          </button>

          <p className="mt-6 rounded-xl bg-sand/70 px-4 py-3 text-center text-xs leading-relaxed text-bark">
            Demo credentials ·{' '}
            <span className="font-semibold text-cocoa">{demoAccount.hintUsername}</span> /{' '}
            <span className="font-semibold text-cocoa">{demoAccount.hintPassword}</span>
          </p>
        </form>
      </div>
    </div>
  );
}
