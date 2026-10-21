# Deployment

Ticket Service runs in Payara domain1 at context root `/`, with JAX-RS `/api/v1`.
Booking Service runs in domain2 at context root `/booking`, with JAX-RS `/api/v1/`.
The public Booking proxy targets `/booking/api/v1/reservation/`.

Use JDK 17 and `mvn clean package`. The database action remains `none`.
Apply `migrations/V002__nullable_fields.sql` transactionally to the existing schema
before deploying this release. It preserves rows and checks the int32 range before
changing the type of tickets.y. Back up the database and current WARs first.

The approved Swagger schema copies are packaged as `api-schemas.json`. The request
filter validates write payloads against them before JSON-B deserialization. Keep
these copies in sync only when an approved API contract changes. Unknown object
properties remain allowed according to OpenAPI's default; read-only fields do not
become required on write. Response DTOs follow the same property names and types.

Use POST `/tickets/search` and `/persons/search` for sorting/filtering. GET lists
support page/size only, as described in the approved contract. Pages start at zero.
Nested sorting supports all enum values from the respective Swagger schemas;
ID is the deterministic final tie-breaker unless explicitly sorted already.

Ticket updates reuse an existing person when person.id is present. Person attributes
are changed through PUT /persons/{id}; a nested person without an ID is an explicit
new person. The frontend preserves this distinction in its person selector.

Booking's `ticket.service.url`, truststore path/password-file/type and timeouts
must be supplied via external MicroProfile Config. The HTTPS URI and truststore
are verified at startup. Never put secrets into source or disable peer validation.
The existing VPS configuration and client-specific truststore are retained.

Booking accepts discounts from 0 through 100 percent. The final sale price has a
minimum of 1 ruble: max(1, originalPrice * (1 - discount / 100)). A 100% discount
therefore creates a ticket priced at 1 ruble and returns 201. Calculating the
percentage in double precision avoids overflow for valid large float prices.
The approved API schemas and Ticket Service price validation remain unchanged.

The integration includes upstream main through 7a28975984d7faa8af0bb41fb3ec3612c8807cfb.
It retains LocalDate birthdays, the birthday/type response names, Russian generic
server errors and explicit validation of ranges, sort fields and directions.
Search uses the schema-based whitelist instead of duplicate typed filter records:
this preserves explicit type:null filters and contract page limits. Booking emits
person.location, not locationDto. Nullable equality and date conversion remain in
CriteriaSearch, including correct handling of creationDate ranges.
