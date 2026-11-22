# Vehicle owner: ordering parts (integration guide)

How a vehicle owner (customer) buys parts on the website / app: from an offer to cart, checkout, payment,
delivery and cancellation. Search is in `docs/website-product-search-guide.md`; requesting parts nobody
lists is in `docs/part-enquiry-integration.md`. Everything here is on part-api `main`.

## 1. Basics

| | |
|---|---|
| Base path | through the gateway: `<gateway>/part/api/v1/...` (below paths start at `/api/v1`) |
| Login | **required** for every call in this guide (JWT; the gateway sets `X-Auto-Adda-Id`) |
| `ownerId` | the logged-in vehicle owner's id; must equal `X-Auto-Adda-Id`, otherwise **403** |
| Someone else's order | **404** (order ids can't be probed) |
| Currency | AED |
| Errors | `{ "status": "failed", "code": "409", "message": "..." }`: show `message` |

## 2. The flow at a glance

```
search / product page / accepted quote
        │  offer.inventoryPartId
        ▼
  add to cart  ──►  view / change cart  ──►  checkout (COD or ONLINE)
                                               │
        ┌──────────────────────────────────────┴──────────────────────┐
        ▼ COD                                                         ▼ ONLINE
  CONFIRMED at once                                 PENDING_PAYMENT + payment link / QR (15 min)
        │                                            │ paid (gateway webhook)      │ not paid in time
        │                                            ▼                             ▼
        │                                        CONFIRMED                CANCELLED (EXPIRED), stock back
        ▼                                            │
  shop accepts → dispatches → SHIPPED → shop marks delivered → DELIVERED   (cash collected on COD)
```
"Buy now" skips the cart and goes straight to checkout with one offer.

## 3. What to add: the offer

A **part** you can buy is a shop's offer (listing) of a product. Its id is **`inventoryPartId`**:
- **Product page:** `GET /api/v1/products/search/{productId}?includeSuppliers=true`, then `suppliers[].inventoryPartId`
  (each offer has its own shop, price, stock and condition);
- **Offers list:** `GET /api/v1/products/search/{productId}/suppliers`;
- **Accepted enquiry quote:** the `inventoryPartId` in the accept response.

Show the offer's `unitPrice`, `quantityInStock` and `productCondition` (GENUINE / USED / AFTER_MARKET) and the
shop name. Use `inventoryPartId` as `partId` in the cart and order calls.

## 4. Cart

| Action | Call | Notes |
|---|---|---|
| Add | `POST /api/v1/cart/{ownerId}/add?partId={inventoryPartId}&quantity=1` | adds to the quantity already in the cart |
| View | `GET /api/v1/cart/{ownerId}` | empty cart: `items: []`, `totalAmount: 0` |
| Set quantity | `PUT /api/v1/cart/{ownerId}/items/{inventoryPartId}?quantity=3` | `0` removes the item |
| Remove | `DELETE /api/v1/cart/{ownerId}/items/{inventoryPartId}` | |
| Empty | `DELETE /api/v1/cart/{ownerId}/clear` | 204 |

Cart response:
```json
{
  "cartId": 7, "vehicleOwnerId": 42, "totalItems": 3, "totalAmount": 360.0,
  "items": [
    { "cartItemId": 11, "partId": 555, "productId": "p1", "supplierId": "5", "partName": "Front brake pad",
      "partNumber": "BP-100", "unitPrice": 120.0, "quantity": 3, "lineTotal": 360.0,
      "availableStock": 8, "available": true }
  ]
}
```
- Stock is **not reserved** in the cart; it's checked when adding and again at checkout.
- `available: false` (the offer was withdrawn or stock dropped below the quantity): show it greyed out with
  `availableStock`, and ask the customer to lower the quantity or remove it before checkout.
- Prices shown in the cart are the current offer prices; the order keeps the price at checkout time.
- Adding more than the stock: **409** "Only N unit(s) of ... available".

## 5. Checkout

| Action | Call |
|---|---|
| Order the whole cart | `POST /api/v1/orders/place/{ownerId}?paymentMethod=COD` (or `ONLINE`) |
| Buy one offer now (cart untouched) | `POST /api/v1/orders/buy-now/{ownerId}?partId={inventoryPartId}&quantity=1&paymentMethod=COD` |

The body says how the customer gets the parts: `"deliveryMethod": "DELIVERY"` (default) or `"PICKUP"`.

**Delivery** needs a **delivery address** (400 without it):
```json
{
  "deliveryAddress": {
    "contactName": "Sara Khan",
    "phone": "050 123 4567",
    "addressLine1": "Villa 12, Street 4",
    "addressLine2": "Near Mall of the Emirates",
    "area": "Al Barsha",
    "city": "Dubai",
    "emirate": "Dubai",
    "mapLink": "https://maps.google.com/?q=25.11,55.20",
    "latitude": 25.11,
    "longitude": 55.20,
    "notes": "Call on arrival"
  }
}
```
| Field | Rule |
|---|---|
| `contactName` | required, max 100 |
| `phone` | required, a UAE number (`0501234567`, `+971501234567`, `00971...`, `04 123 4567`; spaces, dashes and brackets are fine); saved as `+971501234567` |
| `addressLine1` | required, max 200 (building / villa, street) |
| `area`, `city` | required, max 100 each |
| `addressLine2`, `emirate` | optional |
| `mapLink` | optional (a map pin link, max 1000) |
| `latitude`, `longitude` | optional, both or neither |
| `notes` | optional, max 500 ("call on arrival", gate code) |

Prefill name and phone from the customer's profile and let them confirm. The address is stored with the order
as entered; editing it later in the profile doesn't change past orders. Shops see it in their order inbox
(`deliveryAddress`), and it comes back on every order response.

**Self pickup** needs only who collects (400 without it):
```json
{ "deliveryMethod": "PICKUP",
  "pickupContact": { "contactName": "Sara Khan", "phone": "050 123 4567", "notes": "Coming after 6pm" } }
```
`contactName` and a UAE `phone` are required. No delivery address is needed. See section 5b for the rest.

What happens:
- Stock of every line is checked and **deducted** in one step; if one line is short, nothing is ordered (409).
- `place` empties the cart once the order exists. With ONLINE, if starting the payment fails, nothing is ordered
  and the cart stays as it was (502 "Payment initiation failed").
- One order can contain offers from several shops; each shop handles its own lines.

Response (201), the same `OrderResponse` shape everywhere:
```json
{
  "orderId": 42, "vehicleOwnerId": 42, "buyerType": "CUSTOMER",
  "status": "CONFIRMED", "orderDate": "2026-11-21T10:15:00",
  "paymentMethod": "COD", "paymentStatus": "PENDING",
  "totalItems": 3, "totalAmount": 360.0,
  "items": [
    { "orderItemId": 1, "partId": 555, "partName": "Front brake pad", "supplierId": "5",
      "unitPrice": 120.0, "quantity": 3, "lineTotal": 360.0, "condition": "GENUINE",
      "fulfilmentStatus": "PENDING" }
  ],
  "deliveryAddress": { "contactName": "Sara Khan", "phone": "+971501234567", "addressLine1": "Villa 12, Street 4",
                       "area": "Al Barsha", "city": "Dubai", "notes": "Call on arrival" },
  "payment": null
}
```

### Cash on Delivery (COD)
- The order is **CONFIRMED** at once; `paymentStatus` is `PENDING` until the cash is collected at delivery.
- Show "Order placed. Pay AED 360.00 on delivery".

### Online payment (ONLINE)
1. The response has `status: PENDING_PAYMENT` and a `payment` block:
   ```json
   "payment": { "orderReference": "PO-42-1763700900000-1a2b3c4d", "paymentUrl": "https://...", "qrCodeImage": "<base64 PNG>",
                "amount": 360.00, "currency": "AED", "expiryMinutes": 15, "expiresAt": "2026-11-21T10:30:00" }
   ```
2. **Web:** redirect to `paymentUrl`. **App / desktop:** show `qrCodeImage` (`data:image/png;base64,...`) and a
   countdown to `expiresAt`. Stock is held for the customer until then.
3. After paying, the gateway sends the browser back to the website:
   - `/orders/payment-success?orderId=42`, or `/orders/payment-failed?orderId=42`.

   **These pages must not trust the URL.** Read the real state from the backend.
4. Poll `GET /api/v1/payments/orders/{orderId}/status` every 2–3 s for up to about a minute:
   ```json
   { "orderId": 42, "orderStatus": "CONFIRMED", "paymentMethod": "ONLINE", "paymentStatus": "SUCCESS",
     "paymentExpiresAt": "2026-11-21T10:30:00" }
   ```
   Act on `paymentStatus`:
   - **`SUCCESS`:** paid. Show the confirmation.
   - **`PENDING`:** the gateway hasn't confirmed yet. Keep polling (the webhook can arrive a few seconds after the redirect).
   - **`FAILED`:** offer **Pay again** (below) while before `paymentExpiresAt`.
   - **`EXPIRED`:** too late. The order is cancelled and the stock released. Offer to order again.
5. **Pay again:** `POST /api/v1/orders/{orderId}/pay` returns a fresh `payment` block (a new 15-minute window).
   - Allowed only while `PENDING_PAYMENT` and the last attempt **failed**.
   - 409 while a link is still open ("A payment is already in progress"), or after the window has expired.
6. Unpaid orders are cancelled automatically about a minute after `paymentExpiresAt`.
7. **Paid too late:** if a payment arrives after the order was cancelled, the order becomes `PENDING_REFUND`
   (`paymentStatus: REFUND_PENDING`). Tell the customer the money will be refunded.

Only the gateway's server-to-server webhook marks an order paid, so the browser redirect can never fake a payment.

## 6. My orders

| Action | Call |
|---|---|
| List (newest first) | `GET /api/v1/orders/owner/{ownerId}` |
| One order | `GET /api/v1/orders/{orderId}` |
| Payment status (light) | `GET /api/v1/payments/orders/{orderId}/status` |

## 5b. Self pickup

The customer collects the parts at the shop(s) instead of delivery.
1. Checkout with `deliveryMethod: PICKUP` and `pickupContact` (above). Payment works the same:
   - **COD** means pay at the shop;
   - **ONLINE** means pay now and collect later.
2. The order response has:
   - `pickupCode`: 6 digits. Show it prominently ("Show this code at the shop"). Only the customer sees it; shops never get it.
   - `pickupPoints[]`: one per shop in the order, with `name`, `phone`, `address`, `mapLink`, `workingDays`, `openingHours` and `closingHours`. Show a "Directions" button (`mapLink`) and the opening hours.
   - `pickupContact`, and `deliveryAddress: null`.
3. Each shop packs its lines and marks them **ready for pickup**. The line becomes `READY_FOR_PICKUP`, and when
   every remaining line is ready the order becomes `READY_FOR_PICKUP`: tell the customer "Ready to collect".
4. At the shop, the customer gives the code. The shop enters it and hands the parts over. The lines become
   `RECEIVED` ("Collected"), and when everything is collected the order becomes **`COLLECTED`**.
5. An order with parts from several shops means one visit per shop. Each pickup point lists that shop's lines
   (match `items[].supplierId` with `pickupPoints[].supplierId`).

The customer can cancel while the parts wait at the shop (stock goes back), but not after collecting any of them.

### Order status (show to the customer)

| `status` | Meaning | Customer can |
|---|---|---|
| `PENDING_PAYMENT` | waiting for online payment | pay / pay again, cancel |
| `CONFIRMED` | placed (COD) or paid; shops are preparing it | cancel (until a shop dispatches) |
| `SHIPPED` | every line is on its way | wait |
| `DELIVERED` | every line delivered | nothing |
| `READY_FOR_PICKUP` | pickup: every line is waiting at the shops | go and collect (show `pickupCode`), cancel |
| `COLLECTED` | pickup: everything collected | nothing |
| `CANCELLED` | cancelled by the customer, by expiry, or every line rejected by the shops | order again |
| `PENDING_REFUND` | cancelled after money was received; refund pending | nothing |

### Each line (`items[]`): `fulfilmentStatus`

| Value | Show |
|---|---|
| `PENDING` | "Waiting for the shop" |
| `ACCEPTED` | "Shop is preparing it" (`acceptedAt`) |
| `DISPATCHED` | "On the way" (`dispatchedAt`) |
| `READY_FOR_PICKUP` | pickup: "Ready to collect at <shop>" (`dispatchedAt` = when it was ready) |
| `RECEIVED` | "Delivered", or "Collected" for pickup (`receivedAt`) |
| `REJECTED` | "Not available from this shop" (+ `rejectReason`); the line isn't charged |

Lines from different shops move separately; the order turns `SHIPPED` / `DELIVERED` when **all** remaining lines
have. If a shop rejects lines:
- **COD:** `totalAmount` drops by those lines.
- **Paid online:** `refundAmount` shows what will be refunded.
- **Every line rejected:** the order is `CANCELLED`.

## 7. Cancel

`POST /api/v1/orders/{orderId}/cancel`
- Allowed in `PENDING_PAYMENT`, `CONFIRMED` and `READY_FOR_PICKUP`, **until any shop has dispatched a line or the
  customer has collected one** (then 409 "a shop has already sent part of it").
- Stock goes back to the shops.
- `paymentStatus` becomes `CANCELLED`, or `REFUND_PENDING` if it was already paid online (manual refund).

## 8. Errors to handle

| Status | When | Show |
|---|---|---|
| 400 | quantity < 1, missing parameter, delivery address missing or invalid (`message` names the field, e.g. "deliveryAddress.phone must be a UAE number") | fix the input |
| 401 | not logged in | sign-in, then retry |
| 403 | `X-Auto-Adda-Id` isn't `ownerId` | sign in again |
| 404 | unknown offer / order, or someone else's order; removing an item not in the cart | "Not found" |
| 409 | not enough stock ("Only N unit(s) ..."), offer withdrawn ("not available for sale"), cart empty, payment already in progress, payment window expired, cancel not allowed | `message`; refresh the cart / order |
| 502 | payment gateway refused to start the payment | "Payment couldn't start, try again or choose Cash on Delivery" |

## 9. Example (axios)

```js
// add an offer to the cart
await partApi.post(`/api/v1/cart/${ownerId}/add`, null, { params: { partId: offer.inventoryPartId, quantity: 1 } });

// checkout
const { data: order } = await partApi.post(`/api/v1/orders/place/${ownerId}`,
  { deliveryAddress },                             // { contactName, phone, addressLine1, area, city, ... }
  { params: { paymentMethod } },                   // 'COD' | 'ONLINE'
);

if (order.payment?.paymentUrl) {
  window.location.href = order.payment.paymentUrl; // web; apps: show order.payment.qrCodeImage
} else {
  showConfirmation(order);                         // COD
}

// on /orders/payment-success and /orders/payment-failed: never trust the URL, poll the backend
async function waitForPayment(orderId) {
  for (let i = 0; i < 20; i++) {
    const { data } = await partApi.get(`/api/v1/payments/orders/${orderId}/status`);
    if (data.paymentStatus !== 'PENDING') return data;  // SUCCESS | FAILED | EXPIRED | ...
    await new Promise(r => setTimeout(r, 3000));
  }
  return { paymentStatus: 'PENDING' };                   // still waiting: show "We'll update you"
}

// pay again after a failed attempt
const { data: retry } = await partApi.post(`/api/v1/orders/${orderId}/pay`);
window.location.href = retry.payment.paymentUrl;
```

## 10. Known limitations

- Stock isn't reserved in the cart, only at checkout (and during the online payment window).
- No delivery fee, coupons or VAT lines yet: `totalAmount` is the sum of the lines.
- Refunds (late payments, paid orders cancelled or partly rejected) are done manually.
- The customer can't confirm receipt; the shop marks customer orders delivered.
- No saved address book yet: the frontend keeps the customer's addresses (or prefills from the profile).
- Accepted enquiry quotes create a normal offer, which other customers can also see.
