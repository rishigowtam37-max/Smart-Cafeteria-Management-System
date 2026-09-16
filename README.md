# 🍽️ FlowBite

**Seamless food ordering with smart menu suggestions and order coordination.**

FlowBite is a cafeteria ordering app for the counter and the customer at once. Customers browse a live menu, describe what they feel like eating in plain language and get real menu items back, then order in a couple of taps. Staff manage the menu and watch orders arrive from the same app.

Built with React 19, Vite 7 and Tailwind CSS 4. No backend, no database — the whole app runs in the browser, which makes it trivial to clone, run and demo.

---

## ✨ Features

### Customer

- Live menu with category filters, prices, stock counts and sold-out states
- **Smart Craving** — describe a mood (*"something spicy and filling under ₹100"*) and get up to three matching items, each with a one-line reason
- Cart with quantity steppers that never exceed available stock
- One-tap checkout with a printed-style receipt and a generated order ID (`FB-0001`)
- Works on a phone: the cart collapses into a bottom sheet below `lg`

### Admin

- Dashboard summarising items on menu, sold-out count, orders placed and revenue
- Add, edit and delete menu items through a validated modal form
- Live order feed, newest first, with line items and totals
- Edits propagate instantly — a price change reaches any cart already holding that item

### Shared

- Role-based sign-in for admin and customer
- Stock is decremented on order placement and reflected everywhere at once
- Keyboard-navigable throughout, with a consistent focus ring and live regions for async results

---

## 🚀 Getting started

**Requirements:** Node.js 20.19+ (or 22.12+) and npm.

```bash
git clone <repository-url>
cd Smart-Cafeteria-Management-System

cp .env.example .env    # optional — see Configuration
npm install
npm run dev             # http://localhost:5173
```

| Script | What it does |
| --- | --- |
| `npm run dev` | Dev server with hot module replacement on port 5173 |
| `npm run build` | Production bundle into `dist/` |
| `npm run preview` | Serve the built bundle locally |

### Demo credentials

| Role | Username | Password |
| --- | --- | --- |
| Admin | `admin` | `admin123` |
| Customer | `customer` | `cust123` |

These are hard-coded on the client on purpose. FlowBite is a demo — it performs no real authentication.

---

## ⚙️ Configuration

| Variable | Required | Purpose |
| --- | --- | --- |
| `VITE_GEMINI_API_KEY` | No | Enables Smart Craving. Get one at [Google AI Studio](https://aistudio.google.com/apikey). |
| `VITE_GEMINI_MODEL` | No | Overrides the pinned Gemini model. Leave blank unless the default is retired. |

**Without a key the app runs completely** — the Smart Craving box simply does not render. Every other feature is unaffected.

> **Put your key in `.env` only.** `.env` is gitignored; `.env.example` is tracked and must stay blank.
>
> Vite inlines every `VITE_`-prefixed variable into the browser bundle at build time, so the key is readable by anyone who opens a deployed build. That is an accepted trade-off for a local demo. Before deploying publicly, restrict the key in Google AI Studio or move the API call behind a server endpoint.

---

## 🧠 How Smart Craving works

1. The craving text (capped at 200 characters) is sent with the **in-stock** portion of the live menu to the Gemini API, which is asked for schema-shaped JSON: a headline plus item ids with reasons.
2. Every returned id is **joined against the real menu**. Ids that don't resolve, duplicates and anything out of stock are discarded; the rest is capped at three.
3. Name, price and stock always come from the app's own store — never from the model.

That validation step is what keeps the feature safe. A hallucinated item can't reach the screen, and neither can a prompt injection hidden in the craving text, because the model's only influence is *which* of your items get shown. Requests retry on transient API errors and time out at 20 seconds.

---

## 📂 Project structure

```
FlowBite
│
├── src
│   ├── components/         one React component per file
│   ├── context/            the single application store
│   ├── data/               menu loading, credentials, Gemini client
│   ├── utils/              pure helpers (formatting, cart maths)
│   ├── App.jsx             role-based screen switch
│   └── index.css           design tokens and shared component classes
│
├── public/data/menu.json   the menu, editable without touching code
├── legacy-java/            the original Java console app (reference only)
├── .env.example
├── TECH_STACK.md           full architecture notes
└── vite.config.js
```

State lives in one React Context (`src/context/CafeteriaContext.jsx`) covering sign-in, menu, cart and orders. There is no router — `App.jsx` switches screens on role. Orders are held in memory for the session.

See **[TECH_STACK.md](TECH_STACK.md)** for the file-by-file architecture, design decisions and data model.

---

## 🗄️ Origins

FlowBite began as a console-based Java application built around core OOP concepts — encapsulation, inheritance, abstraction, interfaces and polymorphism — with separate admin and customer modules. That app remains in `legacy-java/` as read-only reference; the web app mirrors its domain model but shares no code with it.

```bash
javac legacy-java/interfaces/*.java legacy-java/model/*.java legacy-java/service/*.java \
      legacy-java/menu/*.java legacy-java/util/*.java legacy-java/Main.java
java -cp legacy-java Main
```

---

## 🗺️ Roadmap

- Persistence — orders and menu edits survive a refresh
- Order status tracking (placed → preparing → ready)
- Search across the menu
- Payment integration
- Order history per customer
