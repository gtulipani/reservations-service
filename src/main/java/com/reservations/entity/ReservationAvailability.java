package com.reservations.entity;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class ReservationAvailability {
	LocalDate date;
	Long availability;
}
