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
}
