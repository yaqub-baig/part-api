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
    ProductService->>ProductService: validateProductName(name)
    ProductService->>ProductService: isProductAlreadyExists(name)

    alt Product Already Exists
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
    +Integer year
    +String make
    +String model
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