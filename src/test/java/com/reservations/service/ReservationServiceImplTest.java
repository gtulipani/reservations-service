package com.reservations.service;

import static com.reservations.TestUtils.basicReservation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;
import com.reservations.repository.ReservationRepository;

public class ReservationServiceImplTest {
	@Mock
	private ReservationRepository reservationRepository;

	@InjectMocks
	private ReservationServiceImpl reservationService;

	@BeforeMethod
	public void setup() {
		initMocks(this);
	}

	@Test
	public void testCreateReservation() {
		Reservation reservation = basicReservation();

		Reservation updatedReservation = reservationService.createReservation(reservation);

		verify(reservationRepository).save(reservation);

		reservation.setStatus(ReservationStatus.ACTIVE);
		assertThat(updatedReservation).isEqualTo(reservation);
	}
}
