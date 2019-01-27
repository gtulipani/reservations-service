package com.reservations.validation;

import static com.reservations.TestUtils.DEFAULT_ERROR_MESSAGE;
import static com.reservations.TestUtils.basicError;
import static com.reservations.TestUtils.basicReservation;
import static com.reservations.validation.ReservationValidatorConstants.ARRIVAL_DATE_FIELD;
import static com.reservations.validation.ReservationValidatorConstants.DEPARTURE_DATE_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
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

import com.reservations.entity.DateRange;
import com.reservations.entity.Reservation;
import com.reservations.exception.ReservationValidationException;
import com.reservations.service.ReservationServiceImpl;

/**
 * Altough {@link ReservationCompleteValidatorExtension} is an abstract class, it has some methods implemented.
 * Therefore, we are instantiating one of the implementations and verifying only these methods.
 */
public class ReservationCompleteValidatorExtensionTest {
	private static final int MIN_ARRIVAL_AHEAD_DAYS = 1;
	private static final int MAX_ADVANCE_DAYS = 30;
	private static final int MIN_DURATION = 1;
	private static final int MAX_DURATION = 3;

	private ReservationCompleteValidatorExtension reservationCompleteValidatorExtension;

	@Mock
	private ReservationServiceImpl reservationService;
	@Mock
	private MessageSource messages;

	@BeforeMethod
	public void setup() {
		initMocks(this);

		when(messages.getMessage(any(), any(), any())).thenReturn(DEFAULT_ERROR_MESSAGE);

		reservationCompleteValidatorExtension = new ReservationCreationValidatorExtensionImpl(
				MIN_ARRIVAL_AHEAD_DAYS,
				MAX_ADVANCE_DAYS,
				MIN_DURATION,
				MAX_DURATION,
				reservationService,
				messages);
	}

	@Test
	public void testReservationBeforeMinimumAheadDays_throwsReservationValidationException() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS).minusDays(1))
				.end(LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS).minusDays(1).plusDays(MIN_DURATION))
				.build();
		Reservation reservation = basicReservation(dateRange);
		when(reservationService.checkAvailability(dateRange)).thenReturn(true);

		try {
			reservationCompleteValidatorExtension.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsOnly(basicError(Collections.singletonList(ARRIVAL_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationAfterMaximumAdvanceDays_throwsReservationValidationException() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now().plusDays(MAX_ADVANCE_DAYS).plusDays(1))
				.end(LocalDate.now().plusDays(MAX_ADVANCE_DAYS).plusDays(1).plusDays(MIN_DURATION))
				.build();
		Reservation reservation = basicReservation(dateRange);
		when(reservationService.checkAvailability(dateRange)).thenReturn(true);

		try {
			reservationCompleteValidatorExtension.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsOnly(basicError(Collections.singletonList(ARRIVAL_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationWithLessDurationThanMinimum_throwsReservationValidationException() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS))
				.end(LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS).plusDays(MIN_DURATION).minusDays(1))
				.build();
		Reservation reservation = basicReservation(dateRange);
		when(reservationService.checkAvailability(dateRange)).thenReturn(true);

		try {
			reservationCompleteValidatorExtension.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsOnly(basicError(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationWithMoreDurationThanMaximum_throwsReservationValidationException() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS))
				.end(LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS).plusDays(MAX_DURATION).plusDays(1))
				.build();
		Reservation reservation = basicReservation(dateRange);
		when(reservationService.checkAvailability(dateRange)).thenReturn(true);

		try {
			reservationCompleteValidatorExtension.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsOnly(basicError(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationBeforeMinimumAheadDaysAndWithLessDurationThanMinimum_throwsReservationValidationException() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS).minusDays(1))
				.end(LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS).minusDays(1).plusDays(MIN_DURATION).minusDays(1))
				.build();
		Reservation reservation = basicReservation(dateRange);
		when(reservationService.checkAvailability(dateRange)).thenReturn(true);

		try {
			reservationCompleteValidatorExtension.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsExactlyInAnyOrder(
					basicError(Collections.singletonList(ARRIVAL_DATE_FIELD), DEFAULT_ERROR_MESSAGE),
					basicError(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationAfterMaximumAdvanceDaysAndWithMoreDurationThanMaximum_throwsReservationValidationException() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now().plusDays(MAX_ADVANCE_DAYS).plusDays(1))
				.end(LocalDate.now().plusDays(MAX_ADVANCE_DAYS).plusDays(1).plusDays(MAX_DURATION).plusDays(1))
				.build();
		Reservation reservation = basicReservation(dateRange);
		when(reservationService.checkAvailability(dateRange)).thenReturn(true);

		try {
			reservationCompleteValidatorExtension.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsExactlyInAnyOrder(
					basicError(Collections.singletonList(ARRIVAL_DATE_FIELD), DEFAULT_ERROR_MESSAGE),
					basicError(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}

	@Test
	public void testReservationWithoutAvailability_throwsReservationValidationException() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS))
				.end(LocalDate.now().plusDays(MIN_ARRIVAL_AHEAD_DAYS).plusDays(MIN_DURATION))
				.build();
		Reservation reservation = basicReservation(dateRange);
		when(reservationService.checkAvailability(dateRange)).thenReturn(false);

		try {
			reservationCompleteValidatorExtension.validate(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			assertThat(e.getErrors()).containsOnly(basicError(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD), DEFAULT_ERROR_MESSAGE));
		}
	}
}
