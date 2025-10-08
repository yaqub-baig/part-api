# Part Searching Flow - UML Sequence Diagram

## 1. Simple Search by Category

```mermaid
sequenceDiagram
    actor Customer
    participant ProductController
    participant ProductService
    participant ProductRepo
    participant ProductAttributeService
    participant Database

    Customer->>ProductController: GET /products/category/{categoryId}
    activate ProductController

    ProductController->>ProductService: searchProductsByCategory(categoryId)
    activate ProductService

    ProductService->>ProductRepo: findByCategoryId(categoryId)
    ProductRepo->>Database: SELECT FROM product WHERE category_id=?
    Database-->>ProductRepo: List<Product>
    ProductRepo-->>ProductService: List<Product>

    loop For each product
        Note over ProductService: Build ProductResponse with attributes
        ProductService->>ProductAttributeService: getProductAttributeByProductId(productId)
        ProductAttributeService->>Database: SELECT FROM product_attribute WHERE product_id=?
        Database-->>ProductAttributeService: List<ProductAttribute>
        ProductAttributeService-->>ProductService: Product attributes

        ProductService->>ProductService: Build ProductResponse
        Note over ProductService: Include: id, name, price, stock,<br/>supplierId, attributes, attributeUrl
    end

    ProductService-->>ProductController: GenericResponse(List<ProductResponse>)
    deactivate ProductService

    ProductController-->>Customer: 200 OK (matching products)
    deactivate ProductController
```

## 2. Advanced Search - Category + Supplier Location

```mermaid
sequenceDiagram
    actor Customer
    participant SearchController
    participant ProductService
    participant SupplierService
    participant LocationService
    participant ProductRepo
    participant SupplierRepo
    participant LocationRepo
    participant Database

    Note over Customer: User searches: "Brake pads in Dubai"

    Customer->>SearchController: GET /products/search?category=brake-pads&city=Dubai
    activate SearchController

    Note over SearchController: Step 1: Find suppliers in Dubai
    SearchController->>LocationService: findLocationsByCity("Dubai")
    LocationService->>LocationRepo: findByCity("Dubai")
    LocationRepo->>Database: SELECT FROM location WHERE city='Dubai'
    Database-->>LocationRepo: List<Location>
    LocationRepo-->>LocationService: List<Location> (locationIds)

    SearchController->>SupplierService: findByLocationIds(locationIds)
    SupplierService->>SupplierRepo: findByLocationIn(locationIds)
    SupplierRepo->>Database: SELECT FROM supplier WHERE location_id IN (?)
    Database-->>SupplierRepo: List<Supplier>
    SupplierRepo-->>SupplierService: List<Supplier> (supplierIds)

    Note over SearchController: Step 2: Find products by category + suppliers
    SearchController->>ProductService: searchByCategoryAndSuppliers(categoryId, supplierIds)
    ProductService->>ProductRepo: Custom Query
    ProductRepo->>Database: SELECT FROM product<br/>WHERE category_id=? AND supplier_id IN (?)
    Database-->>ProductRepo: List<Product>
    ProductRepo-->>ProductService: Filtered products

    loop For each product
        ProductService->>ProductService: Enrich with attributes, supplier info
    end

    ProductService-->>SearchController: List<ProductResponse>
    SearchController-->>Customer: 200 OK (products matching criteria)
    deactivate SearchController
```

## 3. Attribute-Based Search (Filter by Specifications)

```mermaid
sequenceDiagram
    actor Customer
    participant SearchController
    participant ProductService
    participant ProductAttributeRepo
    participant ProductRepo
    participant Database

    Note over Customer: Search: "Engine Oil with Viscosity=5W-30"

    Customer->>SearchController: GET /products/search?category=engine-oil&attribute=Viscosity&value=5W-30
    activate SearchController

    Note over SearchController: Step 1: Find attribute ID
    SearchController->>ProductAttributeRepo: findAttributeByName("Viscosity")
    ProductAttributeRepo->>Database: SELECT FROM attribute WHERE name='Viscosity'
    Database-->>ProductAttributeRepo: Attribute (attributeId)

    Note over SearchController: Step 2: Find products with this attribute value
    SearchController->>ProductAttributeRepo: findByAttributeIdAndValue(attributeId, "5W-30")
    ProductAttributeRepo->>Database: SELECT product_id FROM product_attribute<br/>WHERE attribute_id=? AND attribute_value=?
    Database-->>ProductAttributeRepo: List<productIds>
    ProductAttributeRepo-->>SearchController: List<productIds>

    Note over SearchController: Step 3: Get full product details
    SearchController->>ProductService: getProductsByIds(productIds)
    ProductService->>ProductRepo: findAllById(productIds)
    ProductRepo->>Database: SELECT FROM product WHERE id IN (?)
    Database-->>ProductRepo: List<Product>
    ProductRepo-->>ProductService: Products

    loop For each product
        ProductService->>ProductAttributeRepo: Get all attributes for product
        ProductAttributeRepo->>Database: SELECT FROM product_attribute WHERE product_id=?
        Database-->>ProductAttributeRepo: Product attributes
        ProductService->>ProductService: Build ProductResponse
    end

    ProductService-->>SearchController: List<ProductResponse>
    SearchController-->>Customer: 200 OK (filtered products)
    deactivate SearchController
```

## 4. Vehicle Compatibility Search

```mermaid
sequenceDiagram
    actor Customer
    participant ProductController
    participant ProductService
    participant ProductRepo
    participant Database

    Note over Customer: Find parts for: 2020 Toyota Camry

    Customer->>ProductController: GET /products/year/2020/make/Toyota/model/Camry
    activate ProductController

    ProductController->>ProductService: searchProductsByYearMakeModel(2020, "Toyota", "Camry")
    activate ProductService

    ProductService->>ProductRepo: findByYearAndMakeAndModel(2020, "Toyota", "Camry")
    ProductRepo->>Database: SELECT FROM product<br/>WHERE year=2020 AND make='Toyota' AND model='Camry'
    Database-->>ProductRepo: List<Product>
    ProductRepo-->>ProductService: Compatible products

    loop For each product
        ProductService->>ProductService: Enrich with attributes & details
    end

    ProductService-->>ProductController: GenericResponse(List<ProductResponse>)
    deactivate ProductService

    ProductController-->>Customer: 200 OK (compatible parts)
    deactivate ProductController
```

## 5. Complex Multi-Filter Search (Category + Location + Attributes + Vehicle)

```mermaid
sequenceDiagram
    actor Customer
    participant SearchService
    participant ProductRepo
    participant SupplierRepo
    participant ProductAttributeRepo
    participant Database

    Note over Customer: Ultimate Search:<br/>"Brake pads for 2020 Toyota Camry<br/>with Ceramic material<br/>from suppliers in Dubai"

    Customer->>SearchService: complexSearch(filters)
    Note over Customer: Filters:<br/>- category: Brake Pads<br/>- city: Dubai<br/>- year: 2020<br/>- make: Toyota<br/>- model: Camry<br/>- attribute: Material=Ceramic

    activate SearchService

    Note over SearchService: Step 1: Filter by Location
    SearchService->>SupplierRepo: Find suppliers by city
    SupplierRepo->>Database: SELECT id FROM supplier s<br/>JOIN location l ON s.location_id=l.id<br/>WHERE l.city='Dubai'
    Database-->>SupplierRepo: supplierIds
    SupplierRepo-->>SearchService: Dubai supplier IDs

    Note over SearchService: Step 2: Filter by Category + Vehicle + Supplier
    SearchService->>ProductRepo: Complex Query
    ProductRepo->>Database: SELECT id FROM product<br/>WHERE category_id='brake-pads'<br/>AND year=2020<br/>AND make='Toyota'<br/>AND model='Camry'<br/>AND supplier_id IN (Dubai suppliers)
    Database-->>ProductRepo: productIds (filtered)
    ProductRepo-->>SearchService: Candidate products

    Note over SearchService: Step 3: Filter by Attribute (Material=Ceramic)
    SearchService->>ProductAttributeRepo: Filter by attribute
    ProductAttributeRepo->>Database: SELECT product_id FROM product_attribute<br/>WHERE attribute_id='Material'<br/>AND attribute_value='Ceramic'<br/>AND product_id IN (candidate products)
    Database-->>ProductAttributeRepo: Final productIds
    ProductAttributeRepo-->>SearchService: Products matching all criteria

    Note over SearchService: Step 4: Fetch full product details
    SearchService->>ProductRepo: findAllById(productIds)
    ProductRepo->>Database: SELECT * FROM product WHERE id IN (?)
    Database-->>ProductRepo: Products
    ProductRepo-->>SearchService: List<Product>

    loop For each product
        SearchService->>SearchService: Enrich with:<br/>- All attributes<br/>- Supplier details<br/>- Location info<br/>- Stock availability
    end

    SearchService-->>Customer: 200 OK (highly filtered results)
    deactivate SearchService
```

## 6. Search by Part Number (Exact Match)

```mermaid
sequenceDiagram
    actor Customer
    participant ProductController
    participant ProductService
    participant ProductRepo
    participant Database

    Customer->>ProductController: GET /products/partnumber/{partNumber}
    Note over Customer: Example: "ACDelco-PF48"

    activate ProductController
    ProductController->>ProductService: searchProductsByPartNumber("ACDelco-PF48")
    activate ProductService

    ProductService->>ProductRepo: findByPartNumber("ACDelco-PF48")
    ProductRepo->>Database: SELECT FROM product WHERE part_number='ACDelco-PF48'
    Database-->>ProductRepo: Product (exact match)
    ProductRepo-->>ProductService: Product

    alt Multiple Suppliers Selling Same Part
        Note over ProductService: May return multiple products<br/>from different suppliers
        ProductService->>ProductService: Group by supplierId
    end

    ProductService-->>ProductController: GenericResponse(List<ProductResponse>)
    deactivate ProductService

    ProductController-->>Customer: 200 OK (matching products with prices)
    deactivate ProductController
```

## 7. Search Flow State Diagram

```mermaid
stateDiagram-v2
    [*] --> SearchInput : User enters search criteria

    SearchInput --> CategoryFilter : Filter by category
    CategoryFilter --> LocationFilter : Add location filter
    LocationFilter --> VehicleFilter : Add vehicle compatibility
    VehicleFilter --> AttributeFilter : Add attribute filters

    AttributeFilter --> QueryExecution : Build complex query
    QueryExecution --> ResultFetching : Execute on database

    ResultFetching --> ResultEnrichment : Add attributes & details
    ResultEnrichment --> ResultSorting : Sort by relevance/price

    ResultSorting --> ResultsDisplay : Display to customer
    ResultsDisplay --> [*]

    SearchInput --> PartNumberSearch : Direct part number search
    PartNumberSearch --> ExactMatch : Find exact matches
    ExactMatch --> ResultsDisplay
```

## 8. Search Data Flow

```mermaid
flowchart TD
    Start([Start]) --> SearchQuery["Customer Search Query"]

    SearchQuery --> SearchType{"Search Type?"}

    SearchType -->|Category| FilterCategory["Filter by Category"]
    FilterCategory --> GetProducts["Get Products by Category"]
    GetProducts --> ApplyFilters1["Apply Attribute Filters"]

    SearchType -->|Part Number| ExactMatch["Exact Match Search"]
    ExactMatch --> FindByPart["Find by Part Number"]
    FindByPart --> ApplyFilters2["Apply Attribute Filters"]

    SearchType -->|Vehicle| VehicleCompat["Vehicle Compatibility"]
    VehicleCompat --> FilterVehicle["Filter by Year/Make/Model"]
    FilterVehicle --> ApplyFilters3["Apply Attribute Filters"]

    SearchType -->|Supplier| LocationSearch["Location-based Search"]
    LocationSearch --> FindSuppliers["Find Suppliers by Location"]
    FindSuppliers --> GetFromSuppliers["Get Products from Suppliers"]
    GetFromSuppliers --> ApplyFilters4["Apply Attribute Filters"]

    ApplyFilters1 --> CheckStock["Check Stock Availability"]
    ApplyFilters2 --> CheckStock
    ApplyFilters3 --> CheckStock
    ApplyFilters4 --> CheckStock

    CheckStock --> EnrichDetails["Enrich with Details"]
    EnrichDetails --> SortResults["Sort Results"]
    SortResults --> ReturnCustomer["Return to Customer"]
    ReturnCustomer --> End([End])

    style SearchQuery fill:#e1f5ff
    style ReturnCustomer fill:#d4edda
```

## 9. Search Performance Considerations

```mermaid
flowchart TD
    Start([Start]) --> SearchRequest["Search Request"]

    SearchRequest --> UseIndexes{"Use Indexes?"}

    UseIndexes -->|yes| FastQuery["Fast Query < 100ms"]
    FastQuery --> ReturnResults1["Return Results"]

    UseIndexes -->|no| SlowQuery["Slow Query > 1s"]
    SlowQuery --> ConsiderCaching["Consider Caching"]
    ConsiderCaching --> ReturnResults2["Return Results"]

    ReturnResults1 --> CheckResults{"Results > 100?"}
    ReturnResults2 --> CheckResults

    CheckResults -->|yes| Pagination["Implement Pagination"]
    Pagination --> PageResults["Page 1 of N"]

    CheckResults -->|no| ReturnAll["Return All"]
    ReturnAll --> CompleteResults["Complete Results"]

    PageResults --> Stop([Stop])
    CompleteResults --> Stop

    style FastQuery fill:#d4edda
    style SlowQuery fill:#f8d7da
    style SearchRequest fill:#d4edda
```