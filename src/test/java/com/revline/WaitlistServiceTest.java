package com.revline;

import com.revline.service.WaitlistService;

import java.util.List;

/**
 * Tests for FIFO customer waitlist queues (L6-7 Queue).
 */
public class WaitlistServiceTest {

    public static void testJoinWaitlistPosition() {
        WaitlistService ws = new WaitlistService();

        int pos1 = ws.join(10, "Buyer A");
        Assert.assertEquals(1, pos1, "First buyer is position #1");

        int pos2 = ws.join(10, "Buyer B");
        Assert.assertEquals(2, pos2, "Second buyer is position #2");

        // Re-joining with same buyer returns existing position without duplicating
        int pos1Rejoin = ws.join(10, "Buyer A");
        Assert.assertEquals(1, pos1Rejoin, "Buyer A position remains #1");

        Assert.assertEquals(2, ws.getQueueSize(10), "Queue size remains 2");
    }

    public static void testPollOrderFifo() {
        WaitlistService ws = new WaitlistService();
        ws.join(5, "First");
        ws.join(5, "Second");
        ws.join(5, "Third");

        Assert.assertEquals("First", ws.poll(5), "First buyer in line is polled first (FIFO)");
        Assert.assertEquals("Second", ws.poll(5), "Second buyer polled next");
        Assert.assertEquals("Third", ws.poll(5), "Third buyer polled next");
        Assert.assertNull(ws.poll(5), "Queue is now empty, poll returns null");
    }

    public static void testRemoveAndPosition() {
        WaitlistService ws = new WaitlistService();
        ws.join(7, "Alpha");
        ws.join(7, "Beta");
        ws.join(7, "Gamma");

        Assert.assertEquals(2, ws.getPosition(7, "Beta"), "Beta is in position #2");
        Assert.assertEquals(-1, ws.getPosition(7, "Delta"), "Delta is not in waitlist");

        boolean removed = ws.remove(7, "Beta");
        Assert.assertTrue(removed, "Beta was removed");
        Assert.assertEquals(2, ws.getQueueSize(7), "Queue size is now 2");
        Assert.assertEquals(-1, ws.getPosition(7, "Beta"), "Beta is no longer in waitlist");

        List<String> list = ws.getWaitlist(7);
        Assert.assertEquals(2, list.size(), "List has 2 entries");
        Assert.assertEquals("Alpha", list.get(0), "First is Alpha");
        Assert.assertEquals("Gamma", list.get(1), "Second is Gamma");
    }
}
