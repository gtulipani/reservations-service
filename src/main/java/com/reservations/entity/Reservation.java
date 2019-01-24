package com.reservations.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Builder
@ToString
@Table(name = "reservations")
@JsonIgnoreProperties(value = {"id"})
public class Reservation implements Serializable {
	private static final long serialVersionUID = -6507963547063710509L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "created_on", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy hh:mm:ss")
	private LocalDateTime createdOn;

	@Column(name = "last_modified", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy hh:mm:ss")
	private LocalDateTime lastModified;

	@Column(name = "email", nullable = false)
	@NotNull
	@NotEmpty
	private String email;

	@Column(name = "full_name", nullable = false)
	@NotNull
	@NotEmpty
	private String fullName;

	@Enumerated(EnumType.STRING)
	private ReservationStatus status;

	@Column(name = "arrival_date", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy")
	private LocalDate arrivalDate;

	@Column(name = "departure_date", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy")
	private LocalDate departureDate;

	@Column(name = "booking_identifier_uuid", nullable = false, length = 50)
	private String bookingIdentifierUuid;

	@PrePersist
	protected void onCreate() {
		createdOn = LocalDateTime.now();
		lastModified = LocalDateTime.now();
		bookingIdentifierUuid = UUID.randomUUID().toString();
	}

	@PreUpdate
	protected void onUpdate() {
		lastModified = LocalDateTime.now();
	}
}
