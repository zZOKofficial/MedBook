package com.oxyorb.medbook.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The on-disk shape of a demo booking.
 *
 * The store itself needs a Context and is not tested here, but its encoding does not,
 * and the encoding is the part that can quietly lose a person's bookings. A record that
 * cannot be read has to cost one booking rather than the whole book -- there is no
 * delete-and-regenerate for something a person did, the way there is for the dataset.
 */
public class DemoStoreCodecTest {

	private static DemoStore.Booking booking(long _doctorId, int _seq, int _epochDay,
			int _minuteOfDay) {
		return new DemoStore.Booking(_doctorId, _seq, _epochDay, _minuteOfDay, 1725580800000L);
	}

	@Test
	public void survivesARoundTrip() {
		List<DemoStore.Booking> _written = Arrays.asList(
			booking(4210L, 0, 20000, 600),
			booking(77L, 3, 20001, 1020));

		List<DemoStore.Booking> _read = DemoStore.decode(DemoStore.encode(_written));

		assertEquals(2, _read.size());
		assertEquals(4210L, _read.get(0).doctorId);
		assertEquals(0, _read.get(0).seq);
		assertEquals(20000, _read.get(0).epochDay);
		assertEquals(600, _read.get(0).minuteOfDay);
		assertEquals(1725580800000L, _read.get(0).createdAt);
		assertEquals(3, _read.get(1).seq);
		assertEquals(1020, _read.get(1).minuteOfDay);
	}

	@Test
	public void writesNothingForNoBookings() {
		assertEquals("", DemoStore.encode(new ArrayList<DemoStore.Booking>()));
		assertTrue(DemoStore.decode("").isEmpty());
		assertTrue(DemoStore.decode(null).isEmpty());
	}

	@Test
	public void keepsTheChamberKeyItWasBookedUnder() {
		assertEquals("4210:2", booking(4210L, 2, 20000, 600).chamberKey());
	}

	/** One unreadable line costs one booking, never the rest of them. */
	@Test
	public void skipsARecordItCannotRead() {
		String _table = DemoStore.encode(Arrays.asList(booking(1L, 0, 20000, 600)))
			+ "\n1|not-a-number|0|20000|620|0"
			+ "\n1|9|0|20000"
			+ "\n"
			+ "\n2|9|0|20000|640|0"
			+ "\n" + DemoStore.encode(Arrays.asList(booking(2L, 1, 20002, 660)));

		List<DemoStore.Booking> _read = DemoStore.decode(_table);

		assertEquals(2, _read.size());
		assertEquals(1L, _read.get(0).doctorId);
		assertEquals(2L, _read.get(1).doctorId);
	}

	/** A record from a format this build does not know is left alone, not guessed at. */
	@Test
	public void skipsAnUnknownRecordVersion() {
		assertTrue(DemoStore.decode("7|4210|0|20000|600|0").isEmpty());
	}
}
