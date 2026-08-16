// package com.nimeshkmr.model;

// import org.junit.jupiter.api.Test;

// import java.math.BigDecimal;

// import static org.junit.jupiter.api.Assertions.*;

// public class OrderQueueTest {

//     private Order createOrder(long id) {
//         Order order = new Order(
//                     1L,
//                     1L,
//                     Side.BUY,
//                     new BigDecimal("100"),
//                     50
//                 );
//         return order;
//     }

//     @Test
//     void emptyQueue() {
//         OrderQueue q = new OrderQueue();

//         assertTrue(q.isEmpty());
//         assertNull(q.getHead());
//         assertNull(q.getTail());
//     }

//     @Test
//     void appendFirstNode() {
//         OrderNode node = new OrderNode(createOrder(1));
//         OrderQueue q = new OrderQueue();

//         q.append(node);

//         assertFalse(q.isEmpty());
//         assertSame(node, q.getHead());
//         assertSame(node, q.getTail());
//         assertNull(node.prev);
//         assertNull(node.next);
//     }

//     @Test
//     void appendMultipleNodes() {
//         OrderNode a = new OrderNode(createOrder(1));
//         OrderNode b = new OrderNode(createOrder(2));
//         OrderNode c = new OrderNode(createOrder(3));

//         OrderQueue q = new OrderQueue();

//         q.append(a);
//         q.append(b);
//         q.append(c);

//         assertSame(a, q.getHead());
//         assertSame(c, q.getTail());

//         assertSame(b, a.next);
//         assertSame(a, b.prev);

//         assertSame(c, b.next);
//         assertSame(b, c.prev);

//         assertNull(a.prev);
//         assertNull(c.next);
//     }

//     @Test
//     void removeHead() {
//         OrderNode a = new OrderNode(createOrder(1));
//         OrderNode b = new OrderNode(createOrder(2));
//         OrderNode c = new OrderNode(createOrder(3));

//         OrderQueue q = new OrderQueue();

//         q.append(a);
//         q.append(b);
//         q.append(c);

//         q.cancel(a);

//         assertSame(b, q.getHead());
//         assertSame(c, q.getTail());

//         assertNull(b.prev);
//         assertSame(c, b.next);
//         assertSame(b, c.prev);
//     }

//     @Test
//     void removeMiddle() {
//         OrderNode a = new OrderNode(createOrder(1));
//         OrderNode b = new OrderNode(createOrder(2));
//         OrderNode c = new OrderNode(createOrder(3));

//         OrderQueue q = new OrderQueue();

//         q.append(a);
//         q.append(b);
//         q.append(c);

//         q.cancel(b);

//         assertSame(a, q.getHead());
//         assertSame(c, q.getTail());

//         assertSame(c, a.next);
//         assertSame(a, c.prev);
//     }

//     @Test
//     void removeTail() {
//         OrderNode a = new OrderNode(createOrder(1));
//         OrderNode b = new OrderNode(createOrder(2));
//         OrderNode c = new OrderNode(createOrder(3));

//         OrderQueue q = new OrderQueue();

//         q.append(a);
//         q.append(b);
//         q.append(c);

//         q.cancel(c);

//         assertSame(a, q.getHead());
//         assertSame(b, q.getTail());

//         assertSame(b, a.next);
//         assertSame(a, b.prev);
//         assertNull(b.next);
//     }

//     @Test
//     void removeOnlyNode() {
//         OrderNode a = new OrderNode(createOrder(1));

//         OrderQueue q = new OrderQueue();
//         q.append(a);

//         q.cancel(a);

//         assertTrue(q.isEmpty());
//         assertNull(q.getHead());
//         assertNull(q.getTail());
//     }

//     @Test
//     void removeEverything() {
//         OrderNode a = new OrderNode(createOrder(1));
//         OrderNode b = new OrderNode(createOrder(2));
//         OrderNode c = new OrderNode(createOrder(3));

//         OrderQueue q = new OrderQueue();

//         q.append(a);
//         q.append(b);
//         q.append(c);

//         q.cancel(a);
//         q.cancel(b);
//         q.cancel(c);

//         assertTrue(q.isEmpty());
//         assertNull(q.getHead());
//         assertNull(q.getTail());
//     }
// }