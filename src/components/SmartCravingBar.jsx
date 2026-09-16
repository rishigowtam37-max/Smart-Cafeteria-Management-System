/**
 * Smart Craving: the "what are you in the mood for?" box above the menu.
 *
 * Exports the default SmartCravingBar component. It owns the three pieces of
 * state this feature needs - the typed craving, the request status and the
 * suggestion (or error) that came back - and hands the result to
 * CravingSuggestions to draw.
 *
 * This state stays local rather than going into CafeteriaContext because
 * nothing outside the customer menu reads it; the store keeps holding only what
 * the Java services held.
 *
 * The whole component renders nothing when no API key is configured, so a fresh
 * clone without a .env still gets the complete ordering app, just without this.
 */

import { useEffect, useRef, useState } from 'react';
import { useCafeteria } from '../context/CafeteriaContext.jsx';
import {
  CRAVING_EXAMPLES,
  CRAVING_MAX_LENGTH,
  isSmartCravingConfigured,
  suggestFromCraving,
} from '../data/cravingClient.js';
import CravingSuggestions from './CravingSuggestions.jsx';

export default function SmartCravingBar() {
  const { menuItems, isMenuLoading } = useCafeteria();
  const [craving, setCraving] = useState('');
  const [isThinking, setIsThinking] = useState(false);
  const [suggestion, setSuggestion] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');

  // Lets a second submission cancel the first, and cancels on unmount so a
  // reply can never land on a component that has gone away.
  const inFlightRequest = useRef(null);

  useEffect(() => () => inFlightRequest.current?.abort(), []);

  if (!isSmartCravingConfigured()) {
    return null;
  }

  /**
   * Asks Gemini for picks matching the given craving text.
   *
   * @param {string} cravingText - What to search the menu for.
   * @returns {Promise<void>} Resolves once the state reflects the outcome.
   */
  async function requestSuggestions(cravingText) {
    if (cravingText.trim() === '' || isMenuLoading) {
      return;
    }

    inFlightRequest.current?.abort();
    const requestController = new AbortController();
    inFlightRequest.current = requestController;

    setIsThinking(true);
    setErrorMessage('');
    setSuggestion(null);

    try {
      const result = await suggestFromCraving({
        craving: cravingText,
        menuItems,
        signal: requestController.signal,
      });

      setSuggestion(result);
    } catch (error) {
      // A cancelled request was replaced by a newer one - say nothing.
      if (error.name !== 'AbortError') {
        setErrorMessage(error.message);
      }
    } finally {
      if (inFlightRequest.current === requestController) {
        inFlightRequest.current = null;
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

  /** Clears the suggestion and the input, returning the box to its resting state. */
  function handleDismiss() {
    inFlightRequest.current?.abort();
    inFlightRequest.current = null;
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
          disabled={isThinking || isMenuLoading || craving.trim() === ''}
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
          <p className="mt-4 animate-pulse text-sm text-bark">
            Reading the menu for you…
          </p>
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
