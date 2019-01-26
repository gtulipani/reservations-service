package com.reservations.exception.extension;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ExtensionNotFoundException extends RuntimeException {
	@Getter
	private final String message;
}
