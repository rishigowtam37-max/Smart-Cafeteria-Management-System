#!/usr/bin/env bash
#
# Walks the whole API against a running backend, with no browser involved.
#
#   cd backend && ./mvnw spring-boot:run     # in one terminal
#   ./backend/verify-api.sh                  # in another
#
# Proves the rules live in Java: stock refusals, role boundaries and order
# numbering all come back from the server, not from any client-side check.

set -u

BASE="${BASE:-http://localhost:8080}"
CUSTOMER_JAR=$(mktemp)
ADMIN_JAR=$(mktemp)
PASS=0
FAIL=0

cleanup() { rm -f "$CUSTOMER_JAR" "$ADMIN_JAR"; }
trap cleanup EXIT

bold() { printf '\n\033[1m%s\033[0m\n' "$1"; }

# check <description> <expected-status> <actual-status> [body]
check() {
  local what=$1 expected=$2 actual=$3 body=${4:-}
  if [ "$actual" = "$expected" ]; then
    printf '  \033[32m✓\033[0m %-52s %s\n' "$what" "$actual"
    PASS=$((PASS + 1))
  else
    printf '  \033[31m✗\033[0m %-52s expected %s, got %s\n' "$what" "$expected" "$actual"
    [ -n "$body" ] && printf '      %s\n' "$body"
    FAIL=$((FAIL + 1))
  fi
}

# call <jar> <method> <path> [json] -> sets STATUS and BODY
call() {
  local jar=$1 method=$2 path=$3 json=${4:-}
  local response
  if [ -n "$json" ]; then
    response=$(curl -s -o /tmp/fb_body -w '%{http_code}' -X "$method" \
      -b "$jar" -c "$jar" -H 'Content-Type: application/json' -d "$json" "$BASE$path")
  else
    response=$(curl -s -o /tmp/fb_body -w '%{http_code}' -X "$method" \
      -b "$jar" -c "$jar" "$BASE$path")
  fi
  STATUS=$response
  BODY=$(cat /tmp/fb_body)
}

field() { printf '%s' "$1" | python3 -c "import json,sys; print(json.load(sys.stdin)$2)" 2>/dev/null; }

if ! curl -s -o /dev/null --max-time 5 "$BASE/api/config" 2>/dev/null && \
   ! curl -s -o /dev/null --max-time 5 "$BASE/" 2>/dev/null; then
  echo "Backend is not answering on $BASE - start it with: cd backend && ./mvnw spring-boot:run"
  exit 1
fi

bold "1. Anonymous requests are refused"
call "$CUSTOMER_JAR" GET /api/menu;          check "GET /api/menu without a session" 401 "$STATUS" "$BODY"
call "$CUSTOMER_JAR" GET /api/admin/orders;  check "GET /api/admin/orders without a session" 401 "$STATUS" "$BODY"
call "$CUSTOMER_JAR" GET /api/auth/me;       check "GET /api/auth/me without a session" 401 "$STATUS" "$BODY"

bold "2. Sign-in"
call "$CUSTOMER_JAR" POST /api/auth/login '{"role":"customer","username":"customer","password":"wrong"}'
check "wrong password" 401 "$STATUS" "$BODY"

call "$CUSTOMER_JAR" POST /api/auth/login '{"role":"customer","username":"customer","password":"cust123"}'
check "customer signs in" 200 "$STATUS" "$BODY"
echo "      signed in as: $(field "$BODY" "['name']") ($(field "$BODY" "['role']"))"

call "$ADMIN_JAR" POST /api/auth/login '{"role":"admin","username":"admin","password":"admin123"}'
check "admin signs in" 200 "$STATUS" "$BODY"

bold "3. Menu"
call "$CUSTOMER_JAR" GET /api/menu
check "customer reads the menu" 200 "$STATUS" "$BODY"
echo "      items on menu: $(printf '%s' "$BODY" | python3 -c "import json,sys; print(len(json.load(sys.stdin)))")"

bold "4. Role boundaries are enforced by the server"
call "$CUSTOMER_JAR" GET /api/admin/orders;      check "customer -> admin orders" 403 "$STATUS" "$BODY"
call "$CUSTOMER_JAR" DELETE /api/admin/menu/1;   check "customer -> delete a menu item" 403 "$STATUS" "$BODY"
call "$ADMIN_JAR" GET /api/cart;                 check "admin -> customer cart" 403 "$STATUS" "$BODY"
call "$ADMIN_JAR" POST /api/orders;              check "admin -> place an order" 403 "$STATUS" "$BODY"

bold "5. Cart and the stock rule"
call "$CUSTOMER_JAR" POST /api/cart '{"foodId":1,"quantity":2}'
check "add 2 Veg Burgers" 200 "$STATUS" "$BODY"
echo "      cart total: ₹$(field "$BODY" "['total']"), units: $(field "$BODY" "['itemCount']")"

call "$CUSTOMER_JAR" POST /api/cart '{"foodId":12,"quantity":1}'
check "add a sold-out item" 409 "$STATUS" "$BODY"
echo "      server said: $(field "$BODY" "['message']")"

call "$CUSTOMER_JAR" POST /api/cart '{"foodId":4,"quantity":9999}'
check "add more Pizza than exists" 409 "$STATUS" "$BODY"

call "$CUSTOMER_JAR" POST /api/cart '{"foodId":9999,"quantity":1}'
check "add an item that does not exist" 404 "$STATUS" "$BODY"

call "$CUSTOMER_JAR" POST /api/cart '{"foodId":1,"quantity":0}'
check "add a quantity of zero" 400 "$STATUS" "$BODY"

call "$CUSTOMER_JAR" PATCH /api/cart/1 '{"quantity":3}'
check "raise the line to 3" 200 "$STATUS" "$BODY"
echo "      cart total: ₹$(field "$BODY" "['total']")"

bold "6. Reserved stock is visible to everyone"
call "$ADMIN_JAR" GET /api/menu
REMAINING=$(printf '%s' "$BODY" | python3 -c "import json,sys; print([i['quantity'] for i in json.load(sys.stdin) if i['id']==1][0])")
check "Veg Burger stock dropped from 20 to 17" 17 "$REMAINING" "$BODY"

bold "7. Checkout"
call "$CUSTOMER_JAR" POST /api/orders
check "place the order" 201 "$STATUS" "$BODY"
ORDER_CODE=$(field "$BODY" "['orderCode']")
echo "      order: $ORDER_CODE for $(field "$BODY" "['customerName']"), ₹$(field "$BODY" "['totalAmount']")"

call "$CUSTOMER_JAR" POST /api/orders
check "place a second, empty order" 409 "$STATUS" "$BODY"
echo "      server said: $(field "$BODY" "['message']")"

bold "8. The admin sees the order"
call "$ADMIN_JAR" GET /api/admin/orders
check "admin lists orders" 200 "$STATUS" "$BODY"
check "the newest one is the order just placed" "$ORDER_CODE" "$(field "$BODY" "[0]['orderCode']")" "$BODY"
echo "      orders on record: $(printf '%s' "$BODY" | python3 -c 'import json,sys; print(len(json.load(sys.stdin)))')"
echo "      (that count grows across runs - orders are stored in backend/data, not memory)"

bold "9. Admin menu CRUD"
call "$ADMIN_JAR" POST /api/admin/menu '{"name":"Filter Coffee","price":30,"quantity":12,"category":"Beverages","description":"Strong and milky","emoji":"☕"}'
check "create a menu item" 201 "$STATUS" "$BODY"
NEW_ID=$(field "$BODY" "['id']")
echo "      created id $NEW_ID (assigned by the server)"

call "$ADMIN_JAR" PUT "/api/admin/menu/$NEW_ID" '{"name":"Filter Coffee","price":35,"quantity":10,"category":"Beverages","description":"Strong and milky","emoji":"☕"}'
check "update it" 200 "$STATUS" "$BODY"

call "$ADMIN_JAR" POST /api/admin/menu '{"name":"","price":30,"quantity":5,"category":"Beverages","description":"","emoji":"☕"}'
check "create one with a blank name" 400 "$STATUS" "$BODY"

call "$ADMIN_JAR" POST /api/admin/menu '{"name":"Free Lunch","price":0,"quantity":5,"category":"Snacks","description":"","emoji":"🍽️"}'
check "create one priced at zero" 400 "$STATUS" "$BODY"

call "$ADMIN_JAR" DELETE "/api/admin/menu/$NEW_ID"; check "delete it" 204 "$STATUS" "$BODY"
call "$ADMIN_JAR" DELETE "/api/admin/menu/$NEW_ID"; check "delete it again" 404 "$STATUS" "$BODY"

bold "10. Sign-out releases held stock"
call "$CUSTOMER_JAR" POST /api/auth/login '{"role":"customer","username":"customer","password":"cust123"}' >/dev/null
call "$CUSTOMER_JAR" POST /api/cart '{"foodId":5,"quantity":4}'
check "hold 4 Cokes in a cart" 200 "$STATUS" "$BODY"

call "$ADMIN_JAR" GET /api/menu
HELD=$(printf '%s' "$BODY" | python3 -c "import json,sys; print([i['quantity'] for i in json.load(sys.stdin) if i['id']==5][0])")
check "Coke stock reserved (30 -> 26)" 26 "$HELD" ""

call "$CUSTOMER_JAR" POST /api/auth/logout; check "customer signs out" 204 "$STATUS" "$BODY"

call "$ADMIN_JAR" GET /api/menu
RELEASED=$(printf '%s' "$BODY" | python3 -c "import json,sys; print([i['quantity'] for i in json.load(sys.stdin) if i['id']==5][0])")
check "Coke stock released back to 30" 30 "$RELEASED" ""

call "$CUSTOMER_JAR" GET /api/cart; check "the signed-out session is dead" 401 "$STATUS" "$BODY"

bold "Result"
printf '  %d passed, %d failed\n\n' "$PASS" "$FAIL"
[ "$FAIL" -eq 0 ] || exit 1
