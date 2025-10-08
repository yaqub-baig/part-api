# Inventory Management Flow - UML Sequence Diagram

## 1. Add Stock to Inventory (Stock IN)

```mermaid
sequenceDiagram
    actor WarehouseStaff
    participant InventoryController
    participant InventoryService
    participant ProductRepo
    participant InventoryPartRepo
    participant InventoryTxnRepo
    participant Database

    Note over WarehouseStaff,Database: Scenario: New stock arrives from supplier

    WarehouseStaff->>InventoryController: POST /inventory/parts/{partId}/add-stock
    Note over WarehouseStaff: Parameters: quantity=100, note="PO-12345"
    activate InventoryController

    InventoryController->>InventoryService: addStock(partId, quantity, note)
    activate InventoryService

    InventoryService->>InventoryPartRepo: findById(partId)
    InventoryPartRepo->>Database: SELECT FROM inventory_part WHERE id=?
    Database-->>InventoryPartRepo: InventoryPart
    InventoryPartRepo-->>InventoryService: InventoryPart entity

    alt Part Not Found
        InventoryService-->>InventoryController: RuntimeException("Part not found")
        InventoryController-->>WarehouseStaff: 404 Not Found
    end

    Note over InventoryService: Update inventory quantity
    InventoryService->>InventoryService: quantityInStock += quantity
    InventoryService->>InventoryPartRepo: save(inventoryPart)
    InventoryPartRepo->>Database: UPDATE inventory_part SET quantity_in_stock=?
    Database-->>InventoryPartRepo: Success
    InventoryPartRepo-->>InventoryService: Updated InventoryPart

    alt InventoryPart has linked Product
        Note over InventoryService: Sync Product stock
        InventoryService->>ProductRepo: findById(productId)
        ProductRepo->>Database: SELECT FROM product WHERE id=?
        Database-->>ProductRepo: Product
        ProductRepo-->>InventoryService: Product entity

        InventoryService->>InventoryService: product.stock += quantity
        InventoryService->>ProductRepo: save(product)
        ProductRepo->>Database: UPDATE product SET stock=?
        Database-->>ProductRepo: Success
        ProductRepo-->>InventoryService: Updated Product
    end

    Note over InventoryService: Create audit trail
    InventoryService->>InventoryService: Build InventoryTransaction
    Note over InventoryService: transactionType=ADD<br/>quantity=100<br/>referenceNote="PO-12345"<br/>timestamp=now()

    InventoryService->>InventoryTxnRepo: save(transaction)
    InventoryTxnRepo->>Database: INSERT INTO inventory_transaction
    Database-->>InventoryTxnRepo: Transaction saved
    InventoryTxnRepo-->>InventoryService: InventoryTransaction

    InventoryService-->>InventoryController: Success
    deactivate InventoryService
    InventoryController-->>WarehouseStaff: 200 OK "Stock added successfully"
    deactivate InventoryController
```

## 2. Remove Stock from Inventory (Stock OUT)

```mermaid
sequenceDiagram
    actor System
    participant OrderService
    participant InventoryService
    participant InventoryPartRepo
    participant ProductRepo
    participant InventoryTxnRepo
    participant Database

    Note over System,Database: Scenario: Customer places order

    System->>OrderService: placeOrder(orderId, items)
    activate OrderService

    loop For each order item
        OrderService->>InventoryService: removeStock(partId, quantity, "Order-789")
        activate InventoryService

        InventoryService->>InventoryPartRepo: findById(partId)
        InventoryPartRepo->>Database: SELECT FROM inventory_part WHERE id=?
        Database-->>InventoryPartRepo: InventoryPart
        InventoryPartRepo-->>InventoryService: InventoryPart

        alt Not Enough Stock
            InventoryService->>InventoryService: Check: quantityInStock >= quantity
            InventoryService-->>OrderService: IllegalArgumentException("Not enough stock")
            OrderService-->>System: Order Failed (Out of Stock)
        end

        Note over InventoryService: Deduct inventory
        InventoryService->>InventoryService: quantityInStock -= quantity
        InventoryService->>InventoryPartRepo: save(inventoryPart)
        InventoryPartRepo->>Database: UPDATE inventory_part SET quantity_in_stock=?
        Database-->>InventoryPartRepo: Success

        alt Product Linked
            Note over InventoryService: Sync Product stock
            InventoryService->>ProductRepo: findById(productId)
            ProductRepo->>Database: SELECT FROM product WHERE id=?
            Database-->>ProductRepo: Product
            InventoryService->>InventoryService: product.stock -= quantity
            InventoryService->>ProductRepo: save(product)
            ProductRepo->>Database: UPDATE product SET stock=?
            Database-->>ProductRepo: Success
        end

        Note over InventoryService: Create audit trail
        InventoryService->>InventoryService: Build InventoryTransaction
        Note over InventoryService: transactionType=REMOVE<br/>quantity=5<br/>referenceNote="Order-789"

        InventoryService->>InventoryTxnRepo: save(transaction)
        InventoryTxnRepo->>Database: INSERT INTO inventory_transaction
        Database-->>InventoryTxnRepo: Transaction saved

        InventoryService-->>OrderService: Success
        deactivate InventoryService
    end

    OrderService-->>System: Order Placed Successfully
    deactivate OrderService
```

## 3. Stock Reconciliation & Sync

```mermaid
sequenceDiagram
    actor Admin
    participant InventoryController
    participant InventoryService
    participant ProductRepo
    participant InventoryPartRepo
    participant Database

    Admin->>InventoryController: GET /inventory/parts/low-stock?threshold=10
    InventoryController->>InventoryService: getLowStockParts(10)
    InventoryService->>InventoryPartRepo: findByQuantityInStockLessThan(10)
    InventoryPartRepo->>Database: SELECT FROM inventory_part WHERE quantity_in_stock < 10
    Database-->>InventoryPartRepo: List<InventoryPart>
    InventoryPartRepo-->>InventoryService: Low stock items
    InventoryService-->>InventoryController: List<InventoryPart>
    InventoryController-->>Admin: 200 OK (low stock parts)

    alt Stock Mismatch Detected
        Admin->>InventoryController: POST /inventory/sync/{productId}
        InventoryController->>InventoryService: syncProductStock(productId)

        InventoryService->>ProductRepo: findById(productId)
        ProductRepo->>Database: SELECT FROM product WHERE id=?
        Database-->>ProductRepo: Product
        ProductRepo-->>InventoryService: Product

        InventoryService->>InventoryPartRepo: findByProductId(productId)
        InventoryPartRepo->>Database: SELECT FROM inventory_part WHERE product_id=?
        Database-->>InventoryPartRepo: InventoryPart
        InventoryPartRepo-->>InventoryService: InventoryPart

        Note over InventoryService: Sync: product.stock = inventoryPart.quantityInStock
        InventoryService->>InventoryService: product.setStock(inventoryPart.getQuantityInStock())
        InventoryService->>ProductRepo: save(product)
        ProductRepo->>Database: UPDATE product SET stock=?
        Database-->>ProductRepo: Success

        InventoryService-->>InventoryController: Success
        InventoryController-->>Admin: 200 OK "Stock synced"
    end
```

## 4. View Inventory Transactions (Audit Trail)

```mermaid
sequenceDiagram
    actor Admin
    participant InventoryController
    participant InventoryService
    participant InventoryTxnRepo
    participant Database

    Admin->>InventoryController: GET /inventory/parts/{partId}/transactions
    activate InventoryController

    InventoryController->>InventoryService: getTransactionsForPart(partId)
    activate InventoryService

    InventoryService->>InventoryService: findPartById(partId)
    InventoryService->>InventoryTxnRepo: findByPart(inventoryPart)
    InventoryTxnRepo->>Database: SELECT FROM inventory_transaction WHERE part_id=? ORDER BY timestamp DESC
    Database-->>InventoryTxnRepo: List<InventoryTransaction>
    InventoryTxnRepo-->>InventoryService: Transactions

    InventoryService-->>InventoryController: List<InventoryTransaction>
    deactivate InventoryService

    InventoryController-->>Admin: 200 OK
    Note over Admin: Shows:<br/>- Transaction type (ADD/REMOVE)<br/>- Quantity<br/>- Reference note (PO/Order)<br/>- Timestamp
    deactivate InventoryController
```

## 5. Inventory Entity Relationships

```mermaid
classDiagram
    class InventoryPart {
        +Integer id (PK)
        +String productId (FK)
        +String supplierId (FK)
        +String partName
        +String partNumber
        +Integer quantityInStock
        +Double unitPrice
        +String location
        +Boolean isActive
    }

    class Product {
        +String id (PK)
        +String name
        +Integer stock
        +String supplierId (FK)
        +String categoryId (FK)
    }

    class InventoryTransaction {
        +Integer id (PK)
        +InventoryPart part (FK)
        +Integer quantity
        +TransactionType type
        +String referenceNote
        +LocalDateTime timestamp
    }

    class TransactionType {
        <<enumeration>>
        ADD
        REMOVE
    }

    InventoryPart "1" --> "1" Product : linked via productId
    InventoryPart "1" --> "many" InventoryTransaction : has history
    InventoryTransaction "many" --> "1" TransactionType : type
```

## 6. Stock Flow State Diagram

```mermaid
stateDiagram-v2
    [*] --> Available: Initial Stock Added
    Available -->Reserved: Order Placed (Reserved)
    Reserved -->Shipped: Order Confirmed & Shipped
    Shipped -->[*]: Stock Removed from Inventory

    Available -->LowStock: Quantity < Threshold
    LowStock -->Available: Stock Replenished
    LowStock -->OutOfStock: Quantity = 0
    OutOfStock -->Available: Stock Added

    Available -->Reconciliation: Stock Mismatch Detected
    Reconciliation -->Available: Synced with Product
```