package hotel;
import static hotel.Event.Type.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VisitorTests {
    private final Floor floor1 = new Floor(1, List.of(new Room("101", 100, true), new Room("102", 100, false), new Suite("103", 250, 2, true)));
    private final Floor floor2 = new Floor(2, List.of(new Hall("H1", 50, 4), new Spa("S1", 30, 3), new Room("201", 120, true)));
    private final Hotel hotel = new Hotel("Plaza", List.of(floor1, floor2));

    @Test void revenueOfWholeHotelMatchesHandCalculation() { assertEquals(760.0, hotel.accept(new RevenueVisitor())); }  // 350 + 410

    @Test void revenueOfOneFloorOnlyCoversThatSubtree() {
        assertEquals(350.0, floor1.accept(new RevenueVisitor()));
        assertEquals(410.0, floor2.accept(new RevenueVisitor()));
    }

    @Test void doubleDispatchReachesTheMatchingOverload() {
        assertEquals(250.0, new Suite("103", 250, 2, true).accept(new RevenueVisitor()));
        assertEquals(200.0, new Hall("H1", 50, 4).accept(new RevenueVisitor()));
        assertEquals(0.0, new Room("102", 100, false).accept(new RevenueVisitor()));
    }

    @Test void emptyHotelHasZeroRevenue() { assertEquals(0.0, new Hotel("Empty", List.of()).accept(new RevenueVisitor())); }

    @Test void occupancyAccumulatesCountsAcrossTheTree() {
        OccupancyVisitor v = new OccupancyVisitor();
        hotel.accept(v);
        assertEquals(4, v.total());
        assertEquals(3, v.occupied());
        assertEquals(0.75, v.rate());
    }

    @Test void occupancyOfHotelWithoutRoomsIsZero() {
        OccupancyVisitor v = new OccupancyVisitor();
        new Hotel("Empty", List.of()).accept(v);
        assertEquals(0.0, v.rate());
    }

    @Test void cleaningPlanReturnsOnlyDirtyRoomsInTreeOrder() {
        assertEquals(List.of("102", "201"), hotel.accept(new CleaningPlanVisitor(Set.of("201", "102"))));
        assertEquals(List.of("102"), floor1.accept(new CleaningPlanVisitor(Set.of("201", "102"))));
    }

    @Test void cleaningPlanIsEmptyWhenNothingIsDirty() { assertEquals(List.of(), hotel.accept(new CleaningPlanVisitor(Set.of()))); }

    @Test void jsonExportNestsFloorsAndUnitsCorrectly() {
        Hotel small = new Hotel("Plaza", List.of(new Floor(1, List.of(new Room("101", 100, true), new Hall("H1", 50, 4)))));
        assertEquals("{\"hotel\":\"Plaza\",\"floors\":[{\"floor\":1,\"units\":["
            + "{\"type\":\"room\",\"id\":\"101\",\"nightlyRate\":100.0,\"occupied\":true},"
            + "{\"type\":\"hall\",\"id\":\"H1\",\"hourlyRate\":50.0,\"bookedHours\":4}]}]}", small.accept(new JsonExportVisitor()));
    }

    @Test void jsonExportEscapesQuotesInNames() {
        assertEquals("{\"hotel\":\"The \\\"Best\\\"\",\"floors\":[]}", new Hotel("The \"Best\"", List.of()).accept(new JsonExportVisitor()));
    }

    @Test void dirtyRoomEventTriggersVisitorOnThatFloorOnlyAndPublishesPlan() {
        Housekeeping housekeeping = new Housekeeping(hotel);
        List<Event> plans = new ArrayList<>();
        housekeeping.subscribe(CLEANING_PLAN, plans::add);
        housekeeping.update(new Event(ROOM_DIRTY, "102", 1, 0, ""));
        housekeeping.update(new Event(ROOM_DIRTY, "201", 2, 0, ""));
        assertEquals("102", plans.get(0).detail());
        assertEquals("201", plans.get(1).detail());   // floor 2 plan does not contain floor 1's room 102
        assertEquals(1.0, plans.get(1).amount());
        assertEquals(2, plans.get(1).floor());
    }

    @Test void cleanedRoomLeavesThePlanEmpty() {
        Housekeeping housekeeping = new Housekeeping(hotel);
        List<Event> plans = new ArrayList<>();
        housekeeping.subscribe(CLEANING_PLAN, plans::add);
        housekeeping.update(new Event(ROOM_DIRTY, "102", 1, 0, ""));
        housekeeping.update(new Event(ROOM_CLEANED, "102", 1, 0, ""));
        assertEquals("", plans.get(1).detail());
        assertEquals(0.0, plans.get(1).amount());
    }

    @Test void statusEventForUnknownFloorPublishesNothing() {
        Housekeeping housekeeping = new Housekeeping(hotel);
        List<Event> plans = new ArrayList<>();
        housekeeping.subscribe(CLEANING_PLAN, plans::add);
        housekeeping.update(new Event(ROOM_DIRTY, "999", 9, 0, ""));
        assertTrue(plans.isEmpty());
    }

    @Test void eventsAreImmutableValuesWithAType() {
        assertEquals(new Event(BOOKING, "101", 1, 5, ""), new Event(BOOKING, "101", 1, 5, ""));
        assertThrows(NullPointerException.class, () -> new Event(null, "101", 1, 5, ""));
    }
}
