package com.reservations.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LocalDateAttributeConverterTest {
	private LocalDateAttributeConverter localDateAttributeConverter;

	@BeforeMethod
	public void setup() {
		localDateAttributeConverter = new LocalDateAttributeConverter();
	}

	@Test
	public void testConvertToDatabaseColumn_notNull() {
		LocalDate localDate = LocalDate.now();
		Date date = localDateAttributeConverter.convertToDatabaseColumn(localDate);

		assertThat(date).hasYear(localDate.getYear());
		assertThat(date).hasMonth(localDate.getMonthValue());
		assertThat(date).hasDayOfMonth(localDate.getDayOfMonth());
	}

	@Test
	public void testConvertToDatabaseColumn_null() {
		assertThat(localDateAttributeConverter.convertToDatabaseColumn(null)).isNull();
	}

	@Test
	public void testConvertToEntityAttribute_notNull() {
		LocalDate localDate = localDateAttributeConverter.convertToEntityAttribute(new Date());

		assertThat(localDate).isToday();
	}

	@Test
	public void testConvertToEntityAttribute_null() {
		assertThat(localDateAttributeConverter.convertToEntityAttribute(null)).isNull();
	}
}
