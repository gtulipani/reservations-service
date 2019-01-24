package com.reservations.entity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, Date> {
	@Override
	public Date convertToDatabaseColumn(LocalDate localDate) {
		return localDate == null ? null : Date.from(localDate.atStartOfDay()
				.atZone(ZoneId.systemDefault())
				.toInstant());
	}

	@Override
	public LocalDate convertToEntityAttribute(Date date) {
		return date == null ? null : date.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}
}
