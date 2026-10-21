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

A 100% discount cannot produce a valid Ticket because the approved Ticket schema
requires price > 0. This combination returns the documented 400 ErrorResponse,
with a message explaining the positive-price requirement; it never creates a
zero-priced ticket or substitutes a different discount.
