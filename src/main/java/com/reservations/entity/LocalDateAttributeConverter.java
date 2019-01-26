package com.reservations.entity;

import static com.reservations.entity.utils.DateUtils.toDate;
import static com.reservations.entity.utils.DateUtils.toLocalDate;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, Date> {
	@Override
	public Date convertToDatabaseColumn(LocalDate localDate) {
		return toDate(localDate);
	}

	@Override
	public LocalDate convertToEntityAttribute(Date date) {
		return toLocalDate(date);
	}
}
