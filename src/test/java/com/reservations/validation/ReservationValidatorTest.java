package com.reservations.validation;

import static com.reservations.TestUtils.DEFAULT_ERROR_MESSAGE;
import static com.reservations.TestUtils.basicError;
import static com.reservations.TestUtils.basicReservation;
import static com.reservations.validation.ReservationValidatorConstants.ARRIVAL_DATE_FIELD;
import static com.reservations.validation.ReservationValidatorConstants.DEPARTURE_DATE_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.reservations.entity.Reservation;
import com.reservations.exception.ReservationValidationException;

public class ReservationValidatorTest {
	private static final int MIN_ARRIVAL_AHEAD_DAYS = 1;
	private static final int MAX_ADVANCE_DAYS = 30;
	private static final int MIN_DURATION = 1;
	private static final int MAX_DURATION = 3;

	private ReservationValidator reservationValidator;

	@Mock
	private MessageSource messages;

	@BeforeMethod
	public void setup() {
		initMocks(this);

		when(messages.getMessage(any(), any(), any())).thenReturn(DEFAULT_ERROR_MESSAGE);

		reservationValidator = new ReservationValidator(
				MIN_ARRIVAL_AHEAD_DAYS,
				MAX_ADVANCE_DAYS,
				MIN_DURATION,
				MAX_DURATION,
				messages);
	}

	@Test
	public void testValidReservation_noErrors() {
		Reservation reservation = basicReservation();

		assertThatCode(() -> reservationValidator.validate(reservation)).doesNotThrowAnyException();
	}

	@Test
	public void testReservationBeforeMinimumAheadDays_throwsReservationValidationException() {
		LocalDate arrivalDate = LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS - 1);
		LocalDate departureDate = arrivalDate.plusDays(MIN_DURATION);
		Reservation reservation = basicReservation(arrivalDate, departureDate);

		try {
			reservationValidator.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsOnly(basicError(Collections.singletonList(ARRIVAL_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationAfterMaximumAdvanceDays_throwsReservationValidationException() {
		LocalDate arrivalDate = LocalDate.now().plusDays(MAX_ADVANCE_DAYS + 1);
		LocalDate departureDate = arrivalDate.plusDays(MIN_DURATION);
		Reservation reservation = basicReservation(arrivalDate, departureDate);

		try {
			reservationValidator.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsOnly(basicError(Collections.singletonList(ARRIVAL_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationWithLessDurationThanMinimum_throwsReservationValidationException() {
		LocalDate arrivalDate = LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS);
		LocalDate departureDate = arrivalDate.plusDays(MIN_DURATION - 1);
		Reservation reservation = basicReservation(arrivalDate, departureDate);

		try {
			reservationValidator.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsOnly(basicError(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationWithMoreDurationThanMaximum_throwsReservationValidationException() {
		LocalDate arrivalDate = LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS);
		LocalDate departureDate = arrivalDate.plusDays(MAX_DURATION + 1);
		Reservation reservation = basicReservation(arrivalDate, departureDate);

		try {
			reservationValidator.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsOnly(basicError(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationBeforeMinimumAheadDaysAndWithLessDurationThanMinimum_throwsReservationValidationException() {
		LocalDate arrivalDate = LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS - 1);
		LocalDate departureDate = arrivalDate.plusDays(MIN_DURATION - 1);
		Reservation reservation = basicReservation(arrivalDate, departureDate);

		try {
			reservationValidator.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsExactlyInAnyOrder(
					basicError(Collections.singletonList(ARRIVAL_DATE_FIELD), DEFAULT_ERROR_MESSAGE),
					basicError(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationAfterMaximumAdvanceDaysAndWithMoreDurationThanMaximum_throwsReservationValidationException() {
		LocalDate arrivalDate = LocalDate.now().plusDays(MAX_ADVANCE_DAYS + 1);
		LocalDate departureDate = arrivalDate.plusDays(MAX_DURATION + 1);
		Reservation reservation = basicReservation(arrivalDate, departureDate);

		try {
			reservationValidator.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsExactlyInAnyOrder(
					basicError(Collections.singletonList(ARRIVAL_DATE_FIELD), DEFAULT_ERROR_MESSAGE),
					basicError(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}
}
