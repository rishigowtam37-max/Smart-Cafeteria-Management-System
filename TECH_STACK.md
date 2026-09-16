# FlowBite — Tech Stack

_Seamless Food Ordering with Smart Menu Suggestions and Order Coordination._

FlowBite is the web port of the original **Smart Cafeteria Management System** Java console app. The Java sources now live in **`legacy-java/`** — outside `src/` — as **read-only reference**. They define the domain model this app mirrors, and nothing in the web app imports or compiles them. Keeping them out of `src/` also removes the `Main.java`/`main.jsx` and `util/`/`utils/` ambiguity that made the tree confusing to read on a case-insensitive filesystem (Windows, macOS).

> **AI status:** this phase ships **one** AI feature — **Smart Craving**, a menu-suggestion box on the customer screen backed by the Google Gemini API. It is the only third-party call in the project, it is confined to `src/data/cravingClient.js`, and it disables itself cleanly when no API key is configured.

---

## Core technologies

| Technology | Why it's here / what it does in this project |
| --- | --- |
| **Vite 7** | Dev server and build tool. Gives instant start-up and hot module replacement while editing components, and produces the optimised static bundle for `npm run build`. Chosen over Create React App because it needs no config to serve `public/data/menu.json` and handle `.env` variables. |
| **React 19** | Renders every screen. The whole app is one React tree with no router — `App.jsx` switches between login, customer and admin views based on state, exactly as `Main.java` switched on the menu choice. |
| **react-dom** | Mounts the React tree into the real DOM through `createRoot` in `src/main.jsx`. |
| **Tailwind CSS 4** | All styling. The warm cafeteria palette is declared once as CSS custom properties in the `@theme` block of `src/index.css`, so `bg-cream`, `text-cocoa` and `bg-saffron` are project-specific utilities rather than generic grey defaults. |
| **@tailwindcss/vite** | Tailwind v4's official Vite plugin. Compiles the stylesheet during dev and build; replaces the old `tailwind.config.js` + PostCSS setup entirely, which is why this repo has neither file. |
| **@vitejs/plugin-react** | Transforms JSX into JavaScript and wires up React Fast Refresh so edits preserve component state. |
| **Plain JavaScript (ESM)** | No TypeScript, by requirement. Type intent is documented in JSDoc blocks instead, which editors still read for autocomplete. |

---

## Configuration and root files

| File | Why it's here / what it does |
| --- | --- |
| **`package.json`** | Declares the four dependencies above and the three scripts: `dev` (local server), `build` (production bundle), `preview` (serve the built bundle). |
| **`vite.config.js`** | Registers the React and Tailwind plugins and pins the dev server to port 5173. |
| **`index.html`** | The single HTML document Vite serves. Contains only `<div id="root">` and the module script that loads `src/main.jsx`. |
| **`.env.example`** | Tracked template listing `VITE_GEMINI_API_KEY=` and the optional `VITE_GEMINI_MODEL=`, both blank. Anyone cloning the repo copies it to `.env`. **Never holds a real key** — it is tracked by git. |
| **`.env`** | Local, untracked copy holding the actual Gemini key. Read by `src/data/cravingClient.js`. |
| **`.gitignore`** | Keeps `node_modules/`, `dist/`, `.env` and `.env.local` out of git while explicitly keeping `.env.example` tracked. Also retains the original `*.class` rule for the Java app. |
| **`public/data/menu.json`** | The menu, ported field-for-field from the seed list in `FoodService.java`. Lives in `public/` so it is served as a static file and can be edited without touching code. |

---

## Folder structure

| Folder | What lives there |
| --- | --- |
| **`src/components/`** | One React component per file — every visual piece of the app. |
| **`src/context/`** | The single React Context store holding all shared state. |
| **`src/data/`** | Data access and constants: menu loading, demo credentials, category list, Gemini client. |
| **`src/utils/`** | Pure, framework-free helper functions (formatting, cart maths, order IDs). |
| **`public/`** | Static files served as-is, currently just the menu JSON. |
| **`legacy-java/`** | The original Java console app (`Main.java` plus `interfaces/`, `menu/`, `model/`, `service/`, `util/`). Read-only reference — untouched apart from being moved out of `src/`. |

---

## Application modules

### State and data

- **`src/main.jsx`** — Entry point. Mounts `<App />` inside `<CafeteriaProvider>` and imports the global stylesheet.
- **`src/App.jsx`** — Root component. Shows `LoginScreen` when nobody is signed in, otherwise the header plus the admin or customer workspace.
- **`src/context/CafeteriaContext.jsx`** — The whole application store, replacing all four Java service classes at once: sign-in (`LoginService`), the menu and its CRUD operations (`FoodService`), the cart (`CartService`), and order placement (`OrderService`). Exports the `CafeteriaProvider` component and the `useCafeteria()` hook. Everything is plain React state — no backend, no database, no `localStorage`.
- **`src/data/menuRepository.js`** — Fetches and normalises `public/data/menu.json`, and supplies the blank-item template and next-free-ID helper the admin form needs.
- **`src/data/credentials.js`** — The two demo accounts ported from `LoginService`'s constructor (`admin`/`admin123`, `customer`/`cust123`) and the list of food categories. Hard-coded on purpose: this is a demo with no real authentication.
- **`src/data/cravingClient.js`** — The Smart Craving API client and the only module that talks to a third-party service. Builds the prompt, calls Gemini, and validates what comes back. Has no counterpart in the Java app.
- **`src/utils/orderUtils.js`** — `generateOrderId` (formats the sequential counter as `FB-0001`), plus the cart total, item count and line total functions that replace `CartItem.getTotalPrice()` and `CartService.calculateTotal()`.
- **`src/utils/formatters.js`** — `formatRupees` reproduces the Java `₹%.2f` price format; `formatOrderTime` renders the order timestamp.

### Styling

- **`src/index.css`** — Imports Tailwind, declares the design tokens (warm neutrals `cream`/`sand`/`clay`/`bark`/`cocoa`, the `saffron` accent, `leaf` and `ember` status colours, two font stacks, two shadow depths), sets the page's warm gradient background, and defines the shared component classes `surface-card`, `button-primary`, `button-secondary`, `button-quiet`, `field-input` and `stepper-button`.

### Shared UI

- **`src/components/LoginScreen.jsx`** — Full-page sign-in with an Admin/Customer role toggle, credential fields and an inline demo-credentials hint. Client-side validation only.
- **`src/components/AppHeader.jsx`** — Sticky top bar: brand mark, signed-in name, role badge and the sign-out button (the Java "Logout" option).

### Customer flow

- **`src/components/CustomerView.jsx`** — The customer workspace. Owns the category filter, the mobile cart-drawer toggle and the just-placed order, and arranges the Smart Craving box and menu beside the cart.
- **`src/components/SmartCravingBar.jsx`** — The Smart Craving box above the menu: a free-text input, one-tap example chips, and the request state (thinking / error / result). Returns `null` when no API key is set, so the feature vanishes rather than breaking.
- **`src/components/CravingSuggestions.jsx`** — Draws the picks that came back: up to three cards with the item, the model's one-line reason and an add-to-cart button obeying the same stock limit as `FoodCard`.
- **`src/components/CategoryFilter.jsx`** — Scrollable row of category pills, built from whatever categories the live menu contains.
- **`src/components/MenuGrid.jsx`** — Responsive 1/2/3-column grid of food cards, with an empty state when a filter matches nothing.
- **`src/components/FoodCard.jsx`** — One menu item: icon tile, name, category, description, availability badge, price, remaining stock and the add-to-cart button. The button disables at the stock limit, enforcing the same rule as `CartService`'s "Not enough stock available." check.
- **`src/components/CartPanel.jsx`** — The cart: all lines, the live total and "Place Order". Rendered as a sticky sidebar on desktop and inside a bottom sheet on mobile, from this one file.
- **`src/components/CartLineItem.jsx`** — One cart row with a −/+ quantity stepper (capped at available stock), the line total and a remove button. The UI equivalent of the Java `CartItem` class.
- **`src/components/OrderConfirmation.jsx`** — Modal receipt shown after checkout: generated order ID, customer name, each line and the total — the same content `Order.toString()` printed as an ASCII bill.

### Admin flow

- **`src/components/AdminView.jsx`** — The admin workspace: four summary stats, a Menu/Orders tab switch, and the add/edit/delete wiring. Covers the five options of the Java admin menu.
- **`src/components/MenuTable.jsx`** — Every food item with edit and delete actions. A real table from `sm` upward, stacked cards below it so nothing scrolls sideways on a phone.
- **`src/components/StockBadge.jsx`** — The green "N in stock" / red "Sold out" pill used in both of the table's layouts.
- **`src/components/FoodItemForm.jsx`** — Modal form for adding and editing items, with validation. Replaces the sequence of console prompts (`Food Name:`, `Price:`, `Quantity:`) from `Main.java`.
- **`src/components/OrdersList.jsx`** — Every order placed this session, newest first, with its items and total. The web version of `OrderService.displayOrders()`.

---

## Data model

Ported from `FoodItem.java`, which carried **`id`, `name`, `price` and `quantity`** — all four are preserved exactly. The Java class had no category or availability field: availability was derived from `quantity > 0` inside `CartService.addToCart`, and the web app derives it the same way. Three fields were added for the web UI:

| Field | Source | Notes |
| --- | --- | --- |
| `id` | `FoodItem.java` | Integer primary key, unchanged. |
| `name` | `FoodItem.java` | Unchanged. |
| `price` | `FoodItem.java` | Rupees, unchanged. |
| `quantity` | `FoodItem.java` | Stock on hand. Availability is `quantity > 0`, as in the Java app. |
| `category` | **added** | Needed for the card badge and the customer filter bar. |
| `description` | **added** | One line of copy on each menu card. |
| `emoji` | **added** | Icon tile on cards and rows, avoiding any external image dependency. |

The five original seed items (Veg Burger, Chicken Burger, French Fries, Pizza, Coke) keep their exact IDs, names, prices and quantities. Seven Indian cafeteria items were added to reach twelve, one of them deliberately at `quantity: 0` so the "Sold out" state is visible without editing data.

---

## Smart Craving (the AI feature)

A customer types what they feel like eating — *"something spicy and filling under ₹100"* — and gets up to three items off the live menu, each with one line explaining the pick and an **Add** button.

**How a request flows**

1. `SmartCravingBar` takes the craving text (capped at 200 characters) and hands it to `suggestFromCraving`.
2. `cravingClient` trims the live menu to its **in-stock** items only — id, name, category, price, description — so the model can never suggest something the customer then cannot add.
3. It calls `POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent` with a system instruction, the menu, the craving, and a `responseSchema` demanding `{ headline, picks: [{ id, reason }] }`. Asking for schema-shaped JSON means the reply parses without any string cleanup.
4. Returned ids are **joined against the real menu**. Anything that does not resolve to an in-stock item is dropped, duplicates are removed, and the list is capped at three.
5. `CravingSuggestions` renders what survived. Zero survivors is a normal outcome, not an error — the box says so and invites another try.

**Design decisions worth knowing**

| Decision | Why |
| --- | --- |
| **Validate ids against the menu** | This is what makes a hallucinated item harmless. The model chooses *from* the menu; it never supplies the name, price or stock the UI displays — those always come from the store. |
| **Untrusted craving text** | The craving is customer input. The system instruction tells the model to treat it as a preference and never as a command, and step 4 means even a successful prompt injection cannot put a fake item on screen. Reasons are capped at 160 characters and rendered as text, which React escapes. |
| **Local state, not the store** | Nothing outside the customer menu reads a suggestion, so `CafeteriaContext` stays a faithful mirror of the four Java services. |
| **Degrades to nothing** | No key configured → `SmartCravingBar` renders `null`. A clone without a `.env` gets the complete ordering app, minus this box. |
| **Retries transient failures** | Gemini answers `503` when a model is busy. The client retries twice with backoff before surfacing an error, and times the whole call out at 20 seconds. |
| **Model is pinned, not floating** | `gemini-3.6-flash`, overridable via `VITE_GEMINI_MODEL`. Google retires model ids — `gemini-2.5-flash` already returns `404 no longer available to new users` — so the override exists to fix that without a code change. |

**Where the API key lives, and the trade-off**

The key is read from `import.meta.env.VITE_GEMINI_API_KEY`. Vite inlines every `VITE_`-prefixed variable **into the browser bundle at build time**, which is verifiable — the key string appears in `dist/assets/*.js` after `npm run build`. For a locally-run classroom demo that is fine and it keeps the project's no-backend architecture intact. It is *not* fine for a public deployment: anyone could read the key out of the page source and spend your quota. Before deploying publicly, either restrict the key in Google AI Studio or move the `generateContent` call behind a small server endpoint and have `cravingClient.js` call that instead — the rest of the app would not change.

---

## Running the project

```bash
cp .env.example .env   # then paste your Gemini key into .env
npm install            # once
npm run dev            # http://localhost:5173
```

The app runs fine with an empty key — the Smart Craving box simply does not appear.
