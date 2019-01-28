package com.reservations.entity.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;

import org.testng.annotations.Test;

import com.reservations.entity.DateRange;

public class DateUtilsTest {
	@Test
	public void testToLocalDate() {
		Date date = new Date();

		assertThat(DateUtils.toLocalDate(date)).isToday();
	}

	@Test
	public void testToLocalDate_fromNull() {
		Date date = null;

		assertThat(DateUtils.toLocalDate(date)).isNull();
	}

	@Test
	public void testToDate() {
		LocalDate localDate = LocalDate.now();

		Date date = DateUtils.toDate(localDate);

		assertThat(date).hasYear(localDate.getYear());
		assertThat(date).hasMonth(localDate.getMonthValue());
		assertThat(date).hasDayOfMonth(localDate.getDayOfMonth());
	}

	@Test
	public void testToDate_fromNull() {
		LocalDate localDate = null;

		assertThat(DateUtils.toDate(localDate)).isNull();
	}

	@Test
	public void testToLocalDateTime() {
		Timestamp timestamp = Timestamp.from(Instant.now());

		LocalDateTime localDateTime = DateUtils.toLocalDateTime(timestamp);

		assertThat(localDateTime).isEqualTo(timestamp.toLocalDateTime());
	}

	@Test
	public void testToLocalDateTime_fromNull() {
		Timestamp timestamp = null;

		assertThat(DateUtils.toLocalDateTime(timestamp)).isNull();
	}

	@Test
	public void testToTimestamp() {
		LocalDateTime localDateTime = LocalDateTime.now();

		Timestamp timestamp = DateUtils.toTimestamp(localDateTime);

		assertThat(timestamp).hasYear(localDateTime.getYear());
		assertThat(timestamp).hasMonth(localDateTime.getMonthValue());
		assertThat(timestamp).hasDayOfMonth(localDateTime.getDayOfMonth());
		assertThat(timestamp).hasHourOfDay(localDateTime.getHour());
		assertThat(timestamp).hasMinute(localDateTime.getMinute());
		assertThat(timestamp).hasSecond(localDateTime.getSecond());
		assertThat(timestamp).hasMillisecond(localDateTime.get(ChronoField.MILLI_OF_SECOND));
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

	@Test
	public void testDaysBetweenRangeWithTodayAndTomorrow_doesntIncludeTomorrow() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		DateRange dateRange = DateRange.builder()
				.start(today)
				.end(tomorrow)
				.build();

		assertThat(DateUtils.daysBetween(dateRange)).containsOnlyOnce(today);
	}

	@Test
	public void testDaysBetweenRangeWithTodayAndToday_includesToday() {
		LocalDate today = LocalDate.now();
		DateRange dateRange = DateRange.builder()
				.start(today)
				.end(today)
				.build();

		assertThat(DateUtils.daysBetween(dateRange)).containsOnlyOnce(today);
	}

	@Test
	public void testDaysBetweenRangeWithTodayAndYesterday_isEmpty() {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		DateRange dateRange = DateRange.builder()
				.start(today)
				.end(yesterday)
				.build();

		assertThat(DateUtils.daysBetween(dateRange)).isEmpty();
	}

	@Test
	public void testDaysBetweenInclusiveTodayAndTomorrow_includesTomorrow() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);

		assertThat(DateUtils.daysBetweenInclusive(today, tomorrow)).containsExactlyInAnyOrder(today, tomorrow);
	}

	@Test
	public void testDaysBetweenInclusiveTodayAndToday_includesToday() {
		LocalDate today = LocalDate.now();

		assertThat(DateUtils.daysBetweenInclusive(today, today)).containsOnlyOnce(today);
	}

	@Test
	public void testDaysBetweenInclusiveTodayAndYesterday_isEmpty() {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);

		assertThat(DateUtils.daysBetweenInclusive(today, yesterday)).isEmpty();
	}

	@Test
	public void testDaysBetweenInclusiveRangeWithTodayAndTomorrow_includesTomorrow() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		DateRange dateRange = DateRange.builder()
				.start(today)
				.end(tomorrow)
				.build();

		assertThat(DateUtils.daysBetweenInclusive(dateRange)).containsExactlyInAnyOrder(today, tomorrow);
	}

	@Test
	public void testDaysBetweenInclusiveRangeWithTodayAndToday_includesToday() {
		LocalDate today = LocalDate.now();
		DateRange dateRange = DateRange.builder()
				.start(today)
				.end(today)
				.build();

		assertThat(DateUtils.daysBetweenInclusive(dateRange)).containsOnlyOnce(today);
	}

	@Test
	public void testDaysBetweenInclusiveRangeWithTodayAndYesterday_isEmpty() {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		DateRange dateRange = DateRange.builder()
				.start(today)
				.end(yesterday)
				.build();

		assertThat(DateUtils.daysBetweenInclusive(dateRange)).isEmpty();
	}
}
