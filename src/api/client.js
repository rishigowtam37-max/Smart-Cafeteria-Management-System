/**
 * The single place the browser talks to the FlowBite backend.
 *
 * Exports: ApiError, apiGet, apiPost, apiPatch, apiDelete.
 *
 * Every response the server refuses arrives as `{ message, status }`, and the
 * message is the one the Java domain threw - "Not enough stock available.",
 * "Your cart is empty." - so the UI shows the server's words rather than
 * inventing its own wording for a rule it does not enforce.
 *
 * Requests are same-origin: in development Vite proxies `/api` to :8080, and in
 * production the built app is served by Spring Boot. Either way the session
 * cookie travels on its own, so there is no token handling here.
 */

/** Longest a request may take before the UI stops waiting on it. */
const REQUEST_TIMEOUT_MS = 15_000;

/**
 * A failed request, carrying the server's message and HTTP status.
 *
 * `status` is 0 when the request never reached the server at all, which is how
 * the UI distinguishes "the cafeteria says no" from "the cafeteria is closed".
 */
export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }

  /** @returns {boolean} True when the backend could not be reached. */
  get isOffline() {
    return this.status === 0;
  }

  /** @returns {boolean} True when the session is gone and sign-in is required. */
  get isUnauthorized() {
    return this.status === 401;
  }
}

/**
 * Performs one request against the API.
 *
 * @param {string} method - HTTP method.
 * @param {string} path - Path below `/api`, e.g. "/cart".
 * @param {object} [body] - JSON body, omitted for GET and DELETE.
 * @returns {Promise<any>} The parsed response body, or null for 204.
 * @throws {ApiError} For any non-2xx response, or if the server is unreachable.
 */
async function request(method, path, body) {
  const timeoutController = new AbortController();
  const timeoutId = setTimeout(() => timeoutController.abort(), REQUEST_TIMEOUT_MS);

  let response;

  try {
    response = await fetch(`/api${path}`, {
      method,
      credentials: 'same-origin',
      signal: timeoutController.signal,
      headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (error) {
    // fetch only rejects when the request never completed - the server is down,
    // the network died, or the timeout above fired.
    throw new ApiError(
      error.name === 'AbortError'
        ? 'The cafeteria server took too long to answer.'
        : 'Cannot reach the cafeteria server. Is the backend running on port 8080?',
      0,
    );
  } finally {
    clearTimeout(timeoutId);
  }

  if (response.status === 204) {
    return null;
  }

  // An error page from something other than our handler (a proxy, say) may not
  // be JSON at all, so parsing is best-effort.
  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    // The backend always answers with {message, status}. A 5xx carrying no
    // message therefore did not come from the backend at all - it is the dev
    // proxy reporting that it could not reach it, which it does as a 500 rather
    // than by failing the request. Without this, the single most common problem
    // - forgetting to start the backend - would read as "HTTP 500".
    if (response.status >= 500 && !payload?.message) {
      throw new ApiError(
        'Cannot reach the cafeteria server. Is the backend running on port 8080?',
        0,
      );
    }

    throw new ApiError(
      payload?.message ?? `The request failed (HTTP ${response.status}).`,
      response.status,
    );
  }

  return payload;
}

export const apiGet = (path) => request('GET', path);
export const apiPost = (path, body) => request('POST', path, body ?? {});
export const apiPut = (path, body) => request('PUT', path, body ?? {});
export const apiPatch = (path, body) => request('PATCH', path, body ?? {});
export const apiDelete = (path) => request('DELETE', path);
