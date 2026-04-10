# Feed Normalization Service

A Spring Boot microservice that normalizes sports betting feed messages from two providers into a unified internal format.

## Requirements

- Java 21+
- Maven 3.8+

## How to Run

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

## Endpoints

### ProviderAlpha — POST /provider-alpha/feed

**ODDS_CHANGE:**
```bash
curl -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"odds_update","event_id":"ev123","values":{"1":2.0,"X":3.1,"2":3.8}}'
```

**BET_SETTLEMENT:**
```bash
curl -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"settlement","event_id":"ev123","outcome":"1"}'
```

### ProviderBeta — POST /provider-beta/feed

**ODDS_CHANGE:**
```bash
curl -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"ODDS","event_id":"ev456","odds":{"home":1.95,"draw":3.2,"away":4.0}}'
```

**BET_SETTLEMENT:**
```bash
curl -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"SETTLEMENT","event_id":"ev456","result":"away"}'
```

## Standardized Internal Format

All messages from both providers are normalized to one of:

### ODDS_CHANGE
```json
{
  "messageType": "ODDS_CHANGE",
  "provider": "ALPHA",
  "eventId": "ev123",
  "odds": { "home": 2.0, "draw": 3.1, "away": 3.8 },
  "timestamp": "2026-04-09T10:00:00Z"
}
```

### BET_SETTLEMENT
```json
{
  "messageType": "BET_SETTLEMENT",
  "provider": "BETA",
  "eventId": "ev456",
  "outcome": "AWAY",
  "timestamp": "2026-04-09T10:00:00Z"
}
```

Provider values: `ALPHA`, `BETA`

Outcome values: `HOME`, `DRAW`, `AWAY`

Returns **HTTP 200 OK** — the message was normalized and dispatched. Look for `[MESSAGE-QUEUE]` in the application logs.

Returns **HTTP 400 Bad Request** if:
- `msg_type`/`type` or `event_id` is missing or blank
- message type is unknown
- required odds/outcome fields are missing or contain invalid keys

```json
{"message": "Unknown Alpha msg_type: foo"}
{"message": "event_id must not be blank"}
{"message": "Alpha odds_update missing required field: values"}
{"message": "Alpha odds_update values must contain keys: 1, X, 2 (got: [home, draw, away])"}
```

## How to Run Tests

```bash
mvn test
```

## How to Build

```bash
mvn package
java -jar target/feeds-0.0.1-SNAPSHOT.jar
```
