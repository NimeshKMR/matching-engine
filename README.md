# Java Matching Engine

A Java-based matching engine implementing price-time priority, FIFO execution,
limit and market orders, partial fills, multi-level matching, and order
cancellation.

## Current Status

**Core engine complete**

- Limit + market orders
- Price-time priority + FIFO
- Partial and full fills
- Multi-level matching
- Order cancellation + validation
- OOP order hierarchy
- Event model
- Determinism testing
- 44 automated tests
- JMH performance benchmarks

**Next:** Spring Boot backend.

## Architecture

```text
Order
  ↓
OrderBook
  ↓
Matching Engine
  ↓
OrderResult
  ↓
EngineEvents