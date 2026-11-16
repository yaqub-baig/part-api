# Shop coverage: makes, models, categories, conditions

Backend branch: `feature/shop-coverage`. Phase 1 of part enquiries: a spare-part shop's range is stored as ids
so customer enquiries can be routed to the shops that can help. All paths are on the part service (`/api/v1`).

## What a shop stores

| Field (request and response) | Meaning | Empty means |
|---|---|---|
| `makeIds` | makes the shop covers | covers no make (gets no vehicle enquiries) |
| `modelIds` | models covered; each must belong to one of `makeIds` | all models of its makes |
| `categoryIds` | part categories covered; a parent covers its subcategories | all categories |
| `conditions` | `GENUINE`, `USED`, `AFTER_MARKET` | all conditions |
| `brands`, `models` (text) | display only, rewritten from the ids (`"Nissan, Toyota"`) | |

Years are not part of a shop's range.

## Registration and edit (`POST /suppliers`, `PUT /suppliers/{id}`)

Send ids from the dropdowns (`GET /makes`, `GET /models/make/{makeId}`, `GET /categories`):
```json
{
  "name": "Al Noor Auto Parts",
  "makeIds": ["e123...", "a77b..."],
  "modelIds": ["b34d..."],
  "categoryIds": ["CAT-ENGINE", "CAT-BODY"],
  "conditions": ["GENUINE", "USED"]
}
```
- The old `brands` / `models` text is still accepted (comma-separated names) and converted; ids win when both are sent.
- **Update is partial**: a field not sent (null) keeps its current value; an empty list clears it.
- Changing only `makeIds` drops the shop's models of makes it no longer covers.
- Errors (400, show `message`): unknown make / model / category id or name, a model of a make the shop
  doesn't cover, models without makes, a model name that matches two models (send `modelIds`).
  A brand that isn't in the system yet goes through the existing brand onboarding request (`POST /brand/onboard`).

## Existing shops (admin, once after deploy)

`POST /suppliers/coverage/backfill?dryRun=true` reports which shops' `brands` / `models` text converts and which
names don't match; fix those (edit the shop or add the make/model), then run with `dryRun=false`.
Already converted shops are skipped.

## Panel checklist
- [ ] Make MultiSelect sends `makeIds`; model MultiSelect (models of the selected makes) sends `modelIds`
- [ ] New optional pickers: categories (`categoryIds`) and conditions (`conditions`)
- [ ] Edit form preselects from `makeIds` / `modelIds` / `categoryIds` / `conditions`
- [ ] Removing a make removes its models from the selection
- [ ] 400 `message` shown on save
