package com.reservations.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.reservations.entity.EventType;

public class DefaultReservationValidatorExtensionImplTest {
	private DefaultReservationValidatorExtensionImpl defaultReservationValidatorExtension;

	@BeforeMethod
	public void setup() {
		defaultReservationValidatorExtension = new DefaultReservationValidatorExtensionImpl();
	}

	@Test
	public void testIsDefault_true() {
		assertThat(defaultReservationValidatorExtension.isDefault()).isTrue();
	}

	@Test
	public void testSupportsEventTypeCreation_true() {
		assertThat(defaultReservationValidatorExtension.supports(EventType.CREATION)).isTrue();
	}

	@Test
	public void testSupportsEventTypeUpdate_true() {
		assertThat(defaultReservationValidatorExtension.supports(EventType.UPDATE)).isTrue();
	}

	@Test
	public void testSupportsEventTypeCancellation_true() {
		assertThat(defaultReservationValidatorExtension.supports(EventType.CANCELLATION)).isTrue();
	}
}
