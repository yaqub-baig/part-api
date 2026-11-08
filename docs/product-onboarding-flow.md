# Product Onboarding Flow - UML Sequence Diagram

## 1. Master Data Setup (Prerequisites)

```mermaid
sequenceDiagram
actor Admin
participant CategoryController
participant ManufacturerController
participant AttributeController
participant CategoryAttributeController
participant Services
participant Database

Note over Admin,Database: Step 1: Create Categories
Admin->>CategoryController: POST /categories (CategoryListPayload)
CategoryController->>Services: createCategory(List<CategoryRequest>)
Services->>Services: Generate UUID for each category
Services->>Database: INSERT INTO category
Database-->>Services: Category created
Services-->>Admin: 201 Created (categories with IDs)

Note over Admin,Database: Step 2: Create Manufacturers
Admin->>ManufacturerController: POST /manufacturers (ManufacturerListPayload)
ManufacturerController->>Services: createManufacturer(requests)
Services->>Database: INSERT INTO manufacturer
Database->>Services: Manufacturers created
Services->>Admin: 201 Created

Note over Admin,Database: Step 3: Define Attributes
Admin->>AttributeController: POST /attributes (AttributeListPayload)
AttributeController->>Services: createAttribute(requests)
Services->>Database: INSERT INTO attribute
Database->>Services: Attributes created
Services->>Admin: 201 Created (e.g., "Color", "Material", "Warranty")

Note over Admin,Database: Step 4: Link Attributes to Categories
Admin->>CategoryAttributeController: POST /categoryattributes
Note over Admin: Define which attributes belong to which category<br/>e.g., "Engine Oil" category requires "Viscosity", "Volume"
CategoryAttributeController->>Services: createCategoryAttribute(categoryId, attributeId)
Services->>Database: INSERT INTO category_attribute
Database->>Services: Category-Attribute link created
Services->>Admin: 201 Created
```

## 2. Product Creation by Supplier

```mermaid
sequenceDiagram
actor Supplier
participant ProductController
participant ProductService
participant CategoryService
participant ProductAttributeService
participant ProductRepo
participant ProductAttributeRepo
participant Database

Supplier->>ProductController: POST /products (ProductListPayload)
Note over Supplier: Includes: name, categoryId, supplierId,<br/>manufacturerId, price, stock, attributes[]

ProductController->>ProductService: createProduct(List<ProductRequest>)

loop For each ProductRequest
    Note over ProductService: Validate product data
    Note over ProductService: Duplicates are detected by part number, not name
    ProductService->>ProductService: ensurePartNumberAvailable(partNumber)

    alt Part number already used (or repeated in the same request)
        ProductService->>ProductController: AlreadyExistException
        ProductController->>Supplier: 409 Conflict
    end

    Note over ProductService: Validate Category exists
    ProductService->>CategoryService: getCategoryById(categoryId)
    CategoryService->>Database: SELECT FROM category WHERE id=?
    Database->>CategoryService: Category
    CategoryService->>ProductService: Category exists

    Note over ProductService: Generate Product ID
    ProductService->>ProductService: generateGUID()
    ProductService->>ProductService: prepareProduct(request)
    Note over ProductService: Set productId, supplierId,<br/>categoryId, price, stock, etc.

    ProductService->>ProductRepo: save(product)
    ProductRepo->>Database: INSERT INTO product
    Database->>ProductRepo: Product created
    ProductRepo->>ProductService: Product entity

    alt Product has Attributes
        loop For each attribute in request
            ProductService->>ProductService: Generate attribute GUID
            ProductService->>ProductService: Build ProductAttribute entity
            Note over ProductService: Link productId + attributeId + attributeValue
        end

        ProductService->>ProductAttributeRepo: saveAll(productAttributes)
        ProductAttributeRepo->>Database: INSERT INTO product_attribute
        Database->>ProductAttributeRepo: Attributes saved
        ProductAttributeRepo->>ProductService: Success
    end

    ProductService->>ProductService: Add product to list
end

ProductService->>ProductController: GenericResponse(List<Product>)
ProductController->>Supplier: 201 Created (products with IDs)
```

## 3. Product with Inventory Creation (Complete Flow)

```mermaid
sequenceDiagram
actor Supplier
participant ProductController
participant InventoryController
participant ProductService
participant InventoryService
participant ProductRepo
participant InventoryPartRepo
participant Database

Note over Supplier,Database: Step 1: Create Product (Catalog Item)
Supplier->>ProductController: POST /products (ProductRequest)
ProductController->>ProductService: createProduct(request)
ProductService->>ProductService: generateGUID() -> productId
ProductService->>ProductRepo: save(product)
ProductRepo->>Database: INSERT INTO product
Database->>ProductRepo: Product created
ProductRepo->>ProductService: Product (productId="abc-123")
ProductService->>ProductController: GenericResponse
ProductController->>Supplier: 201 Created (productId="abc-123")

Note over Supplier,Database: Step 2: Create Inventory Part (Warehouse Tracking)
Supplier->>InventoryController: POST /inventory/parts (InventoryPartRequest)
Note over Supplier: Includes: productId="abc-123",<br/>supplierId, location="Shelf-A1",<br/>quantityInStock=100

InventoryController->>InventoryService: addPart(request)
InventoryService->>InventoryService: prepareInventoryPart(request)
Note over InventoryService: Link productId, supplierId<br/>Set quantityInStock, location, isActive=true

InventoryService->>InventoryPartRepo: save(inventoryPart)
InventoryPartRepo->>Database: INSERT INTO inventory_part
Database->>InventoryPartRepo: InventoryPart created
InventoryPartRepo->>InventoryService: InventoryPart entity

InventoryService->>InventoryController: InventoryPart
InventoryController->>Supplier: 200 OK (inventoryPartId, details)

Note over Supplier,Database: Product and InventoryPart are now linked via productId
```

## 4. Product Data Model

```mermaid
classDiagram
class Product {
    +String id (UUID)
    +String name
    +String description
    +Double price
    +Integer stock
    +String sku
    +String partNumber
    +String categoryId (FK)
    +String supplierId (FK)
    +String manufacturerId (FK)
    +String makeId (FK make)
    +Set~String~ modelIds (product_model)
    +Set~Integer~ years (product_year)
    +String make, model, year (display text)
    +String partCondition
    +String imageId
}

class Category {
    +String id (UUID)
    +String name
    +String code
    +String parentCategoryId
}

class Manufacturer {
    +String id
    +String name
}

class ProductAttribute {
    +String id (UUID)
    +String productId (FK)
    +String attributeId (FK)
    +String attributeValue
}

class Attribute {
    +String id
    +String name
}

class CategoryAttribute {
    +String id
    +String categoryId (FK)
    +String attributeId (FK)
}

class InventoryPart {
    +Integer id
    +String productId (FK)
    +String supplierId (FK)
    +Integer quantityInStock
    +String location
    +Boolean isActive
}

Product "many" --> "1" Category
Product "many" --> "1" Manufacturer
Product "1" --> "many" ProductAttribute
ProductAttribute "many" --> "1" Attribute
Category "1" --> "many" CategoryAttribute
CategoryAttribute "many" --> "1" Attribute
Product "1" --> "1" InventoryPart : linked via productId
```
## 5. Vehicle fit (make / model / year)

A product fits one make, any number of its models and any number of model years
(every listed model x every listed year).

| Stored in | Holds | Used for |
|---|---|---|
| `product.make_id` | `make.id` | search |
| `product_model (product_id, model_id)` | one row per `model.id` | search |
| `product_year (product_id, year)` | one row per year | search |
| `product.make`, `product.model`, `product.year` | `"Toyota"`, `"Camry, Corolla"`, `"2018, 2019"` | display, old clients |

**Saving** (`POST/PUT /products`): send `makeId`, `modelIds`, `years`, or the legacy text the panel
sends (`make` name, comma-separated `model` names, comma-separated `year`, ranges like `2018-2022`).
`VehicleFitResolver` looks names up, checks that every model belongs to the make and that years are
between 1950 and next year + 5, and rewrites the text columns from the resolved names.
Unknown make/model, a model of another make, or a bad year: 400 with the offending value.
On update, parts not sent are kept.

**Searching**: `POST /products/search/paginated` and `/advanced` (`makeId`/`make`, `modelId`/`model`, `year`),
`GET /products/search/by-vehicle?makeId=&modelId=&year=` (any combination, names also accepted),
`GET /products/year/{year}/make/{make}/model/{model}`. Names are resolved to ids first; an unknown name
returns no results. Products without vehicle data are not returned by vehicle searches.

**Existing products**: `POST /products/vehicle-data/backfill?dryRun=true` reports what would convert and
which values don't match; `dryRun=false` converts the resolvable ones. Already converted products are skipped.
