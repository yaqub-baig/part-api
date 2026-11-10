# Website integration: product search

How the customer website searches spare parts, shows product pages and sends a chosen offer to the cart.
Covers every search endpoint in part-api. Backend branch: `feature/vehicle-fit-search` (vehicle search and
`inventoryPartId` need it).

## 0. Before you start (blocking items)

| # | Item | Status |a
|---|---|---|
| 1 | Backend `feature/vehicle-fit-search` deployed and the vehicle backfill run (`POST /api/v1/products/vehicle-data/backfill`) | pending |
| 2 | Gateway: open the read-only search endpoints to visitors (below). Today only `POST .../products/search/paginated` is public, so product pages and vehicle search need a login | **gateway change needed** |
| 3 | Gateway: the public make/model picker routes only accept numeric ids (`{id:\d+}`) but make/model ids are UUIDs, so `GET /models/make/{uuid}` fails for visitors | **gateway change needed** |

Gateway change (`gateway-api/src/main/resources/application.yml`, `public-paths`), replacing the part
`models/make` and `engines/model` lines:
```yaml
      # part: website product search (read-only)
      - POST /part/api/v1/products/search/advanced
      - GET /part/api/v1/products/search/by-vehicle
      - GET /part/api/v1/products/search/by-name
      - GET /part/api/v1/products/search/by-part-number
      - GET /part/api/v1/products/search/by-category
      - GET /part/api/v1/products/search/by-manufacturer
      - GET /part/api/v1/products/search/by-tag
      - GET /part/api/v1/products/search/by-other-names
      - 'GET /part/api/v1/products/search/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}'
      - 'GET /part/api/v1/products/search/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/suppliers'
      - GET /part/api/v1/attributes            # names for the product page specs table
      # make / model ids are UUIDs
      - 'GET /part/api/v1/models/make/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}'
      - 'GET /part/api/v1/engines/model/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}'
```
`GET /part/api/v1/products/search` (all products, unpaged) is deliberately **not** listed.

## 1. Base URL and auth

- Through the gateway: `https://<gateway-host>/part/api/v1/...`. Below, paths start at `/api/v1`
  (the website's `partApi` base is `VITE_PART_URL`, i.e. `<gateway>/part`).
- Search, product pages, dropdown data, images: **no login** (after section 0).
- Cart and orders: **login required** (JWT), see section 8.

## 2. Endpoint map

| Use on the website | Endpoint | Returns |
|---|---|---|
| **Search / listing pages** (keyword, filters, sorting, paging) | `POST /api/v1/products/search/paginated` | page of products |
| Same filters, no paging (small result sets only) | `POST /api/v1/products/search/advanced` | array |
| "Parts for my car" | `GET /api/v1/products/search/by-vehicle` | array |
| Part-number box | `GET /api/v1/products/search/by-part-number?partNumber=` | one product, 404 if none |
| Product page | `GET /api/v1/products/search/{id}?includeSuppliers=true` | one product + attributes + offers, 404 if none |
| Offers (shops) for a product | `GET /api/v1/products/search/{id}/suppliers` | array, **404 when nothing in stock** |
| Simple lists | `by-name`, `by-other-names`, `by-tag`, `by-category`, `by-manufacturer` | array |
| Dropdown data | `GET /api/v1/makes`, `GET /api/v1/models/make/{makeId}`, `GET /api/v1/categories/toplevelcategories`, `GET /api/v1/manufacturers` | GenericResponse (`body`) |
| Attribute names (specs table) | `GET /api/v1/attributes` | GenericResponse (`body`) |

Use **`/paginated` for every listing page**. The `by-*` endpoints return everything that matches, with
no paging, and are only for small lookups.

## 3. What comes back

### Product (`ProductResponse`)
```json
{
  "id": "0c1f8e2a-...",
  "name": "Front brake pad set",
  "description": "Ceramic, low dust",
  "partNumber": "BP-100",
  "otherNames": "brake shoe",
  "tag": "brakes",
  "categoryId": "CAT-001",
  "manufacturerId": "MFG-001",
  "make": "Toyota",
  "model": "Camry, Corolla",
  "year": "2018, 2019, 2020",
  "makeId": "e123566a-...",
  "modelIds": ["b34d163d-...", "ed82c2f2-..."],
  "years": [2018, 2019, 2020],
  "images": [{ "id": 12, "fileName": "a1_pad.jpg", "filePath": "photos/a1_pad.jpg", "mediaType": "image/jpeg" }],
  "availableSupplierCount": 3,
  "suppliers": [ /* only when includeSuppliers=true, see below */ ],
  "attributes": [ /* product page only: { id, attributeId, attributeValue, productId } */ ]
}
```
- **Image URL**: `<partApi base>/api/v1/uploads/<image.filePath>`, e.g.
  `https://<gateway>/part/api/v1/uploads/photos/a1_pad.jpg` (public, usable in `<img src>`).
- `availableSupplierCount`: shops with this part **in stock** (with a condition filter: only matching listings).
- `make` / `model` / `year` are display text; `makeId` / `modelIds` / `years` are the real fit.

### Offer (`SupplierAvailabilityResponse`)
One per shop listing that is active and in stock:
```json
{
  "inventoryPartId": 77,
  "supplierId": "5",
  "supplierName": "Al Noor Spare Parts",
  "supplierBrandName": "Al Noor",
  "phoneNumber": "+971...",
  "email": "sales@...",
  "website": "https://...",
  "locationId": "12",
  "productCondition": "GENUINE",
  "unitPrice": 120.0,
  "quantityInStock": 3,
  "sku": "ALN-BP100",
  "isActive": true
}
```
- **`inventoryPartId` is what the cart and buy-now need** (section 8). Not the product id.
- `productCondition`: `GENUINE`, `USED` or `AFTER_MARKET`.

### Page (`/paginated`)
```json
{ "content": [ /* products */ ], "page": 0, "size": 20, "totalElements": 134, "totalPages": 7,
  "first": true, "last": false, "numberOfElements": 20, "empty": false }
```

## 4. The main search: `POST /api/v1/products/search/paginated`

Query param: `includeSuppliers` (default `false`). Body (every field optional; all given filters are combined with AND):

| Field | Type | Match |
|---|---|---|
| `name` | string | contains, case-insensitive |
| `otherNames` | string | contains, case-insensitive (alternative names) |
| `tag` | string | contains, case-insensitive |
| `partNumber` | string | exact |
| `categoryId` | string | exact |
| `manufacturerId` | string | exact (part brand, e.g. Bosch) |
| `makeId` / `make` | string | vehicle make; id preferred, name is looked up |
| `modelId` / `model` | string | vehicle model; id preferred, name is looked up |
| `year` | number | vehicle model year, matches products whose fit includes it |
| `conditions` | string[] | product has an in-stock offer in one of these: `GENUINE`, `USED`, `AFTER_MARKET` |
| `inStockOnly` | boolean | at least one shop has it in stock |
| `minSuppliers` / `maxSuppliers` | number | number of shops with it in stock |
| `sortBy` | string | `name`, `partNumber`, `supplierCount` (only these) |
| `sortDirection` | string | `ASC` (default) or `DESC` |
| `page` | number | 0-based, default 0 |
| `size` | number | 1-50, default 10 |

Behaviour:
- Filters run in the database, so `totalElements` / `totalPages` are exact.
- An unknown make/model **name** returns an empty page (not an error). Ids from the dropdowns never miss.
- With `conditions`, `availableSupplierCount` and `suppliers` only include matching offers.
- Vehicle filters don't return parts with no vehicle data (universal parts such as oil); search those by name/category.

### Ready-made requests

Keyword search box:
```json
{ "name": "brake pad", "inStockOnly": true, "page": 0, "size": 20, "sortBy": "name" }
```
Category page:
```json
{ "categoryId": "CAT-001", "page": 0, "size": 20 }
```
Part-brand page (e.g. Bosch):
```json
{ "manufacturerId": "MFG-001", "inStockOnly": true, "page": 0, "size": 20 }
```
"Parts for my car" with a keyword:
```json
{ "makeId": "e123...", "modelId": "b34d...", "year": 2020, "name": "brake", "inStockOnly": true, "page": 0, "size": 20 }
```
Genuine or after-market only, most shops first:
```json
{ "categoryId": "CAT-001", "conditions": ["GENUINE", "AFTER_MARKET"], "sortBy": "supplierCount", "sortDirection": "DESC", "page": 0, "size": 20 }
```

### Keyword box: one field, several kinds of input
`/paginated` has no single "search everything" field; filters are ANDed. Recommended flow:
1. Input looks like a part number (no spaces, has digits) → `GET /by-part-number?partNumber=`; a hit goes
   straight to the product page.
2. Otherwise → `/paginated` with `name`.
3. No results → retry `/paginated` with `otherNames`, then `tag`, and show "Did you mean…" results.

## 5. Vehicle search ("parts for my car")

UI: Make → Model → Year dropdowns.
1. `GET /api/v1/makes` → `body: [{ id, name, logo }]` (logo: image, same URL rule as product images)
2. `GET /api/v1/models/make/{makeId}` → `body: [{ id, name, ... }]` (load after the make is chosen)
3. Year: numbers from 1950 to current year + 5 (built in the UI)

Then either `/paginated` (with `makeId`, `modelId`, `year` + any filters, recommended) or the quick call:
```
GET /api/v1/products/search/by-vehicle?makeId=e123...&modelId=b34d...&year=2020&includeSuppliers=false
```
`by-vehicle` accepts any combination (make only, make + year, …; names `make=`/`model=` also work);
calling it with no parameter returns 400. It is not paged.

Remember the chosen vehicle (e.g. in `localStorage`) and add it to every search as a "My car" filter chip.

## 6. Product page

```
GET /api/v1/products/search/{productId}?includeSuppliers=true
```
Gives the product, its `attributes` and `suppliers` (offers). 404 → "Product not found".

Specs table: attributes carry only `attributeId` + `attributeValue`; load `GET /api/v1/attributes` once
(cache it) and map `attributeId` → attribute name.

Offers block:
- Sort offers by `unitPrice`; group or filter by `productCondition`.
- Each row: shop name, condition badge, price (AED), stock, **Add to cart** (uses `inventoryPartId`).
- No offers (`suppliers: []`, or 404 from `/{id}/suppliers`) → "Currently out of stock".
- "Fits" section: show `make` + `model` + `year`, or use `makeId`/`modelIds`/`years` to check
  against the visitor's saved car ("Fits your 2020 Camry ✓").

## 7. Filters panel data

| Filter | Source | Sends |
|---|---|---|
| Category | `GET /api/v1/categories/toplevelcategories` | `categoryId` |
| Part brand | `GET /api/v1/manufacturers` | `manufacturerId` |
| Make / model / year | section 5 | `makeId`, `modelId`, `year` |
| Condition | fixed: Genuine, Used, After-market | `conditions` |
| In stock | checkbox | `inStockOnly` |
| Sort | Name A-Z, Most shops | `sortBy` + `sortDirection` |

Cache dropdown data for the session; it changes rarely.

## 8. From search to cart (login required)

`ownerId` is the logged-in vehicle owner's id. `partId` is the offer's **`inventoryPartId`**.

| Action | Call |
|---|---|
| Add to cart | `POST /api/v1/cart/{ownerId}/add?partId={inventoryPartId}&quantity=1` |
| View cart | `GET /api/v1/cart/{ownerId}` |
| Change quantity (0 removes) | `PUT /api/v1/cart/{ownerId}/items/{inventoryPartId}?quantity=2` |
| Remove | `DELETE /api/v1/cart/{ownerId}/items/{inventoryPartId}` |
| Buy one offer now | `POST /api/v1/orders/buy-now/{ownerId}?partId={inventoryPartId}&quantity=1&paymentMethod=COD` (or `ONLINE`) |
| Checkout the cart | `POST /api/v1/orders/place/{ownerId}?paymentMethod=COD` (or `ONLINE`) |

`ONLINE` responses include `payment.paymentUrl` / `qrCodeImage`; redirect the user there and then poll
`GET /api/v1/orders/{orderId}` for the result. Not logged in → send the visitor to sign-in, then retry.

## 9. Example code (axios)

```js
import { partApi } from "@/api/axiosInstance";

export const imageUrl = (img) =>
  img ? `${import.meta.env.VITE_PART_URL}/api/v1/uploads/${img.filePath}` : "/placeholder-part.png";

// Listing / search page
export async function searchProducts(filters, page = 0, size = 20, signal) {
  const body = { ...filters, page, size };
  Object.keys(body).forEach((k) => (body[k] === "" || body[k] == null) && delete body[k]);
  const { data } = await partApi.post("/api/v1/products/search/paginated", body, { signal });
  return data; // { content, totalElements, totalPages, ... }
}

// Keyword box with part-number shortcut
export async function keywordSearch(text, extra = {}, signal) {
  const q = text.trim();
  if (/^[A-Za-z0-9-]+$/.test(q) && /\d/.test(q)) {
    try {
      const { data } = await partApi.get("/api/v1/products/search/by-part-number", { params: { partNumber: q }, signal });
      if (data) return { exact: data };
    } catch (e) { if (e.response?.status !== 404) throw e; }
  }
  let page = await searchProducts({ ...extra, name: q }, 0, 20, signal);
  if (page.empty) page = await searchProducts({ ...extra, otherNames: q }, 0, 20, signal);
  return { page };
}

// Product page
export async function getProduct(id) {
  const { data } = await partApi.get(`/api/v1/products/search/${id}`, { params: { includeSuppliers: true } });
  return data;
}

// Add an offer to the cart (requires login)
export const addToCart = (ownerId, offer, quantity = 1) =>
  partApi.post(`/api/v1/cart/${ownerId}/add`, null, { params: { partId: offer.inventoryPartId, quantity } });
```
For the search box: debounce typing (~400 ms) and cancel the previous request (`AbortController`) so an old
response can't overwrite a newer one.

## 10. Performance

- `includeSuppliers=true` loads offers per product; use it on the product page, not on large listings
  (show `availableSupplierCount` there instead, e.g. "3 shops").
- Keep `size` ≤ 20-30 for listings (max 50).
- Don't call `GET /api/v1/products/search` (all products) or `by-*` endpoints for listing pages.

## 11. Errors

| Status | When | Show |
|---|---|---|
| 400 | `size` > 50 or < 1 (`Validation failed: size: ...`), bad `conditions` value, `by-vehicle` with no filter | fix the request; generic "Invalid search" |
| 404 | `by-part-number` miss, unknown product id, `/{id}/suppliers` with nothing in stock | "Not found" / "Out of stock" |
| 401 | cart/order calls without login (or a search endpoint not yet public, section 0) | sign-in |
| 500 | `sortBy` other than `name`, `partNumber`, `supplierCount` | don't send other values |

Error bodies from part-api are `{ "status": "failed", "code": "...", "message": "..." }`;
`by-part-number`, `/{id}` and `/{id}/suppliers` 404s have an empty body.

## 12. Known limitations

- No single "search everything" keyword field (name / other names / tag / part number are separate filters).
- Every listed model fits every listed year (no per-model year ranges yet).
- Prices are per offer; there is no "lowest price" sort on products yet.
- Supplier contact details (phone, email) are returned in offers; hide them in the UI if the business wants
  all sales to go through the cart.
