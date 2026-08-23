# Java Matching Engine

A Java-based electronic order matching engine implementing **price-time priority**, **FIFO execution**, limit and market orders, partial fills, multi-level matching, order cancellation, event-driven results, and a Spring Boot REST API.

## Current Status

**v1 Complete**

- Limit and market orders
- Price-time priority
- FIFO execution at each price level
- Partial and full fills
- Multi-level matching
- Order cancellation
- Account ownership validation during cancellation
- Duplicate order ID validation
- HTTP request validation
- Domain exception handling
- Event-driven engine results
- Aggregated order-book snapshots
- Spring Boot REST API
- Integration tests
- **57 automated tests**
- Deterministic matching tests
- JMH performance benchmarks

## Architecture

```text
                    HTTP Request
                         │
                         ▼
                 Spring Controller
                         │
                         ▼
                   Web Service
                         │
                         ▼
                     OrderBook
                         │
             ┌───────────┴───────────┐
             │                       │
        Order Matching          Order Cancellation
             │                       │
             └───────────┬───────────┘
                         ▼
                    OrderResult
                         │
                         ▼
                   EngineEvent[]
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
        OrderAccepted  TradeExecuted  OrderCancelled
                         │
                         ▼
                    Web Response
```

## Core Engine

The matching engine maintains two price-level books:

```text
Bids                         Asks
101 × 20                     102 × 15
100 × 35                     103 × 40
 99 × 10                     104 × 20
```

Bids are ordered **highest to lowest price**, while asks are ordered **lowest to highest price**.

Orders at the same price are stored in FIFO queues, providing price-time priority.

### Matching Rules

For a limit order:

- A buy order matches the lowest available ask when `buy price >= ask price`.
- A sell order matches the highest available bid when `sell price <= bid price`.
- Execution occurs at the resting order's price.
- Orders are matched FIFO within the same price level.
- A single incoming order may match against multiple price levels.
- Partially filled orders retain their remaining quantity.

Market orders consume available liquidity starting from the best available price.

## Order Book Data Structure Evolution

One of the main design problems in this project was choosing the right data structure for the order book. The final implementation was not the first idea — it evolved through several designs as the requirements became clearer.

### 1. Priority Queue

The initial idea was to use a priority queue of orders:

```text
PriorityQueue<Order>
```

with a comparator based on price and timestamp.

This made finding the best order straightforward, but it became awkward for:

- maintaining separate bid and ask books
- cancelling arbitrary orders efficiently
- preserving FIFO at each price level
- representing the actual price-level structure of a market

### 2. Comparator: Price + Timestamp

The next idea was to keep a priority queue but explicitly compare:

```text
price → timestamp
```

This captured price-time priority more directly.

However, arbitrary cancellation was still problematic. Finding an order by `orderID` required scanning the queue, and removing an arbitrary order from the priority queue was not naturally efficient.

### 3. `TreeMap<Price, Queue<Order>>` + `Map<OrderID, Order>`

The design then moved toward separating the two concerns:

```text
TreeMap<Price, Queue<Order>>
Map<OrderID, Order>
```

The `TreeMap` provided sorted price levels, while the queue provided FIFO ordering within each price.

The second map provided fast lookup by `orderID` for cancellation.

This was a major improvement because it reflected the actual structure of the order book:

```text
Price Level
    ↓
FIFO Queue
    ↓
Orders
```

But cancellation still required the queue to locate the specific order object before it could be removed efficiently.

### 4. Store Queue Metadata Inside the Order

The next idea was to make each order carry information such as:

```text
price
quantity
timestamp
queue index
```

This was an attempt to make arbitrary cancellation faster by letting the order know where it sat inside its queue.

The problem was that a queue index is not stable under insertion and deletion. Removing one order can shift the positions of other orders, making index maintenance expensive and error-prone.

The design needed a stable position reference instead.

### 5. Final Design

The final implementation uses:

```text
TreeMap<Price, DoublyLinkedList<OrderNode>>
                    +
HashMap<OrderID, OrderNode>
```

Conceptually:

```text
BIDS / ASKS
    │
    ▼
TreeMap<Price, OrderQueue>
    │
    ├── Price 101 → OrderNode ↔ OrderNode ↔ OrderNode
    ├── Price 100 → OrderNode ↔ OrderNode
    └── Price  99 → OrderNode
                         │
                         ▼
                  Order / Order data

HashMap<OrderID, OrderNode>
    │
    ├── 101 → Node
    ├── 102 → Node
    └── 103 → Node
```

Each `OrderNode` participates in a doubly linked FIFO queue through `prev` and `next` references.

This gives the engine:

| Operation | Structure | Purpose |
|---|---|---|
| Best price | `TreeMap` | Sorted price levels |
| FIFO execution | Doubly linked queue | Time priority within a price |
| Order lookup | `HashMap<OrderID, OrderNode>` | Direct order lookup |
| Cancellation | `OrderNode` links | O(1)-style unlinking from its queue |
| Price-level removal | `TreeMap` | Remove empty levels |

This final design cleanly separates the three major requirements of a matching engine:

**price priority + time priority + fast cancellation.**

It also avoids using a mutable queue index whose value would change whenever other orders are inserted or removed.

## Event Model

Engine operations return an `OrderResult` containing the events produced by that operation.

```text
OrderResult
    │
    └── List<EngineEvent>
            ├── OrderAccepted
            ├── TradeExecuted
            └── OrderCancelled
```

For example, placing a limit order that matches two resting orders can produce:

```text
OrderAccepted
TradeExecuted
TradeExecuted
```

Cancellation produces:

```text
OrderCancelled
```

The core event model is separated from HTTP response DTOs through the web-layer mapper.

## REST API

### Place an Order

```http
POST /orders
```

Example:

```json
{
  "orderID": 1,
  "accountID": 100,
  "side": "BUY",
  "type": "LIMIT",
  "quantity": 10,
  "price": 100
}
```

A successful response can contain:

```json
{
  "orderID": 1,
  "eventResponses": [
    {
      "type": "ORDER_ACCEPTED",
      "orderID": 1,
      "accountID": 100
    }
  ]
}
```

### Cancel an Order

```http
DELETE /orders/{orderID}?accountID={accountID}
```

Example:

```http
DELETE /orders/1?accountID=100
```

A successful cancellation produces an `ORDER_CANCELLED` event.

### Get Order Book

```http
GET /orderbook
```

The order book is returned as aggregated price levels:

```json
{
  "bids": [
    {
      "price": 101,
      "quantity": 20
    },
    {
      "price": 100,
      "quantity": 35
    }
  ],
  "asks": [
    {
      "price": 102,
      "quantity": 15
    },
    {
      "price": 103,
      "quantity": 40
    }
  ]
}
```

Bids are sorted highest-to-lowest and asks lowest-to-highest.

## Validation and Error Handling

The API separates domain errors from HTTP concerns.

| Condition | HTTP Status |
|---|---:|
| Invalid request | `400 Bad Request` |
| Order not found | `404 Not Found` |
| Unauthorized cancellation | `403 Forbidden` |
| Duplicate order ID | `409 Conflict` |

Domain exceptions are generated by the core engine and translated into HTTP responses by the Spring `@RestControllerAdvice` layer.

## Testing

The project contains **57 automated tests** covering:

- Order creation
- Limit-order matching
- Market-order matching
- Partial fills
- Multi-level matching
- FIFO execution
- Order cancellation
- Cancellation authorization
- Duplicate order IDs
- Order-book snapshots
- Price-level aggregation
- Bid/ask ordering
- REST controller behavior
- HTTP validation
- Exception handling
- API integration flows
- Deterministic behavior

Run the complete test suite with:

```bash
mvn clean test
```

## Project Structure

```text
src/
├── main/
│   └── java/
│       └── com/nimeshkmr/
│           ├── core/
│           │   ├── Order.java
│           │   ├── LimitOrder.java
│           │   ├── MarketOrder.java
│           │   ├── OrderBook.java
│           │   ├── OrderQueue.java
│           │   ├── OrderNode.java
│           │   ├── Trade.java
│           │   └── ...
│           │
│           ├── events/
│           │   ├── EngineEvent.java
│           │   ├── OrderAccepted.java
│           │   ├── TradeExecuted.java
│           │   ├── OrderCancelled.java
│           │   └── OrderResult.java
│           │
│           └── matching_engine/
│               ├── controller/
│               ├── service/
│               ├── dto/
│               ├── mapper/
│               └── exception/
│
└── test/
    └── java/
        └── ...
```

## Tech Stack

- Java 21
- Spring Boot
- Maven
- JUnit 5
- Mockito
- JMH
- REST API

## Design Goals

The project focuses on correctness, deterministic behavior, and clear separation of responsibilities.

Key design principles include:

- Price-time priority
- FIFO execution
- Deterministic matching
- Separation of domain logic from HTTP concerns
- Explicit order and event models
- Thin service and controller layers
- Domain exceptions translated at the API boundary
- Automated testing of core and HTTP behavior
- Data structures chosen around the actual operational requirements of a matching engine

## Roadmap

### v1 — Complete

Core matching engine and Spring Boot REST API.

### v2 — Planned

- Persistent order and trade storage
- Trade history API
- Improved market-data endpoints
- Real-time order-book updates
- Trading UI
- Performance and concurrency improvements
- Deployment
