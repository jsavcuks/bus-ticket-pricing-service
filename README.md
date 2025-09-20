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

1. Create/set a base price for a bus terminal
- POST /api/bus-terminals
  - Request
    - ```json
        {
            "terminalName": "Tallinn, Estonia",
            "basePrice": 30.00
        }
  - Successful response (example)
      - ```json
        {
            "terminalName": "Tallinn, Estonia",
            "basePrice": 30.00
        }
  
- Responses
    - 201 Created
    - 409 Conflict: terminal already exists
      - ```json
           {
                "timestamp": "2025-09-20 12:54:45",
                "status": 409,
                "error": "Validation error",
                "path": "/api/bus-terminals",
                "errors": [
                    {
                    "field": "terminalName",
                    "message": "terminal already exists",
                    "rejectedValue": "Tallinn, Estonia"
                    }
                ]
           }
        
2. Calculate draft ticket price
- POST /api/pricing/draft
  - Request
  - ```json
    {
        "route": "Vilnius, Lithuania",
        "passengers": [
            { "type": "ADULT", "luggageCount": 1 }
        ]
    }
- Successful response (example)
  - ```json
        {
            "items": [
              {
                "description": "Passenger 1 (ADULT)",
                "price": 12.10,
                "priceDescription": "Adult (10.00 EUR + 21%) = 12.10 EUR"
              },
              {
                "description": "Luggage for passenger 1 (1 item)",
                "price": 3.63,
                "priceDescription": "1 item (1 x 10.00 EUR x 30% + 21%) = 3.63 EUR"
              }
            ],
            "totalPrice": 15.73,
            "totalPriceDescription": "15.73 EUR"
        }
- Error responses
    - 404 Not Found: unknown route (terminal not in DB)
      - ```json
         {
              "timestamp": "2025-09-20 12:58:50",
              "status": 404,
              "error": "Validation error",
              "path": "/api/pricing/draft",
              "errors": [
                  {
                  "field": "route",
                  "message": "route not found",
                  "rejectedValue": "Kaunas, Lithuania"
                  }
              ]
          }
    - 400 Bad Request: validation failed (blank route, empty passengers, etc.) 
      - ```json
            {
                  "timestamp": "2025-09-20 13:05:34",
                  "status": 400,
                  "error": "Validation failed",
                  "path": "/api/pricing/draft",
                  "errors": [
                      {
                      "field": "passengers[0].luggageCount",
                      "message": "Luggage count must be greater than or equal to 0",
                      "rejectedValue": -1
                      }
                  ]
             }
    - 500 Internal Server Error: unexpected error

- The base price is retrieved by the terminal name (route).
- For each passenger:
    - Passenger item:
        - Adult: base
        - Child: base × 0.5
    - Luggage item (if any): base × 0.3 × luggageCount
    - Taxes: compute taxMultiplier = 1 + (sum of tax rates)/100 and multiply each item by taxMultiplier
- Rounding: price per item is rounded to two decimals (HALF_UP). Total is the sum of item prices rounded to 2 decimals.
- Example (acceptance criteria)
    - Base: 10.00, VAT: 21%
    - Adult: 10 × 1.21 = 12.10
    - Two bags: 2 × (10 × 0.30) × 1.21 = 7.26
    - Child: (10 × 0.50) × 1.21 = 6.05
    - One bag: (10 × 0.30) × 1.21 = 3.63
    - Total = 29.04

Testing

How to run
- Unix/macOS: `./gradlew test`
- Windows: `gradlew.bat test`

Exception handling
- Validation errors (MethodArgumentNotValidException/BindException) → ApiError with errors[]
- Custom ValidationErrorException → ApiError with provided HTTP status (e.g., 422)
- Generic exceptions → 500 Internal Server Error with "Unexpected error"

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