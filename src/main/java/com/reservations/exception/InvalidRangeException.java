package com.reservations.exception;

import java.time.LocalDate;

import lombok.Getter;

import org.springframework.http.HttpStatus;

import com.reservations.entity.DateRange;

@Getter
public class InvalidRangeException extends ReservationServiceException {
	public InvalidRangeException(DateRange dateRange) {
		super(HttpStatus.BAD_REQUEST, String.format("Invalid range: endDate=%s must be greater or equal than startDate=%s and the range can't include past dates.",
				dateRange.getEnd(),
				dateRange.getStart()));
	}
}
