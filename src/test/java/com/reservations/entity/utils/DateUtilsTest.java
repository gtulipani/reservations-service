package com.reservations.entity.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.testng.annotations.Test;

public class DateUtilsTest {
	@Test
	public void testToLocalDate_fromNull() {
		Date date = null;

		assertThat(DateUtils.toLocalDate(date)).isNull();
	}

	@Test
	public void testToDate_fromNull() {
		LocalDate localDate = null;

		assertThat(DateUtils.toDate(localDate)).isNull();
	}

	@Test
	public void testToLocalDateTime_fromNull() {
		Timestamp timestamp = null;

		assertThat(DateUtils.toLocalDateTime(timestamp)).isNull();
	}

	@Test
	public void testToTimestamp_fromNull() {
		LocalDateTime localDateTime = null;

		assertThat(DateUtils.toTimestamp(localDateTime)).isNull();
	}

	@Test
	public void testDaysBetweenTodayAndTomorrow_doesntIncludeTomorrow() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);

		assertThat(DateUtils.daysBetween(today, tomorrow)).containsOnlyOnce(today);
	}

	@Test
	public void testDaysBetweenTodayAndToday_includesToday() {
		LocalDate today = LocalDate.now();

		assertThat(DateUtils.daysBetween(today, today)).containsOnlyOnce(today);
	}

	@Test
	public void testDaysBetweenTodayAndYesterday_isEmpty() {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);

		assertThat(DateUtils.daysBetween(today, yesterday)).isEmpty();
	}
}
