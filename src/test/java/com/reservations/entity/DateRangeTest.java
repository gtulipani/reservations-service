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
}
