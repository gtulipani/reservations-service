package com.reservations.validation;

import static com.reservations.TestUtils.DEFAULT_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.reservations.entity.EventType;
import com.reservations.service.ReservationServiceImpl;

public class ReservationCreationValidatorExtensionImplTest {
	private static final int MIN_ARRIVAL_AHEAD_DAYS = 1;
	private static final int MAX_ADVANCE_DAYS = 30;
	private static final int MIN_DURATION = 1;
	private static final int MAX_DURATION = 3;

	private ReservationCreationValidatorExtensionImpl reservationCreationValidatorExtension;

	@Mock
	private ReservationServiceImpl reservationService;
	@Mock
	private MessageSource messages;

	@BeforeMethod
	public void setup() {
		initMocks(this);

		when(messages.getMessage(any(), any(), any())).thenReturn(DEFAULT_ERROR_MESSAGE);

		reservationCreationValidatorExtension = new ReservationCreationValidatorExtensionImpl(
				MIN_ARRIVAL_AHEAD_DAYS,
				MAX_ADVANCE_DAYS,
				MIN_DURATION,
				MAX_DURATION,
				reservationService,
				messages);
	}

	@Test
	public void testIsDefault_false() {
		assertThat(reservationCreationValidatorExtension.isDefault()).isFalse();
	}

	@Test
	public void testSupportsEventTypeCreation_true() {
		assertThat(reservationCreationValidatorExtension.supports(EventType.CREATION)).isTrue();
	}

	@Test
	public void testSupportsEventTypeUpdate_false() {
		assertThat(reservationCreationValidatorExtension.supports(EventType.UPDATE)).isFalse();
	}

	@Test
	public void testSupportsEventTypeCancellation_false() {
		assertThat(reservationCreationValidatorExtension.supports(EventType.CANCELLATION)).isFalse();
	}
}
