# Garages buying parts from AutoAdda shops

Garages order and enquire parts **in the garage's name** (not the owner's or an employee's). Parts are
delivered to the garage. Shops see garage requests separately, with the car (make, model, year), the garage
and its work order. Parts bought for a job card flow back into that job card and its invoice automatically.

Services: part-api (`/part/api/v1`) holds orders, enquiries and shops; garage-api (`/garage/api/v1`) decides
who may buy for a garage and owns the job cards. All calls need a login; the gateway sets `X-Auto-Adda-Id`.

## 1. Who can buy

New garage permission **`PARTS_PURCHASE`** ("Order and enquire parts from AutoAdda shops").

| Role | Has it |
|---|---|
| Owner | always |
| Admin, Manager (default roles of **new** garages) | yes |
| Existing garages' roles, Technician, HR | no: the owner adds it in the role editor (`PUT /garages/{id}/roles/{roleId}`) |

Only **approved, active** garages can buy (409 otherwise). Without the permission every call below answers 403.

## 2. Where it starts

**From a job card (recommended).**
1. A technician raises a part request.
2. The customer chooses "garage buys it" (`GARAGE_WILL_PROCURE`).
3. On that item, the garage panel shows **Buy on AutoAdda**. Pass the item's id as `partRequestItemId`.

The car, quantity, part number and description come from the job automatically.

**Standalone (garage stock):** no `partRequestItemId`. The car is optional (make/model/year dropdowns).

Optional helper (garage-api), to show what will be sent:
```
GET /garage/api/v1/part-requests/item/{itemId}/purchase-context
-> { garageId, workOrderId, jobCardId, partName, partNumber, quantity, brand, model, year,
     purchasable, procurementStatus, marketplaceOrderId }
```
`purchasable: false` means the customer hasn't agreed yet, or the part is already ordered or bought. Hide the button.

## 3. Find a part

- **The part is listed:** use the normal product search (`POST /part/api/v1/products/search/paginated` with the
  car's `makeId` / `modelId` / `year`) and take the offer's `inventoryPartId`.
- **Nothing is listed:** send an enquiry (section 4). Accepting a quote gives an `inventoryPartId`.

## 4. Garage enquiries (`/part/api/v1/garages/{garageId}/enquiries`)

| Call | Notes |
|---|---|
| `POST` | body = the normal enquiry body (`docs/part-enquiry-integration.md`) + optional `partRequestItemId` |
| `GET ?status=OPEN` | the garage's enquiries with quotes |
| `GET /{enquiryId}` | one enquiry, quotes cheapest first |
| `POST /{enquiryId}/cancel` | |
| `POST /{enquiryId}/quotes/{quoteId}/accept` | returns `inventoryPartId` → order it (section 5) |

From a job card the body can be just `{ "partRequestItemId": 11 }`. If the car's brand isn't in the make list,
the call answers 400 "choose the make": send `makeId` (and `modelId`) from the dropdowns. A garage can
have up to 20 open enquiries.

### Choosing shops (panel)

The "Request part" dialog has a **Send to** choice:

| Option | What the panel does |
|---|---|
| All matching shops (default) | send without `supplierIds`; up to 20 shops covering the car, part and condition |
| Shops for this car | `GET /part/api/v1/enquiries/matching-shops?makeId=&modelId=&year=&categoryId=&conditions=` → checkbox list (name, area, city, logo) |
| Our suppliers | the garage's usual shops, picked from `GET /part/api/v1/suppliers/all` (any active shop, even one not covering this car) |

- Send the ticked shops as `"supplierIds": [5, 9]`; at most **20**, so disable more ticks at 20.
- Keep the garage's usual suppliers in the panel (e.g. saved per garage) to tick them in one click; the backend
  doesn't store a favourites list yet.
- The enquiry response has `shopsChosen: true` and `recipientCount`; show "Sent to 3 chosen shops".
- 400 "Shop '...' isn't taking enquiries" (blocked or deactivated since it was picked) or "Unknown shop id":
  show the message and refresh the list.

```js
const shops = (await partApi.get('/api/v1/enquiries/matching-shops', {
  params: { makeId, modelId, year, categoryId, conditions },
  paramsSerializer: { indexes: null },
})).data;

await partApi.post(`/api/v1/garages/${garageId}/enquiries`, {
  partRequestItemId,                       // from the job card, or makeId/modelId/year for stock
  conditions,
  ...(chosenIds.length ? { supplierIds: chosenIds } : {}),
});
```

## 5. Garage orders (`/part/api/v1/garages/{garageId}/orders`)

Garage orders don't use a cart. One order can hold parts from several shops and for several cars.
```
POST /part/api/v1/garages/3/orders
{
  "paymentMethod": "COD",
  "notes": "Deliver before 4pm",
  "items": [
    { "inventoryPartId": 555, "partRequestItemId": 11 },
    { "inventoryPartId": 120, "quantity": 10 },
    { "inventoryPartId": 130, "quantity": 1, "makeId": "e123...", "modelId": "b34d...", "year": 2019 }
  ]
}
```
- `quantity` defaults to the job's quantity (or 1).
- `paymentMethod`:
  - **COD**: the order is confirmed at once.
  - **ONLINE**: the response has `payment.paymentUrl`. Unpaid orders expire like customer orders, and `POST /{orderId}/pay` retries the payment.
- The job card item becomes **ORDERED** in garage-api (it shows "ordered from <shop>").

| Call | Notes |
|---|---|
| `GET ?status=CONFIRMED` | the garage's orders |
| `GET /{orderId}` | one order |
| `POST /{orderId}/cancel` | only before any shop has dispatched; job items go back to "to be procured" |
| `POST /{orderId}/received` | body `{ "itemIds": [1, 2] }`, or empty for every accepted/dispatched line |

**Received** marks the lines RECEIVED. For lines linked to a job card it also:
- creates the procurement in garage-api: supplier = shop name, price = unit price, condition, receipt = "AutoAdda order #N";
- adds the part to the work order's invoice, exactly like a manual procurement.

When every line is received, the order becomes DELIVERED.

Order response (additions to the customer order shape):
- **Order:** `buyerType` (`GARAGE`), `garageId`, `garageName`, `garagePhone`, `garageAddress`, `garageMapLink`, `notes`, `refundAmount`.
- **Each line:**
  - `fulfilmentStatus` (PENDING, ACCEPTED, REJECTED, DISPATCHED, RECEIVED) and its timestamps;
  - `rejectReason`, `condition`;
  - `vehicleMakeName` / `vehicleModelName` / `vehicleYear`;
  - `workOrderId`, `jobCardId`, `partRequestItemId`;
  - `pendingGarageSync` / `garageSyncError` (see section 7).

## 6. Shop panel: orders inbox (`/part/api/v1/suppliers/{supplierId}/orders`)

The shop owner sees customer **and** garage orders, but only the shop's own lines.

| Call | Notes |
|---|---|
| `GET ?buyerType=GARAGE&status=PENDING` | garage orders waiting for this shop (`buyerType`: CUSTOMER / GARAGE; `status`: of the shop's lines) |
| `GET /{orderId}` | one order |
| `POST /{orderId}/accept` | PENDING lines → ACCEPTED |
| `POST /{orderId}/reject` | body `{ "reason": "Out of stock" }`; lines not yet dispatched → REJECTED |
| `POST /{orderId}/dispatch` | open lines → DISPATCHED (out for delivery) |
| `POST /{orderId}/delivered` | customer orders only; garages confirm receipt themselves |

Each order shows:
- `buyerType`, then:
  - for garages: `garageName`, `garagePhone`, `garageAddress`, `garageMapLink` (deliver here);
  - for customers: `vehicleOwnerId`;
- `paymentMethod` / `paymentStatus`;
- the shop's `items`, each with the car (`vehicleMakeName`, `vehicleModelName`, `vehicleYear`), `workOrderId` and `jobCardId`;
- `shopTotal`.

What changes as the shop acts:
- **Rejecting:**
  - stock goes back;
  - a COD order's total drops; on a paid order, `refundAmount` grows (refund it by hand for now);
  - the garage's job card item is freed;
  - if every line is rejected, the order is cancelled.
- **Unpaid online orders** can't be handled yet (409 "awaiting online payment").
- **Order status** follows the lines: all sent → SHIPPED; all received → DELIVERED.

**Garage enquiries** reach shops the same way as customer enquiries (`GET /enquiries/supplier/{id}?buyerType=GARAGE`).
- They show `garageName`, the car and `workOrderId`.
- The garage's phone and address appear after the garage accepts that shop's quote.

## 7. Job card sync

part-api tells garage-api when a job line is ordered, cancelled or received.
- If garage-api is down, the order still goes through. The line shows `pendingGarageSync`, and an hourly job retries it.
- If garage-api refuses (for example, the part was already bought by hand), the line shows `garageSyncError`. Fix the job card in the garage panel.

## 8. Errors

| Status | When |
|---|---|
| 400 | bad body (unknown make/model, the same job item twice, quantity < 1, more than 50 items) |
| 403 | no `PARTS_PURCHASE` in the garage / not the shop's owner |
| 404 | another garage's order/enquiry/job item, or another shop's order |
| 409 | garage not approved; job item not purchasable; cancel after dispatch; receive before accept/dispatch; unpaid online order for shops |
| 503 | garage-api unavailable (try again) |

## 9. Deploy

1. **garage-api first** (new permission, endpoints, `ORDERED` status, new columns).
2. **part-api.** It calls garage-api at `garage.api.url` (default `http://localhost:8020/api/v1`); set it if garage-api runs elsewhere.
3. **Once on the part-api database** (ddl-auto doesn't relax NOT NULL):
   ```sql
   ALTER TABLE part_enquiry MODIFY vehicle_owner_id INT NULL;
   ```
4. **Close database client sessions** before restarting either service.
5. **Existing garages:** the owner adds `PARTS_PURCHASE` to the roles that should buy.
