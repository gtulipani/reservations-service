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
import com.reservations.entity.DateRange;

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
	 * Returns a {@link List} with all the dates within a range. It's exclusive, meaning that the end is not included.
	 * The end is only included when start and end are the same day.
	 */
	public static List<LocalDate> daysBetween(LocalDate start, LocalDate end) {
		if (rangeInvalid(start, end)) {
			return Lists.newArrayList();
		}
		// ChronoUnit.DAYS.between() is exclusive.
		return start.isEqual(end) ?
				daysBetweenInclusive(start, end) :
				Stream.iterate(start, date -> date.plusDays(1))
						.limit(ChronoUnit.DAYS.between(start, end))
						.collect(Collectors.toList());
	}

	/**
	 * Returns a {@link List} with all the dates within a range. It's exclusive, meaning that the end is not included.
	 * The end is only included when start and end are the same day.
	 */
	public static List<LocalDate> daysBetween(DateRange dateRange) {
		return daysBetween(dateRange.getStart(), dateRange.getEnd());
	}

	/**
	 * Returns a {@link List} with all the dates within a range. It's incluse, meaning that the end is also included.
	 */
	public static List<LocalDate> daysBetweenInclusive(LocalDate start, LocalDate end) {
		if (rangeInvalid(start, end)) {
			return Lists.newArrayList();
		}
		// ChronoUnit.DAYS.between() is exclusive. So we sum 1 to the limit
		return Stream.iterate(start, date -> date.plusDays(1))
				.limit(ChronoUnit.DAYS.between(start, end) + 1)
				.collect(Collectors.toList());
	}

	/**
	 * Returns a {@link List} with all the dates within a range. It's incluse, meaning that the end is also included.
	 */
	public static List<LocalDate> daysBetweenInclusive(DateRange dateRange) {
		return daysBetweenInclusive(dateRange.getStart(), dateRange.getEnd());
	}

	/**
	 * Checks if the range is valid
	 */
	private static boolean rangeInvalid(LocalDate start, LocalDate end) {
		return start.isAfter(end);
	}
}
