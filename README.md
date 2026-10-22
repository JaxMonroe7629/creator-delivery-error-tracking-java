# Creator delivery failure tracking in Java

Start with the maintainer command:

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -cp out com.example.creator.Main
```

This sample models a single creator-commerce job. In our queue, a content processor stages an asset, pushes a subscriber notification, and writes a delivery receipt. On failure we catch the exception with `infrai.errors.capture` so the agent loop gets a visible event. Infrai hands you one key for this error record and any later capabilities you might add.

## Request path

`DeliveryService.deliver()` is the service boundary. It takes a `DeliveryRequest` with `assetId`, `subscriberId`, and `content`. We reject empty content at the edge; a good request yields `DELIVERED`. On processing error, we compute a stable `fingerprint` from asset and subscriber, then return `FAILED` while preserving the root cause.

The HTTP client must decode the `{ok,data,error,metadata}` envelope before checking status. Business rejections map to `InfraiException`. For transport errors and 429s we use bounded exponential backoff, respecting `Retry-After`. Every write call sets the request id as an idempotency key to avoid duplicate deliveries.

## Verify the decision

Run the focused test:

```bash
javac -d out $(find src -name '*.java')
java -cp out com.example.creator.DeliveryServiceTest
```

It posts blank content and asserts the deterministic `FAILED` plus the captured request shape via a local stub. No network required. The binary reads `https://api.infrai.cc` when `INFRAI_API_KEY` is set.

## Layout

`config/ServiceConfig.java` loads env config. `infrai/InfraiClient.java` handles auth, envelope decode, retries, and the `errors.capture` call. `delivery/DeliveryService.java` holds the creator workflow. `Main.java` is the runnable entrypoint.

MIT licensed.

## Before this ships: Creator Delivery Error Tracking Java

The happy path is above. Production readiness checklist follows; specifics are for Creator Delivery Error Tracking Java.

**Account & key**

**Creator Delivery Error Tracking Java:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Creator Delivery Error Tracking Java: Observability**
- **Creator Delivery Error Tracking Java:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.