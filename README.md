# reservations-service

## Description
Microservice that handles reservations requests for a fictional campsite.

## Building and Running
### Build
#### Locally using maven
The standard way to build the application is with the following maven command in the root path:
```
mvn clean install -DskipTests
```

Take into account that the previous command doesn't include running the UTs. These can be by omitting the `-DskipTests`
parameter or by executing the following command:
```
mvn test
```

After executing the tests, a HTML report is generated using surefire plugin under path:
```
target/surefire-reports/emailable-report.html
```


### Run
#### Locally using maven
The standard way to run the application is with the following maven command:
```
mvn spring-boot:run
```

Take into account that a MySQL Service running is required before executing that command.

## Technologies
### Spring
The Application is a standard Spring Boot Application and it's conformed by the following layers:
- **ReservationController**: handles all the incoming requests from the outside world.
- **ReservationService**: contains all the logic to get, create, update or cancel reservations.
- **ReservationValidatorExtension**: extension that contains all the business logic applicable to reservations, such as
maximum duration of the reservation or check-in time.
- **ReservationRepository**: CrudRespository that handles the last communication between the service and the Database.

### MySQL
The chosen Database Engine is MySQL, a free relational DB model. By the moment it contains only one table called
`reservations` with the following columns
```
id|created_on|last_modified|status|arrival_date|departure_date|booking_identifier_uuid|email|full_name|
```

### Flyway
Flyway is a database migration tool similar to Liquibase. It is recommended by Spring Boot.
See the documentation [here](http://flywaydb.org/). The migrations scripts are in SQL directly. Make sure that your SQL
scripts run both on MySQL and H2. H2's syntax is pretty flexible and will handle most MySQL specific instructions.

The project is configured to run the migration scripts on start.

##### Configuration
Flyway migration can be disabled completely by disabling `flyway.enabled`. On certain occasions, a SQL script might be
changed after being run. Flyway validates the checksum of each migration and will report error on startup. Enable
`flyway.repair` to correct this situation.

## Postman Collection
A Postman has been included in the repository, containing all the existing endpoints among with examples:
### GET /reservations/availability
API to get the campsite availability within a given range. It accepts to query parameters: `start` and `end`. These
parameters represent dates and the expected format is `MM-dd-yyyy` (as defined in the `Reservation` entity). The
response includes for all the dates contained in the range (default is one month from today) the quantity of available
reservations:
```
[
    {
        "date": [
            2019,
            1,
            27
        ],
        "availability": 9
    },
    {
        "date": [
            2019,
            1,
            28
        ],
        "availability": 10
    }
]
```
### POST /reservations
API to create a new reservation. The complete reservation data is returned as part of the response, including a
`bookingIdentifierUuid`, which can be used to update or cancel it later. Expected JSON input:
```
{
    "email": "gtulipani@hotmail.com",
    "fullName": "Gaston Tulipani",
    "arrivalDate": "02-11-2019",
    "departureDate": "02-12-2019"
}
```
### PATCH /reservations/{bookingIdentifierUuid}
API to update an existing reservation. Only the arrival and departure dates can be modified.
Take into account that same business rules apply at the update time, maximum duration of the reservation or check-in
time. Expected input:
```
{
    "arrivalDate": "02-12-2019",
    "departureDate": "02-13-2019"
}
```
### DELETE /reservations/{bookingIdentifierUuid}
API to cancel an existing reservation. The reservation can be cancelled any time, if it hasn't already started.

## Business Rules
There is a set of business rules that are being applied for all available flows (Creation, Update and Cancellation).
These business rules are defined by environment variables, which can be easily modified without altering the code.
- `reservations.availability-default-days`: default days to apply in the range when asking for campsite visiblity.
Currently **30**.
- `reservations.min-arrival-ahead-days`: minimum quantity of days ahead to reserve the campsite. Currently **1**.
- `reservations.max-advance-days`: maximum quantity of days to reserve the campsite in advance. Currently **30**.
- `reservations.min-duration`: minimum quantity of days to reserve the campsite. Currently **1**.
- `reservations.max-duration`: maximum quantity of days to reserve the campsite. Currently **3**.
- `reservations.max-capacity`: maximum capacity of the campsite per day. Currently **10**.
- `reservations.check-in-time-hour`: hour of the day for check-in. This is used to validate if the cancellation can take
place. Currently **12**.
- `reservations.check-in-time-minute`: minute of the hour of the day for check-in. This is used ti validate if the
cancellation can take place. Currently **00**.

## CI

## More Stats
The project includes a total of **87** Unit Tests. The total Coverage is **92%**, where all classes have **100%**
coverage, except from `Application.java` (main Spring Boot Application class) and `ReservationValidatorConstants.java`
classes.
