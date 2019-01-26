package com.reservations.validation;

import static com.reservations.TestUtils.DEFAULT_ERROR_MESSAGE;
import static com.reservations.TestUtils.basicError;
import static com.reservations.TestUtils.basicReservation;
import static com.reservations.validation.ReservationValidatorConstants.ARRIVAL_DATE_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.util.Collections;

import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.reservations.entity.EventType;
import com.reservations.entity.Reservation;
import com.reservations.exception.ReservationValidationException;

public class ReservationCancellationValidatorExtensionImplTest {
	private static final int MIN_ARRIVAL_AHEAD_DAYS = 1;
	private static final int MAX_ADVANCE_DAYS = 30;
	private static final int MIN_DURATION = 1;
	private static final int MAX_DURATION = 3;

	private ReservationCancellationValidatorExtensionImpl reservationCancellationValidatorExtension;

	@Mock
	private MessageSource messages;

	@BeforeMethod
	public void setup() {
		initMocks(this);

		when(messages.getMessage(any(), any(), any())).thenReturn(DEFAULT_ERROR_MESSAGE);

		reservationCancellationValidatorExtension = new ReservationCancellationValidatorExtensionImpl(
				MIN_ARRIVAL_AHEAD_DAYS,
				MAX_ADVANCE_DAYS,
				MIN_DURATION,
				MAX_DURATION,
				messages);
	}

	@Test
	public void testIsDefault_false() {
		assertThat(reservationCancellationValidatorExtension.isDefault()).isFalse();
	}

	@Test
	public void testSupportsEventTypeCreation_false() {
		assertThat(reservationCancellationValidatorExtension.supports(EventType.CREATION)).isFalse();
	}

	@Test
	public void testSupportsEventTypeUpdate_false() {
		assertThat(reservationCancellationValidatorExtension.supports(EventType.UPDATE)).isFalse();
	}

	@Test
	public void testSupportsEventTypeCancellation_true() {
		assertThat(reservationCancellationValidatorExtension.supports(EventType.CANCELLATION)).isTrue();
	}

	@Test
	public void testReservationCancellationAfterItStarted_throwsReservationValidationException() {
		LocalDate arrivalDate = LocalDate.now().minusDays(1);
		LocalDate departureDate = arrivalDate.plusDays(MIN_DURATION);
		Reservation reservation = basicReservation(arrivalDate, departureDate);

		try {
			reservationCancellationValidatorExtension.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsOnly(basicError(Collections.singletonList(ARRIVAL_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}
}
