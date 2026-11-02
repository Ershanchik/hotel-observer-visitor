package hotel;
import static hotel.Event.Type.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import org.junit.jupiter.api.Test;

class ObserverTests {
    private static Event booking(double amount) { return new Event(BOOKING, "101", 1, amount, ""); }

    @Test void listenerReceivesOnlySubscribedType() {
        EventSubject subject = new EventSubject();
        List<Event> got = new CopyOnWriteArrayList<>();
        subject.subscribe(BOOKING, got::add);
        subject.publish(booking(10));
        subject.publish(new Event(ROOM_DIRTY, "101", 1, 0, ""));
        assertEquals(1, got.size());
        assertEquals(BOOKING, got.get(0).type());
    }

    @Test void allListenersOfATypeAreNotified() {
        EventSubject subject = new EventSubject();
        LongAdder a = new LongAdder(), b = new LongAdder();
        subject.subscribe(BOOKING, e -> a.increment());
        subject.subscribe(BOOKING, e -> b.increment());
        subject.publish(booking(10));
        assertEquals(1, a.sum());
        assertEquals(1, b.sum());
    }

    @Test void unsubscribedListenerStopsReceiving() {
        EventSubject subject = new EventSubject();
        LongAdder seen = new LongAdder();
        Observer<Event> listener = e -> seen.increment();
        subject.subscribe(BOOKING, listener);
        subject.publish(booking(10));
        subject.unsubscribe(BOOKING, listener);
        subject.publish(booking(10));
        assertEquals(1, seen.sum());
    }

    @Test void listenerCanUnsubscribeInsideItsOwnHandler() {
        EventSubject subject = new EventSubject();
        AtomicInteger oneShotCalls = new AtomicInteger();
        LongAdder other = new LongAdder();
        class OneShot implements Observer<Event> {
            @Override public void update(Event e) { oneShotCalls.incrementAndGet(); subject.unsubscribe(BOOKING, this); }
        }
        subject.subscribe(BOOKING, new OneShot());
        subject.subscribe(BOOKING, e -> other.increment());
        subject.publish(booking(10));
        subject.publish(booking(10));
        assertEquals(1, oneShotCalls.get());
        assertEquals(2, other.sum());
    }

    @Test void listenerCanSubscribeAnotherInsideHandlerAndItJoinsFromNextEvent() {
        EventSubject subject = new EventSubject();
        LongAdder late = new LongAdder();
        AtomicInteger added = new AtomicInteger();
        subject.subscribe(BOOKING, e -> { if (added.getAndIncrement() == 0) subject.subscribe(BOOKING, x -> late.increment()); });
        subject.publish(booking(10));
        assertEquals(0, late.sum());
        subject.publish(booking(10));
        assertEquals(1, late.sum());
    }

    @Test void revenueManagerRepublishesRunningTotal() {
        RevenueManager manager = new RevenueManager();
        List<Event> summaries = new ArrayList<>();
        manager.subscribe(REVENUE_SUMMARY, summaries::add);
        manager.update(booking(100));
        manager.update(booking(50));
        assertEquals(2, summaries.size());
        assertEquals(100.0, summaries.get(0).amount());
        assertEquals(150.0, summaries.get(1).amount());
        assertEquals("2 bookings", summaries.get(1).detail());
        assertEquals(150.0, manager.total());
    }

    @Test void frontDeskCountsBookingsAndTracksReadyRooms() {
        FrontDesk desk = new FrontDesk();
        desk.update(booking(10));
        desk.update(new Event(ROOM_CLEANED, "101", 1, 0, ""));
        assertEquals(1, desk.bookings());
        assertTrue(desk.isReady("101"));
        desk.update(new Event(ROOM_DIRTY, "101", 1, 0, ""));
        assertFalse(desk.isReady("101"));
    }

    @Test void auditLogCountsEachTypeSeparately() {
        AuditLog audit = new AuditLog();
        audit.update(booking(10));
        audit.update(booking(10));
        audit.update(new Event(ROOM_DIRTY, "101", 1, 0, ""));
        assertEquals(2, audit.count(BOOKING));
        assertEquals(1, audit.count(ROOM_DIRTY));
        assertEquals(0, audit.count(CLEANING_PLAN));
    }

    @Test void stressFourThreadsTimesThousandEventsAreAllDelivered() throws Exception {
        EventSubject subject = new EventSubject();
        LongAdder seen = new LongAdder();
        subject.subscribe(BOOKING, e -> seen.increment());
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < 4; t++) futures.add(pool.submit(() -> { for (int i = 0; i < 1000; i++) subject.publish(booking(1)); }));
        for (Future<?> f : futures) f.get();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(4000, seen.sum());
    }

    @Test void noEventIsLostWhileListenersJoinAndLeaveConcurrently() throws Exception {
        EventSubject subject = new EventSubject();
        LongAdder stable = new LongAdder();
        subject.subscribe(BOOKING, e -> stable.increment());
        ExecutorService pool = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < 4; t++) futures.add(pool.submit(() -> { for (int i = 0; i < 1000; i++) subject.publish(booking(1)); }));
        futures.add(pool.submit(() -> {
            for (int i = 0; i < 1000; i++) { Observer<Event> temp = e -> { }; subject.subscribe(BOOKING, temp); subject.unsubscribe(BOOKING, temp); }
        }));
        for (Future<?> f : futures) f.get();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(4000, stable.sum());
    }

    @Test void fullPipelineKeepsEveryEventWithFourProducerThreads() throws Exception {
        AuditLog audit = Main.run(4, 1000);
        assertEquals(2000, audit.count(BOOKING));
        assertEquals(1000, audit.count(ROOM_DIRTY));
        assertEquals(1000, audit.count(ROOM_CLEANED));
        assertEquals(2000, audit.count(CLEANING_PLAN));
        assertEquals(2000, audit.count(REVENUE_SUMMARY));
    }
}
