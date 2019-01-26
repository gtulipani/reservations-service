package com.reservations.extensibility.utils;

import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.reservations.exception.extension.ExtensionNotFoundException;
import com.reservations.extensibility.extension.Extension;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExtensionUtils {
	public static <T extends Extension, C> T get(Set<T> extensions, C context) {
		return findCustomExtension(extensions, context);
	}

	private static <T extends Extension, C> T findCustomExtension(Set<T> extensions, C context) {
		return extensions.stream()
				.filter(e -> !e.isDefault() && e.supports(context))
				.findFirst()
				.orElseGet(() -> findDefaultExtension(extensions, context));
	}

	private static <T extends Extension, C> T findDefaultExtension(Set<T> extensions, C context) {
		return extensions.stream()
				.filter(T::isDefault)
				.findFirst()
				.orElseThrow(() -> new ExtensionNotFoundException("No default extension candidate could be found for context: " + context));
	}
}
