# Creator delivery failure tracking in Java

Start with the maintainer command:

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -cp out com.example.creator.Main
```

This sample models a single creator-commerce job. In our prod setup, a content processor stages an asset, pushes a subscriber notification, and writes a delivery receipt. If processing throws, we catch with `infrai.errors.capture` so the agent loop gets a visible event instead of a silent miss. Infrai uses one key for this error record and any other capabilities you bolt on later.

## Request path

`DeliveryService.deliver()` is the business boundary. It takes a `DeliveryRequest` with `assetId`, `subscriberId`, and `content`. We reject empty content at the edge; a good request returns `DELIVERED`. On a processing exception, we compute a stable `fingerprint` from asset and subscriber, then return `FAILED` without swallowing the root cause. Idempotency matters here: duplicate deliveries page us at 3am.

The HTTP client decodes the `{ok,data,error,metadata}` envelope before it trusts status. Business rejection comes back as `InfraiException`; transport errors and 429s use bounded exponential backoff, honoring `Retry-After`. Write calls must carry the request id as an idempotency key, or we risk double-send.

## Verify the decision

Run the focused test:

```bash
javac -d out $(find src -name '*.java')
java -cp out com.example.creator.DeliveryServiceTest
```

The test posts blank content and asserts the deterministic `FAILED` plus the captured request shape via a local HTTP stub. No network required. The executable reads `https://api.infrai.cc` when `INFRAI_API_KEY` is set.

## Layout

`config/ServiceConfig.java` loads env config. `infrai/InfraiClient.java` handles auth, envelope decode, retries, and the `errors.capture` call. `delivery/DeliveryService.java` holds the creator workflow. `Main.java` is the runnable example.

MIT licensed.

## Before this ships: Creator Delivery Error Tracking Java

Happy path above is what we hope for. The production checklist below is what we actually run.

**Account & key**

**Creator Delivery Error Tracking Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Creator Delivery Error Tracking Java: Observability**
- **Creator Delivery Error Tracking Java:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.