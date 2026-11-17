# Part enquiries ("Request this part")

Backend branch: `feature/part-enquiries` (builds on `feature/shop-coverage`). All paths are on the part service
(`/api/v1`). Every customer and shop call needs the logged-in user's `X-Auto-Adda-Id` header (set by the
gateway): customer calls answer 403 unless it equals the `vehicleOwnerId` in the path, shop calls unless it is
the shop's owner.

## Flow
1. Customer's search finds nothing (or a product has 0 shops) -> website shows "Request this part" with
   `GET /enquiries/matching-shops/count` ("12 shops may have this").
2. Customer sends the enquiry -> it goes to up to **20** matching shops; shops can quote for **48 h**.
3. Shops see it in their inbox and quote (price, condition, quantity) or decline.
4. Customer compares quotes and accepts one (each quote is valid **72 h** from when it was sent).
5. Accepting creates a normal listing at the quoted price and returns `inventoryPartId`:
   add it to the cart (`POST /cart/{vehicleOwnerId}/add?partId=...&quantity=...`) or buy now. Payment is unchanged.

Matching shops: active, not blocked, cover the make; and cover the model, category (a shop covering a parent
category matches its subcategories), condition and the customer's model year (`yearFrom`..`yearTo`), or have
none of those set. An enquiry without a year isn't filtered by year. Shops set their coverage at
registration (`docs/shop-coverage-integration.md`).

## Website / customer app

| Call | Notes |
|---|---|
| `GET /enquiries/matching-shops/count?makeId=&modelId=&year=&categoryId=&conditions=USED&conditions=GENUINE` | `{ "count": 12 }`; makeId required |
| `POST /enquiries/owner/{vehicleOwnerId}` | create, 201; body below |
| `GET /enquiries/owner/{vehicleOwnerId}?status=OPEN` | my enquiries, newest first, with quotes |
| `GET /enquiries/owner/{vehicleOwnerId}/{enquiryId}` | one enquiry, quotes cheapest first |
| `POST /enquiries/owner/{vehicleOwnerId}/{enquiryId}/cancel` | while OPEN / QUOTED |
| `POST /enquiries/owner/{vehicleOwnerId}/{enquiryId}/quotes/{quoteId}/accept` | returns `inventoryPartId` |

Create body:
```json
{
  "makeId": "e123...",
  "modelId": "b34d...",
  "year": 2019,
  "categoryId": "CAT-LIGHTS",
  "productId": null,
  "partNumber": "81150-06C60",
  "description": "Left headlight, LED type",
  "conditions": ["GENUINE", "USED"],
  "imageIds": [31, 32]
}
```
- `makeId` required; `modelId` must belong to it; `year` 1950..next 5 years.
- Describe the part with at least one of `productId` (when starting from a product page), `partNumber`, `description`.
- `conditions` empty = any. `imageIds`: up to 5, uploaded first through the existing upload API.
- Max 5 open enquiries per customer.

Accept response:
```json
{ "enquiryId": 100, "quoteId": 7, "inventoryPartId": 555, "supplierId": 5, "productId": "p1",
  "unitPrice": 150.0, "quantity": 2, "condition": "USED" }
```

Enquiry statuses: `OPEN` (no quotes yet) -> `QUOTED` -> `ACCEPTED`, or `CANCELLED`, or `EXPIRED`
(OPEN after 48 h; QUOTED once no quote can still be accepted). An hourly job applies expiry.

## Shop panel (inbox)

| Call | Notes |
|---|---|
| `GET /enquiries/supplier/{supplierId}?status=NEW` | inbox; status NEW / VIEWED / QUOTED / DECLINED |
| `GET /enquiries/supplier/{supplierId}/{enquiryId}` | details; marks it VIEWED |
| `POST /enquiries/supplier/{supplierId}/{enquiryId}/quote` | send or replace the quote |
| `POST /enquiries/supplier/{supplierId}/{enquiryId}/decline` | only before quoting |

Quote body:
```json
{ "productId": "p1", "unitPrice": 150.0, "condition": "USED", "quantity": 2, "notes": "Original, tested" }
```
- `productId` is the catalogue product offered. If the part isn't in the catalogue yet, create it first
  (`POST /products`, then quote with its id).
- The condition must be one the customer asked for (if they chose any).
- Quoting is allowed until the enquiry's `expiresAt`; quoting again replaces the shop's quote.
- Shops don't see who the customer is. Once the customer accepts a shop's quote, that shop's view includes
  `vehicleOwnerId`.

Response fields shared by both views: `id`, `status`, `makeId/makeName`, `modelId/modelName`, `year`,
`categoryId/categoryName`, `productId`, `partNumber`, `description`, `conditions`, `images[{id, filePath}]`
(download `GET /api/v1/uploads/{filePath}`), `recipientCount`, `createdAt`, `expiresAt`, `closedAt`.
Customer view adds `quotes[]` and `acceptedQuoteId`; shop view adds `myStatus`, `myQuote`, `vehicleOwnerId`.

## Errors
400 bad input (message says which), 403 the logged-in user isn't that customer / the shop's owner, 404 enquiry/quote not found (also for someone
else's enquiry), 409 for: no shop covers this vehicle and part, too many open enquiries, enquiry closed or past
its deadline, quote no longer available, shop already quoted (decline).

## Not included yet
Notifications to shops (poll `?status=NEW` for a badge), chat, ranking shops by distance.

## Gateway
Customer and shop endpoints need a logged-in user. `GET /enquiries/matching-shops/count` can be public.
