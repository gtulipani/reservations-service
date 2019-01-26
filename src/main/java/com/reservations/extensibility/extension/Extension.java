package com.reservations.extensibility.extension;

public interface Extension<C> {
	boolean isDefault();

	boolean supports(C context);
}
