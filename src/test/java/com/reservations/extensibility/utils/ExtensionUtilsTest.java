package com.reservations.extensibility.utils;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Set;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.reservations.entity.EventType;
import com.reservations.exception.extension.ExtensionNotFoundException;
import com.reservations.extensibility.extension.Extension;
import com.reservations.validation.DefaultReservationValidatorExtensionImpl;
import com.reservations.validation.ReservationCancellationValidatorExtensionImpl;
import com.reservations.validation.ReservationCreationValidatorExtensionImpl;
import com.reservations.validation.ReservationUpdateValidatorExtensionImpl;
import com.reservations.validation.ReservationValidatorExtension;

public class ExtensionUtilsTest {
	@Mock
	private DefaultReservationValidatorExtensionImpl defaultReservationValidatorExtension;
	@Mock
	private ReservationCreationValidatorExtensionImpl reservationCreationValidatorExtension;
	@Mock
	private ReservationUpdateValidatorExtensionImpl reservationUpdateValidatorExtension;
	@Mock
	private ReservationCancellationValidatorExtensionImpl reservationCancellationValidatorExtension;

	private Set<ReservationValidatorExtension> reservationValidatorExtensions;

	@BeforeMethod
	public void setup() {
		initMocks(this);

		reservationValidatorExtensions = Sets.newHashSet(
				defaultReservationValidatorExtension,
				reservationCreationValidatorExtension,
				reservationUpdateValidatorExtension,
				reservationCancellationValidatorExtension);

		when(defaultReservationValidatorExtension.isDefault()).thenReturn(true);
		when(reservationCreationValidatorExtension.isDefault()).thenReturn(false);
		when(reservationUpdateValidatorExtension.isDefault()).thenReturn(false);
		when(reservationCancellationValidatorExtension.isDefault()).thenReturn(false);
	}

	@Test
	public void testGetReservationValidatorExtension_getDefault() {
		when(defaultReservationValidatorExtension.supports(EventType.CREATION)).thenReturn(false);
		when(reservationCreationValidatorExtension.supports(EventType.CREATION)).thenReturn(false);
		when(reservationUpdateValidatorExtension.supports(EventType.CREATION)).thenReturn(false);
		when(reservationCancellationValidatorExtension.supports(EventType.CREATION)).thenReturn(false);

		Extension extension = ExtensionUtils.get(reservationValidatorExtensions, EventType.CREATION);

		assertThat(extension).isEqualTo(defaultReservationValidatorExtension);
	}

	@Test
	public void testGetReservationValidatorExtension_getCreation() {
		when(defaultReservationValidatorExtension.supports(EventType.CREATION)).thenReturn(false);
		when(reservationCreationValidatorExtension.supports(EventType.CREATION)).thenReturn(true);
		when(reservationUpdateValidatorExtension.supports(EventType.CREATION)).thenReturn(false);
		when(reservationCancellationValidatorExtension.supports(EventType.CREATION)).thenReturn(false);

		Extension extension = ExtensionUtils.get(reservationValidatorExtensions, EventType.CREATION);

		assertThat(extension).isEqualTo(reservationCreationValidatorExtension);
	}

	@Test
	public void testGetReservationValidatorExtension_getUpdate() {
		when(defaultReservationValidatorExtension.supports(EventType.UPDATE)).thenReturn(false);
		when(reservationCreationValidatorExtension.supports(EventType.UPDATE)).thenReturn(false);
		when(reservationUpdateValidatorExtension.supports(EventType.UPDATE)).thenReturn(true);
		when(reservationCancellationValidatorExtension.supports(EventType.CREATION)).thenReturn(false);

		Extension extension = ExtensionUtils.get(reservationValidatorExtensions, EventType.UPDATE);

		assertThat(extension).isEqualTo(reservationUpdateValidatorExtension);
	}

	@Test
	public void testGetReservationValidatorExtension_getCancellation() {
		when(defaultReservationValidatorExtension.supports(EventType.CANCELLATION)).thenReturn(false);
		when(reservationCreationValidatorExtension.supports(EventType.CANCELLATION)).thenReturn(false);
		when(reservationUpdateValidatorExtension.supports(EventType.CANCELLATION)).thenReturn(false);
		when(reservationCancellationValidatorExtension.supports(EventType.CANCELLATION)).thenReturn(true);

		Extension extension = ExtensionUtils.get(reservationValidatorExtensions, EventType.CANCELLATION);

		assertThat(extension).isEqualTo(reservationCancellationValidatorExtension);
	}

	@Test
	public void testGetReservationValidatorExtension_throwsExtensionNotFoundException() {
		when(defaultReservationValidatorExtension.supports(EventType.CREATION)).thenReturn(false);
		when(reservationCreationValidatorExtension.supports(EventType.CREATION)).thenReturn(false);
		when(reservationUpdateValidatorExtension.supports(EventType.CREATION)).thenReturn(false);
		when(reservationCancellationValidatorExtension.supports(EventType.CREATION)).thenReturn(false);

		when(defaultReservationValidatorExtension.isDefault()).thenReturn(false);

		assertThatThrownBy(() -> ExtensionUtils.get(reservationValidatorExtensions, EventType.CREATION))
				.isInstanceOf(ExtensionNotFoundException.class)
				.hasMessageContaining(EventType.CREATION.name());
	}
}
