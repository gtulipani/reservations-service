package com.reservations.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LocalDateTimeAttributeConverterTest {
	private LocalDateTimeAttributeConverter localDateTimeAttributeConverter;

	@BeforeMethod
	public void setup() {
		localDateTimeAttributeConverter = new LocalDateTimeAttributeConverter();
	}

	@Test
	public void testConvertToDatabaseColumn_notNull() {
		LocalDateTime localDateTime = LocalDateTime.now();
		Timestamp timestamp = localDateTimeAttributeConverter.convertToDatabaseColumn(localDateTime);

		assertThat(timestamp).hasYear(localDateTime.getYear());
		assertThat(timestamp).hasMonth(localDateTime.getMonthValue());
		assertThat(timestamp).hasDayOfMonth(localDateTime.getDayOfMonth());
		assertThat(timestamp).hasHourOfDay(localDateTime.getHour());
		assertThat(timestamp).hasMinute(localDateTime.getMinute());
		assertThat(timestamp).hasSecond(localDateTime.getSecond());
		assertThat(timestamp).hasMillisecond(localDateTime.get(ChronoField.MILLI_OF_SECOND));
	}

	@Test
	public void testConvertToDatabaseColumn_null() {
		assertThat(localDateTimeAttributeConverter.convertToDatabaseColumn(null)).isNull();
	}

	@Test
	public void testConvertToEntityAttribute_notNull() {
		Timestamp timestamp = Timestamp.from(Instant.now());
		LocalDateTime localDateTime = localDateTimeAttributeConverter.convertToEntityAttribute(timestamp);

		assertThat(localDateTime).isEqualTo(timestamp.toLocalDateTime());
	}

	@Test
	public void testConvertToEntityAttribute_null() {
		assertThat(localDateTimeAttributeConverter.convertToEntityAttribute(null)).isNull();
	}
}
