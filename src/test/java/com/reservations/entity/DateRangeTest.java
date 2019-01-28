package com.reservations.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.testng.annotations.Test;

public class DateRangeTest {
	@Test
	public void testIsValid_startBeforeEnd_true() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().plusDays(1))
				.build();

		assertThat(dateRange.isValid()).isTrue();
	}

	@Test
	public void testIsValid_startEqualsEnd_true() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now())
				.build();

		assertThat(dateRange.isValid()).isTrue();
	}

	@Test
	public void testIsValid_startAfterEnd_false() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().minusDays(1))
				.build();

		assertThat(dateRange.isValid()).isFalse();
	}

	@Test
	public void testIsPast_startsBeforeToday_true() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now().minusDays(1))
				.end(LocalDate.now())
				.build();

		assertThat(dateRange.isPast()).isTrue();
	}

	@Test
	public void testIsPast_startsToday_false() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().plusDays(1))
				.build();

		assertThat(dateRange.isPast()).isFalse();
	}

	@Test
	public void testIsPast_startsTomorrow_false() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now().plusDays(1))
				.end(LocalDate.now().plusDays(2))
				.build();

		assertThat(dateRange.isPast()).isFalse();
	}
}
