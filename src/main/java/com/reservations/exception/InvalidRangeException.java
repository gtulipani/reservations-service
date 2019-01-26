package com.reservations.exception;

import java.time.LocalDate;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class InvalidRangeException extends ReservationServiceException {
	public InvalidRangeException(LocalDate start, LocalDate end) {
		super(HttpStatus.BAD_REQUEST, String.format("Invalid range: endDate=%s must be greater or equal than startDate=%s",
				start,
				end));
	}
}
