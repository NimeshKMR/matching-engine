// package com.nimeshkmr.benchmark;

// import org.openjdk.jmh.annotations.*;

// import com.nimeshkmr.core.LimitOrder;
// import com.nimeshkmr.core.MarketOrder;
// import com.nimeshkmr.core.OrderBook;
// import com.nimeshkmr.core.Side;

// import java.math.BigDecimal;
// import java.util.concurrent.TimeUnit;

// @BenchmarkMode(Mode.Throughput)
// @OutputTimeUnit(TimeUnit.SECONDS)
// @Warmup(iterations = 3, time = 1)
// @Measurement(iterations = 5, time = 1)
// @Fork(2)
// @State(Scope.Thread)
// public class MatchingEngineJmhBenchmark {

//     // ============================
//     // ADD ORDER
//     // ============================

//     private OrderBook addBook;
//     private long addId;

//     @Setup(Level.Iteration)
//     public void setupAdd() {
//         addBook = new OrderBook();
//         addId = 0;
//     }

//     @Benchmark
//     public void addOrder() {
//         addBook.placeOrder(
//                 createLimitOrder(
//                         addId++,
//                         Side.BUY,
//                         "100",
//                         100
//                 )
//         );
//     }

//     // ============================
//     // LIMIT MATCH
//     // ============================

//     private OrderBook matchBook;
//     private long matchId;

//     @Setup(Level.Iteration)
//     public void setupMatch() {
//         matchBook = new OrderBook();
//         matchId = 0;

//         matchBook.placeOrder(
//                 createLimitOrder(
//                         matchId++,
//                         Side.SELL,
//                         "100",
//                         100
//                 )
//         );
//     }

//     @Benchmark
//     public void matchOrder() {

//         // Incoming limit BUY crosses the resting SELL
//         matchBook.placeOrder(
//                 createLimitOrder(
//                         matchId++,
//                         Side.BUY,
//                         "101",
//                         100
//                 )
//         );

//         // Replenish the book so every invocation has liquidity
//         matchBook.placeOrder(
//                 createLimitOrder(
//                         matchId++,
//                         Side.SELL,
//                         "100",
//                         100
//                 )
//         );
//     }

//     // ============================
//     // MARKET MATCH
//     // ============================

//     private OrderBook marketBook;
//     private long marketId;

//     @Setup(Level.Iteration)
//     public void setupMarket() {
//         marketBook = new OrderBook();
//         marketId = 0;

//         marketBook.placeOrder(
//                 createLimitOrder(
//                         marketId++,
//                         Side.SELL,
//                         "100",
//                         100
//                 )
//         );
//     }

//     @Benchmark
//     public void marketOrder() {

//         // Market BUY consumes the resting SELL
//         marketBook.placeOrder(
//                 createMarketOrder(
//                         marketId++,
//                         Side.BUY,
//                         100
//                 )
//         );

//         // Replenish liquidity
//         marketBook.placeOrder(
//                 createLimitOrder(
//                         marketId++,
//                         Side.SELL,
//                         "100",
//                         100
//                 )
//         );
//     }

//     // ============================
//     // CANCEL ORDER
//     // ============================

//     private OrderBook cancelBook;
//     private long cancelId;

//     @Setup(Level.Iteration)
//     public void setupCancel() {
//         cancelBook = new OrderBook();
//         cancelId = 0;

//         for (int i = 0; i < 10_000; i++) {
//             cancelBook.placeOrder(
//                     createLimitOrder(
//                             cancelId++,
//                             Side.BUY,
//                             "100",
//                             100
//                     )
//             );
//         }
//     }

//     @Benchmark
//     public void cancelOrder() {

//         long id = cancelId - 10_000;

//         cancelBook.removeOrder(id);

//         // Re-add the order so the book remains at roughly
//         // the same size throughout the benchmark.
//         cancelBook.placeOrder(
//                 createLimitOrder(
//                         id,
//                         Side.BUY,
//                         "100",
//                         100
//                 )
//         );

//         cancelId++;
//     }

//     // ============================
//     // ORDER CREATION
//     // ============================

//     private LimitOrder createLimitOrder(
//             long id,
//             Side side,
//             String price,
//             long quantity
//     ) {
//         return new LimitOrder(
//                 id,
//                 1L,
//                 side,
//                 quantity,
//                 new BigDecimal(price)
//         );
//     }

//     private MarketOrder createMarketOrder(
//             long id,
//             Side side,
//             long quantity
//     ) {
//         return new MarketOrder(
//                 id,
//                 1L,
//                 side,
//                 quantity
//         );
//     }
// }