# RV Park Reservation System MVP

A Spring Boot REST API for managing RV park and ATV park reservations.

## Features

- **200 Campsites**: 75 full hookup RV sites ($40/night), 125 tent sites ($15/night)
- **ATV Park Integration**: Age-based pricing ($20 adults, $10 teens 15-17, free under 15)
- **Party Management**: Track party members and vehicle information
- **Flexible Payments**: 25% deposit or full payment upfront
- **Cancellation Policy**: Full refund 14+ days, 50% refund 7-13 days, no refund after
- **Business Rules**: 1pm check-in, 11am check-out, same-day booking allowed

## Technology Stack

- **Java 21** with Spring Boot 3.5
- **Spring Data JPA** with H2 (development) / PostgreSQL (production)
- **RESTful API** with JSON responses
- **Docker** containerization
- **Lombok** for clean code
- **Maven** build system

## Quick Start

### Using Docker (Recommended)
```bash
docker-compose up --build
```

### Manual Setup
```bash
./mvnw clean package
./mvnw spring-boot:run
```

## API Endpoints

### Customers
- `GET /api/customers` - List all customers
- `GET /api/customers/{id}` - Get customer by ID
- `GET /api/customers/search?term={searchTerm}` - Search customers
- `POST /api/customers` - Create new customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Soft delete customer

### Campsites
- `GET /api/campsites` - List all campsites
- `GET /api/campsites/available` - Available campsites
- `GET /api/campsites/available/type/{FULL_HOOKUP|TENT}` - Available by type
- `GET /api/campsites/available/dates?startDate=2024-06-01&endDate=2024-06-07` - Available for dates
- `PUT /api/campsites/{id}/maintenance` - Mark for maintenance
- `PUT /api/campsites/{id}/available` - Mark as available

### Reservations
- `GET /api/reservations` - List all reservations
- `GET /api/reservations/{id}` - Get reservation by ID
- `GET /api/reservations/confirmation/{confirmationNumber}` - Get by confirmation
- `GET /api/reservations/customer/{customerId}` - Get by customer
- `GET /api/reservations/checkin/today` - Today's check-ins
- `GET /api/reservations/checkout/today` - Today's check-outs
- `PUT /api/reservations/{id}/checkin` - Check in reservation
- `PUT /api/reservations/{id}/checkout` - Check out reservation
- `PUT /api/reservations/{id}/cancel` - Cancel reservation

## Data Models

### Customer
```json
{
  "firstName": "John",
  "lastName": "Doe", 
  "email": "john@example.com",
  "phone": "555-1234",
  "emergencyContactName": "Jane Doe",
  "emergencyContactRelationship": "Spouse",
  "emergencyEmail": "jane@example.com",
  "emergencyPhone": "555-5678"
}
```

### Reservation Request
```json
{
  "customerId": 1,
  "campsiteId": 15,
  "startDate": "2024-06-01",
  "endDate": "2024-06-07",
  "partyMembers": ["John Doe", "Jane Doe", "Billy Doe"],
  "partySize": 3,
  "vehicleLicensePlate": "ABC123",
  "vehicleMake": "Ford",
  "vehicleModel": "F-350",
  "rvLengthFeet": 35
}
```

## Development

### Database Console
- H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`, Password: `password`

### Project Structure
```
src/main/java/com/reservations/reservation_system/
├── entity/          # JPA entities
├── enums/           # Business enums
├── valueobject/     # Value objects
├── repository/      # Data access layer
├── controller/      # REST controllers
├── dto/             # API DTOs
└── config/          # Configuration
```

## Production Deployment

1. Update `application.properties` for PostgreSQL
2. Set environment variables for database connection
3. Use `docker-compose up --build` for containerized deployment
4. Configure reverse proxy for HTTPS (nginx recommended)
