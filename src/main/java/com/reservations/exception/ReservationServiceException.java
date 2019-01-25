package com.reservations.exception;

import java.util.Collections;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public abstract class ReservationServiceException extends RuntimeException {
	private final HttpStatus responseStatus;
	private final String message;

	public Map<String, String> getResponseBody() {
		return Collections.singletonMap("error", message);
	}
}
