package com.reservations.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
	Optional<Reservation> findByBookingIdentifierUuidAndStatus(String bookingIdentifierUuid, ReservationStatus reservationStatus);

	@Query("SELECT r FROM Reservation r WHERE NOT(r.departureDate <= ?1 OR r.arrivalDate >= ?2) AND r.status = ?3 ORDER BY r.arrivalDate ASC")
	Page<Reservation> findReservationsByDateRangeAndStatus(LocalDate start, LocalDate end, ReservationStatus reservationStatus, Pageable pageable);

	// We omit the bookingIdentifierUuid, in order to be able to update the same subscription in the same date range
	@Query("SELECT count(*) FROM Reservation r WHERE NOT(r.departureDate <= ?1 OR r.arrivalDate >= ?2) AND r.status = ?3 AND r.bookingIdentifierUuid <> ?4")
	Long findQuantityByDateRangeAndStatusOmittingBookingIdentifierUuid(LocalDate start, LocalDate end, ReservationStatus reservationStatus, String bookingIdentifierUuid);
}
