package com.reservations.entity.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.google.common.collect.Lists;

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

	/**
	 * Returns a {@link List} with all the dates within a range
	 */
	public static List<LocalDate> daysBetween(LocalDate start, LocalDate end) {
		if (start.isAfter(end)) {
			return Lists.newArrayList();
		}
		// ChronoUnit.DAYS.between() is exclusive. If start == end we want to return one date
		return Stream.iterate(start, date -> date.plusDays(1))
				.limit(start.isEqual(end) ? 1 : ChronoUnit.DAYS.between(start, end))
				.collect(Collectors.toList());
	}
}
