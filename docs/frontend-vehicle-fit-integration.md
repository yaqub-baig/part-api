# Frontend integration: product make / model / year

Backend branch: `feature/vehicle-fit-search`. Applies to the panel (`AutoAdda_panel`) and any app that
searches parts by vehicle. All paths below are on the part service (`partApi`, base `/api/v1`).

## 1. What changed, in one minute

- A product's vehicle fit is now stored as **ids**: one make (`makeId`), any number of its models
  (`modelIds`) and any number of model years (`years`). Every listed model fits every listed year.
- Product responses now include `makeId`, `modelIds` and `years` next to the old `make`, `model`, `year` text.
- Search by make / model / year now works for products with several models or years.
- **Nothing breaks if the panel is not changed yet**: the current form (make name + comma-joined model
  names and years) is still accepted and converted to ids. The only new behaviour is that bad values are
  rejected with a 400 (see section 5), so the form must show the error message.

## 2. Where the ids come from (dropdowns)

| Dropdown | Call | Use |
|---|---|---|
| Make | `GET /api/v1/makes` | `id`, `name` |
| Models of a make | `GET /api/v1/models/make/{makeId}` | `id`, `name` |
| Years | built in the UI | numbers from **1950** to **current year + 5** (anything else is rejected) |

The panel already loads these (`useBrandStore`: `getModelsByBrandId`); keep the selected **objects/ids**,
not only their names.

## 3. Saving a product (recommended: send ids)

### Create — `POST /api/v1/products`
```json
{
  "productRequestList": [
    {
      "name": "Front brake pad",
      "partNumber": "BP-100",
      "categoryId": "CAT-001",
      "manufacturerId": "MFG-001",
      "makeId": "e123566a-77ec-41c7-b508-0c56af9ff4e7",
      "modelIds": ["b34d163d-ad51-4f65-865d-7af83349bb72", "ed82c2f2-7ff7-4080-b0d1-f9700eaeb4ac"],
      "years": [2018, 2019, 2020],
      "imageIds": [12, 13]
    }
  ]
}
```

Change in `SparePartModal.jsx` (create and update payloads):
```js
// before
model: selectedModels.join(", "),
year: selectedYears.join(", "),

// after: selectedModels holds model ids (use model.id as the MultiSelect option value)
makeId: selectedBrandId,
modelIds: selectedModels,                 // ["b34d...", "ed82..."]
years: selectedYears.map(Number),         // [2018, 2019, 2020]
// stop sending make / model / year text
```
For the model MultiSelect use `options={availableModels.map(m => ({ label: m.name, value: m.id }))}`.

Rules:
- Every model must belong to the chosen make, otherwise 400.
- Models without a make: 400.
- If both ids and text are sent, the ids win.

### Update — `PUT /api/v1/products` (partial)
Only the fields you send change. For the vehicle fit:

| You send | Result |
|---|---|
| none of `makeId` / `modelIds` / `years` (or the old text fields) | fit unchanged |
| `years: [2021, 2022]` only | years replaced, make and models kept |
| `modelIds: [...]` only | models replaced (must belong to the current make) |
| new `makeId` | **send `modelIds` of the new make too**, otherwise 400 (old models belong to the old make) |
| `makeId: "", modelIds: [], years: []` | fit cleared (universal part) |

### Editing: prefill from the ids
`GET /api/v1/products/{id}` → `body` has:
```json
{ "make": "Toyota", "model": "Camry, Corolla", "year": "2018, 2019, 2020",
  "makeId": "e123...", "modelIds": ["b34d...", "ed82..."], "years": [2018, 2019, 2020] }
```
Use `makeId` / `modelIds` / `years` to preselect; no more splitting the strings.
`modelIds` and `years` are sorted; both are `[]` when not set. Show names via the dropdown data
(or the `make` / `model` text, which is kept in sync).

> Products saved before the migration have `makeId: null` and empty lists until the backend backfill
> has run (section 7). Fall back to the text fields for those.

## 4. Searching by vehicle

### Quick vehicle search — `GET /api/v1/products/search/by-vehicle`
Any combination; at least one is required (400 otherwise).
```
GET /api/v1/products/search/by-vehicle?makeId=e123...&modelId=b34d...&year=2020&includeSuppliers=true
GET /api/v1/products/search/by-vehicle?makeId=e123...&year=2020          (make + year, no model)
GET /api/v1/products/search/by-vehicle?make=Toyota&model=Camry&year=2020  (names also work)
```
Returns a plain array of products (not wrapped in GenericResponse).

### Paginated search with other filters — `POST /api/v1/products/search/paginated`
```json
{
  "makeId": "e123...",
  "modelId": "b34d...",
  "year": 2020,
  "categoryId": "CAT-001",
  "conditions": ["GENUINE", "AFTER_MARKET"],
  "inStockOnly": true,
  "page": 0,
  "size": 10
}
```
Returns `{ content, page, size, totalElements, totalPages, first, last, numberOfElements, empty }`.
`POST /api/v1/products/search/advanced` takes the same body and returns a plain array.

Behaviour to design the UI around:
- **Use ids from the dropdowns.** Names are accepted (`make`, `model`) and looked up, but an unknown or
  misspelt name simply returns **no results** (not an error).
- `year` is one number (the customer's car year). It matches products whose year list contains it.
- `modelId` is one model. Without a make, a model **name** matches that name in every make.
- Products with no vehicle data (universal parts like oil) are **not** returned by vehicle searches;
  find them by name or category.
- Typical customer flow: pick make → load its models → pick model → pick year → search.

## 5. Errors to show

Errors come back as HTTP 400 with:
```json
{ "status": "failed", "code": "400", "message": "Model 'Civic' does not belong to make 'Toyota'; send the models of the new make" }
```
Show `message` to the user. Possible messages:

| Case | Example message |
|---|---|
| unknown make | `Unknown make id '...'` / `Unknown make 'Toyta'` |
| unknown model | `Unknown model id '...'` / `Unknown model 'Camry Hybrid' for make 'Toyota'` |
| model of another make | `Model 'Civic' does not belong to make 'Toyota'; ...` |
| models without make | `A make is required when models are given` |
| duplicate model name in a make (text only) | `Model name 'Camry' matches 2 models of make 'Toyota'; send modelIds instead` |
| bad year | `Year 1949 is outside 1950-2031` / `Invalid year '20x0'; ...` |
| `by-vehicle` with no filter | `Give at least one of year, makeId, make, modelId or model` |

Sending ids from the dropdowns avoids all of these except a make change without new models.

## 6. Response fields reference (products)

| Field | Type | Notes |
|---|---|---|
| `makeId` | string \| null | `make.id` |
| `modelIds` | string[] | sorted, `[]` if none |
| `years` | number[] | sorted, `[]` if none |
| `make` | string \| null | make name (display) |
| `model` | string \| null | `"Camry, Corolla"` (display) |
| `year` | string \| null | `"2018, 2019, 2020"` (display) |

## 7. Rollout order

1. Backend deployed; admin runs `POST /api/v1/products/vehicle-data/backfill?dryRun=true`, fixes the
   reported products, then runs it with `dryRun=false`. **Until then, vehicle searches don't find
   existing products.**
2. Panel: show 400 messages on product save (works with the current form).
3. Panel: switch the form to `makeId` / `modelIds` / `years` and prefill from them (section 3).
4. Customer app / search UI: make → model → year dropdowns calling `by-vehicle` or `paginated` (section 4).

## 8. Checklist

- [ ] Model MultiSelect values are model ids; make id kept from the brand dropdown
- [ ] Create / update send `makeId`, `modelIds`, `years` (numbers)
- [ ] Changing the make clears the selected models and requires new ones
- [ ] Edit form preselects from `makeId` / `modelIds` / `years`, falls back to text for old products
- [ ] 400 `message` shown on save
- [ ] Vehicle search uses ids; empty result state handled ("no parts found for this vehicle")
