# Bus Ticket Pricing Service

A Spring Boot service that calculates draft ticket prices for bus routes using:
- Base price per bus terminal (stored in H2)
- Passenger type discounts (child 50%)
- Luggage pricing (30% of base per bag)
- Date-based tax rates (summed and applied to each item)

Implementation description

- Technology: Java 21, Spring Boot, Spring MVC, Spring Data JPA (H2), Jakarta Validation, Lombok.
- Persistence: Only BusTerminal (terminal name + base price) is stored in H2.
- External services (conceptually):
    - Base price service uses the BusTerminal table.
    - Tax rate service is in-memory and returns VAT 21% (can be extended).
- Calculation:
    - Adult fare = base price
    - Child fare = base price × 50%
    - Luggage = base price × 30% per bag
    - Taxes: Sum all percentage tax rates for the given date and apply to each item
    - Rounding: Each item and total rounded to 2 decimals, HALF_UP

How to run

- Start the app:
    - Unix/macOS: ./gradlew bootRun
    - Windows: gradlew.bat bootRun
- Run tests:
    - Unix/macOS: ./gradlew test
    - Windows: gradlew.bat test
- H2 Console (optional):
    - Enable via Spring properties if needed and visit /h2-console

API

1) Create/set base price for a bus terminal
- POST /api/bus-terminals
- Request
  json { "terminalName": "Vilnius, Lithuania", "basePrice": 10.00 }
- Responses
    - 201 Created: terminal stored
    - 409 Conflict: terminal already exists
2) Calculate draft ticket price
- POST /api/pricing/draft
- Request
  json { "route": "Vilnius, Lithuania", "date": "2025-01-01", "passengers": }
- Successful response (example)
  json { "items": , "total": 29.04 }
- Error responses
    - 404 Not Found: unknown route (terminal not in DB)
    - 400 Bad Request: validation errors (blank route, missing date, empty passengers, etc.)

Calculation rules

- Base price is retrieved by terminal name (route).
- For each passenger:
    - Passenger item:
        - Adult: base
        - Child: base × 0.5
    - Luggage item (if any): base × 0.3 × luggageCount
    - Taxes: compute taxMultiplier = 1 + (sum of tax rates)/100 and multiply each item by taxMultiplier
- Rounding: price per item is rounded to 2 decimals (HALF_UP). Total is the sum of item prices rounded to 2 decimals.
- Example (acceptance criteria)
    - Base: 10.00, VAT: 21%
    - Adult: 10 × 1.21 = 12.10
    - Two bags: 2 × (10 × 0.30) × 1.21 = 7.26
    - Child: (10 × 0.50) × 1.21 = 6.05
    - One bag: (10 × 0.30) × 1.21 = 3.63
    - Total = 29.04

Testing

- Unit tests cover:
    - Acceptance case (adult+2 bags, child+1 bag, VAT 21) → total 29.04
    - Multiple taxes summed and applied
    - Empty passenger list behavior in the service
    - Base price service success and not-found cases
- Web slice tests cover:
    - POST /api/pricing/draft returns expected JSON
    - Validation errors (400) and route-not-found mapping (404)

Extensibility considerations

- Passenger types:
    - Add new enum values and a mapping of type → discount or pricing strategy.
    - Consider a configuration-driven rules engine for discounts to avoid frequent code changes.
- Luggage multipliers:
    - Make luggage pricing configurable per route or tier; introduce a FareRules config entity/service.
- Currency:
    - Add a currency field on requests and store base prices in a canonical currency.
    - Integrate a currency conversion service with date-based FX rates; keep calculations in BigDecimal.
- Multiple routes and tax calendars:
    - Store multiple terminals; BasePriceService already supports lookup by name.
    - Replace the in-memory tax provider with a real TaxRateService that sources rates by date/region.
    - Support multiple tax components (VAT, municipal, service fee) and possibly tax exemptions by passenger type.
- Validation and error handling:
    - Use a consistent problem-details JSON structure for errors.
    - Add route normalization (case/trim) to improve lookup UX.
- Performance:
    - Cache base prices and tax rates per date/route if needed, with proper TTLs.
- API evolution:
    - Version endpoints (e.g., /api/v1/pricing/draft).
    - Consider adding an endpoint to preview/configure fare rules.

Notes

- Only base prices are persisted. All other data is computed at request time.
- Keep monetary amounts as numeric JSON values; presentation formatting is up to clients.

