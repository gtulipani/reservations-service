package com.reservations.entity.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {
	public static LocalDate toLocalDate(Date date) {
		return date == null ? null : date.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}

	public static Date toDate(LocalDate localDate) {
		return localDate == null ? null : Date.from(localDate.atStartOfDay()
				.atZone(ZoneId.systemDefault())
				.toInstant());
	}

	public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}

	public static Timestamp toTimestamp(LocalDateTime localDateTime) {
		return localDateTime == null ? null : Timestamp.valueOf(localDateTime);
	}
}
