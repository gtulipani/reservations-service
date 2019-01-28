package com.reservations.entity;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DateRange {
	private LocalDate start;
	private LocalDate end;

	public boolean isValid() {
		return start.compareTo(end) <= 0;
	}

	public boolean isPast() {
		return start.isBefore(LocalDate.now());
	}
}
