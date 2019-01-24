CREATE TABLE IF NOT EXISTS `reservations` (
	`id`											bigint(20) unsigned NOT NULL AUTO_INCREMENT,
	`created_on`							datetime						NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified`						datetime						NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`status`									varchar(50)					NOT NULL DEFAULT '',
	`arrival_date`						date								NOT NULL,
	`departure_date`					date								NOT NULL,
	`booking_identifier_uuid`	varchar(50)						NOT NULL DEFAULT '',
	`email`										varchar(255)				NOT NULL DEFAULT '',
	`full_name`								varchar(255)				NOT NULL DEFAULT '',
	PRIMARY KEY (`id`)
)
	ENGINE = InnoDB
	DEFAULT CHARSET = utf8;
