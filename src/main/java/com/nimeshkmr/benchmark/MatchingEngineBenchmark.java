// package com.nimeshkmr.benchmark;

// import com.nimeshkmr.model.*;

// import java.math.BigDecimal;

// public class MatchingEngineBenchmark {

//     private static Order createOrder(long id, Side side, String price, long quantity) {
//         return new Order(
//                 id,
//                 1L,
//                 side,
//                 new BigDecimal(price),
//                 quantity
//         );
//     }

//     public static void main(String[] args) {

//         int[] sizes = {10_000, 100_000, 1_000_000};

//         for (int size : sizes) {

//             System.out.println("=================================");
//             System.out.println("Orders: " + size);
//             System.out.println("=================================");

//             benchmarkAdd(size);
//             benchmarkMatching(size);
//             benchmarkCancellation(size);
//         }
//     }

//     private static void benchmarkAdd(int size) {

//         OrderBook book = new OrderBook();

//         long start = System.nanoTime();

//         for (int i = 0; i < size; i++) {

//             Side side = (i % 2 == 0) ? Side.BUY : Side.SELL;

//             String price;

//             if (side == Side.BUY) {
//                 price = "100";
//             } else {
//                 price = "200";
//             }

//             book.placeOrder(
//                     createOrder(
//                             i,
//                             side,
//                             price,
//                             100
//                     )
//             );
//         }

//         long elapsed = System.nanoTime() - start;

//         printResults("ADD", size, elapsed);
//     }

//     private static void benchmarkMatching(int size) {

//         long start = System.nanoTime();

//         for (int i = 0; i < size; i++) {

//             OrderBook book = new OrderBook();

//             book.placeOrder(
//                     createOrder(
//                             1,
//                             Side.SELL,
//                             "100",
//                             100
//                     )
//             );

//             book.placeOrder(
//                     createOrder(
//                             2,
//                             Side.BUY,
//                             "101",
//                             100
//                     )
//             );
//         }

//         long elapsed = System.nanoTime() - start;

//         printResults("MATCH", size, elapsed);
//     }

//     private static void benchmarkCancellation(int size) {

//         OrderBook book = new OrderBook();

//         for (int i = 0; i < size; i++) {

//             book.placeOrder(
//                     createOrder(
//                             i,
//                             Side.BUY,
//                             "100",
//                             100
//                     )
//             );
//         }

//         long start = System.nanoTime();

//         for (int i = 0; i < size; i++) {
//             book.removeOrder((long) i);
//         }

//         long elapsed = System.nanoTime() - start;

//         printResults("CANCEL", size, elapsed);
//     }

//     private static void printResults(
//             String operation,
//             int size,
//             long elapsedNanos
//     ) {

//         double seconds = elapsedNanos / 1_000_000_000.0;

//         double operationsPerSecond = size / seconds;

//         double nanosecondsPerOperation =
//                 (double) elapsedNanos / size;

//         System.out.printf(
//                 "%-8s | %.4f sec | %.0f ops/sec | %.2f ns/op%n",
//                 operation,
//                 seconds,
//                 operationsPerSecond,
//                 nanosecondsPerOperation
//         );
//     }
// }