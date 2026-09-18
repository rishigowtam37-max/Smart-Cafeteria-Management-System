/**
 * Smart Craving: the "what are you in the mood for?" box above the menu.
 *
 * Exports the default SmartCravingBar component. It owns the three pieces of
 * state this feature needs - the typed craving, the request status and the
 * suggestion (or error) that came back - and hands the result to
 * CravingSuggestions to draw.
 *
 * All it does is post the text to /api/craving. The prompt, the Gemini call and
 * the validation that every suggested id is a real in-stock item all live in the
 * Java CravingService. That is also where the API key lives, which is the point:
 * this file could not leak it if it tried.
 *
 * The box hides itself when the server reports the feature as off, so a clone
 * without a key gets the whole ordering app minus this panel.
 */

import { useEffect, useRef, useState } from 'react';
import { suggestFromCraving } from '../api/flowbiteApi.js';
import { ApiError } from '../api/client.js';
import CravingSuggestions from './CravingSuggestions.jsx';

/** Matches the server-side cap in CravingService.MAX_CRAVING_LENGTH. */
const CRAVING_MAX_LENGTH = 200;

/** One-tap starter cravings shown as chips under the input. */
const CRAVING_EXAMPLES = [
  'Something spicy',
  'Light and healthy',
  'Very hungry',
  'Under ₹100',
  'Something sweet',
];

/**
 * @param {{ enabled: boolean }} props - Whether the server has Smart Craving
 *   configured, from /api/config.
 * @returns {JSX.Element | null} The panel, or nothing when the feature is off.
 */
export default function SmartCravingBar({ enabled }) {
  const [craving, setCraving] = useState('');
  const [isThinking, setIsThinking] = useState(false);
  const [suggestion, setSuggestion] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');

  // Cancels a reply that arrives after the component has gone away.
  const isMounted = useRef(true);

  useEffect(() => {
    isMounted.current = true;
    return () => {
      isMounted.current = false;
    };
  }, []);

  if (!enabled) {
    return null;
  }

  /**
   * Asks the server for picks matching the given craving text.
   *
   * @param {string} cravingText - What to search the menu for.
   * @returns {Promise<void>}
   */
  async function requestSuggestions(cravingText) {
    if (cravingText.trim() === '' || isThinking) {
      return;
    }

    setIsThinking(true);
    setErrorMessage('');
    setSuggestion(null);

    try {
      const result = await suggestFromCraving(cravingText);

      if (isMounted.current) {
        setSuggestion(result);
      }
    } catch (error) {
      if (!(error instanceof ApiError)) {
        throw error;
      }

      if (isMounted.current) {
        setErrorMessage(error.message);
      }
    } finally {
      if (isMounted.current) {
        setIsThinking(false);
      }
    }
  }

  /**
   * @param {React.FormEvent} event - The form submission.
   * @returns {void}
   */
  function handleSubmit(event) {
    event.preventDefault();
    requestSuggestions(craving);
  }

  /**
   * Fills the input from a chip and searches for it in one tap.
   *
   * @param {string} example - The chip's text.
   * @returns {void}
   */
  function handleExampleClick(example) {
    setCraving(example);
    requestSuggestions(example);
  }

  /** Clears the suggestion and the input, returning the box to rest. */
  function handleDismiss() {
    setSuggestion(null);
    setErrorMessage('');
    setCraving('');
  }

  return (
    <section className="surface-card mb-6 p-5" aria-labelledby="smart-craving-heading">
      <div className="flex items-center gap-2">
        <span aria-hidden="true" className="text-lg">
          ✨
        </span>
        <h2 id="smart-craving-heading" className="text-lg font-semibold text-cocoa">
          Smart Craving
        </h2>
        <span className="rounded-full bg-sand px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wider text-bark">
          AI
        </span>
      </div>

      <p className="mt-1 text-sm text-bark">
        Not sure what to order? Describe the mood and we&rsquo;ll pick from today&rsquo;s menu.
      </p>

      <form onSubmit={handleSubmit} className="mt-4 flex flex-col gap-2 sm:flex-row">
        <label htmlFor="craving-input" className="sr-only">
          What are you craving?
        </label>
        <input
          id="craving-input"
          type="text"
          value={craving}
          onChange={(event) => setCraving(event.target.value)}
          maxLength={CRAVING_MAX_LENGTH}
          placeholder="Something spicy and filling, under ₹100…"
          disabled={isThinking}
          className="field-input flex-1 disabled:opacity-60"
        />
        <button
          type="submit"
          disabled={isThinking || craving.trim() === ''}
          className="button-primary shrink-0 sm:w-40"
        >
          {isThinking ? 'Thinking…' : 'Suggest for me'}
        </button>
      </form>

      <div className="mt-3 flex flex-wrap gap-2">
        {CRAVING_EXAMPLES.map((example) => (
          <button
            key={example}
            type="button"
            onClick={() => handleExampleClick(example)}
            disabled={isThinking}
            className="rounded-full border border-clay bg-cream/70 px-3 py-1 text-xs font-medium text-bark transition hover:border-saffron hover:text-saffron disabled:opacity-50 disabled:hover:border-clay disabled:hover:text-bark"
          >
            {example}
          </button>
        ))}
      </div>

      {/* One live region for every outcome, so a screen reader hears the result
          of a search it did not visually observe. */}
      <div aria-live="polite">
        {isThinking && (
          <p className="mt-4 animate-pulse text-sm text-bark">Reading the menu for you…</p>
        )}

        {errorMessage && (
          <p className="mt-4 rounded-2xl border border-ember/30 bg-ember-soft px-4 py-3 text-sm text-ember">
            {errorMessage}
          </p>
        )}

        {suggestion && !isThinking && (
          <CravingSuggestions
            headline={suggestion.headline}
            picks={suggestion.picks}
            onDismiss={handleDismiss}
          />
        )}
      </div>
    </section>
  );
}
