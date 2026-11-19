package com.revline;

import com.revline.model.Reservation;
import com.revline.service.ReservationManager;
import com.revline.util.exception.CarNotAvailableException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tests for PriorityQueue reservation management and Iterator.remove() expiration (L6-7).
 */
public class ReservationManagerTest {

    public static void testReserveCar() throws CarNotAvailableException {
        ReservationManager mgr = new ReservationManager();
        Assert.assertEquals(0, mgr.count(), "Initial reservations empty");

        Reservation res = mgr.reserve(5, "Bruce Wayne", 60);
        Assert.assertNotNull(res, "Reservation created");
        Assert.assertEquals(5, res.getCarId(), "Car ID matches");
        Assert.assertEquals("Bruce Wayne", res.getBuyerName(), "Buyer matches");
        Assert.assertTrue(mgr.isReserved(5), "Car #5 is marked reserved");
        Assert.assertFalse(mgr.isReserved(6), "Car #6 is not reserved");
    }

    public static void testCannotDoubleReserve() throws CarNotAvailableException {
        ReservationManager mgr = new ReservationManager();
        mgr.reserve(10, "First Buyer", 30);

        boolean caught = false;
        try {
            mgr.reserve(10, "Second Buyer", 30);
        } catch (CarNotAvailableException e) {
            caught = true;
        }
        Assert.assertTrue(caught, "Reserving an already reserved car should throw CarNotAvailableException");
    }

    public static void testCancelReservation() throws CarNotAvailableException {
        ReservationManager mgr = new ReservationManager();
        mgr.reserve(12, "Clark Kent", 45);

        boolean caughtWrong = false;
        try {
            mgr.cancel(12, "Lex Luthor");
        } catch (CarNotAvailableException e) {
            caughtWrong = true;
        }
        Assert.assertTrue(caughtWrong, "Cancelling with wrong buyer name must fail");

        Reservation cancelled = mgr.cancel(12, "Clark Kent");
        Assert.assertNotNull(cancelled, "Cancelled reservation returned");
        Assert.assertFalse(mgr.isReserved(12), "Car #12 is no longer reserved");
    }

    public static void testPurgeExpiredUsesIteratorRemove() {
        ReservationManager mgr = new ReservationManager();

        Reservation expiredRes = new Reservation(20, "Late Buyer", LocalDateTime.now().minusMinutes(10));
        Reservation activeRes = new Reservation(21, "Active Buyer", LocalDateTime.now().plusMinutes(30));

        mgr.addReservationDirectly(expiredRes);
        mgr.addReservationDirectly(activeRes);

        List<Integer> purged = mgr.purgeExpired();
        Assert.assertEquals(1, purged.size(), "1 reservation should be purged");
        Assert.assertEquals(20, purged.get(0).intValue(), "Car #20 should be in purged list");

        Assert.assertFalse(mgr.isReserved(20), "Car #20 is no longer reserved");
        Assert.assertTrue(mgr.isReserved(21), "Car #21 remains actively reserved");
    }
}
