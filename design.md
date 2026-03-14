# Design

## Vision

The Ultra Processed Detective should feel like a serious consumer health tool: minimal, trustworthy, and fast. The app must preserve the warmth and simplicity of early contributor explorations while maturing the product into something professional, privacy-first, and operationally clean.

## Product Principles

### 1. Privacy-first by default

- OCR happens locally.
- Rules classification always works offline.
- Images are not stored by default.
- Remote API use is optional and entirely user-controlled.

### 2. One app, multiple intelligence engines

Do not treat the classifier as one implementation. Treat it as a platform surface.

- `RulesClassifier`
- `OnDeviceLLMClassifier`
- `ApiLLMClassifier`

This keeps the experience stable while allowing different inference strategies behind the same UI.

### 3. Graceful degradation

The app should still be useful when:

- the user has no network
- the device does not support on-device GenAI
- no API key is configured
- OCR confidence is weak

That is why the rules engine is not optional. It is the foundation.

## End-to-End Flow

```text
camera -> OCR -> ingredient normalization -> choose engine -> verdict -> save locally
```

## Classifier Strategy

### RulesClassifier

This runs on every device and should always exist.

Responsibilities:

- detect obvious NOVA 4 additive markers
- detect obvious NOVA 1 ingredient patterns
- produce quick offline verdicts
- provide low-confidence fallback when richer engines are unavailable

This is the safety net for the whole application.

### OnDeviceLLMClassifier

This is the local AI path.

Use it as an enhancement layer, not as a hard dependency.

Responsibilities:

- refine ambiguous rule-based outcomes
- provide richer explanations locally
- operate only when device capability checks pass

Expected Android path:

- Gemini Nano / AICore or equivalent Android on-device GenAI tooling
- explicit capability detection before use
- automatic fallback when unsupported

### ApiLLMClassifier

This is the direct remote path using a user-provided API key.

Responsibilities:

- optionally provide higher-quality reasoning
- support multiple providers through a pluggable network layer
- avoid backend dependency for v1

Security model:

- key entered by the user
- stored locally with Keystore-backed protection
- never hardcoded into the shipped app

## Mode Selection

The app should expose three user-facing modes:

- `Auto`
- `On-device only`
- `API only`

### Auto mode behavior

Recommended orchestration:

1. OCR locally
2. Run `RulesClassifier`
3. If supported, use `OnDeviceLLMClassifier` for refinement
4. If allowed and configured, use `ApiLLMClassifier` for a second opinion or richer explanation
5. Save result locally

This produces the best balance of privacy, resilience, and quality.

## Data Architecture

### Room

Use Room for structured historical records:

- scan history
- OCR text
- normalized ingredient text
- verdicts
- highlighted ingredients
- confidence
- explanation text
- selected engine / source of verdict

### DataStore

Use DataStore for lightweight settings:

- engine mode
- preferred provider
- history enabled / disabled
- accessibility preferences
- API fallback preference

### Keystore-backed storage

Use Android Keystore-backed secure storage for:

- user API keys

## Package Design

```text
app/
  ui/
  camera/
  ocr/
  classify/
    Classifier.kt
    RulesClassifier.kt
    OnDeviceLLMClassifier.kt
    ApiLLMClassifier.kt
    ClassifierOrchestrator.kt
  storage/
    room/
    datastore/
    secrets/
  network/
  settings/
```

## Implementation Phases

### Phase 1

- Compose UI
- CameraX capture
- ML Kit OCR
- ingredient normalization
- `RulesClassifier`
- local result flow
- Room history

### Phase 2

- settings screen
- user API key entry
- Keystore-backed storage
- `ApiLLMClassifier`
- provider abstraction

### Phase 3

- `OnDeviceLLMClassifier`
- device capability detection
- Auto mode orchestration

This order is important because it produces a useful app early without blocking on GenAI device variability.

## UI Direction

The product should feel:

- formal
- calm
- efficient
- medically adjacent, but not clinical in a cold way

The interaction model should emphasize:

- large verdict states
- very low chrome
- one-handed scanning
- obvious rescan behavior
- clear explanation of why a product was flagged

## Design Reference Interpretation

The contributor concept is strong because it captures:

- a simple three-step workflow
- immediate feedback
- visual approachability

The production direction should preserve that clarity while improving:

- typography hierarchy
- spacing discipline
- card structure
- consistency of iconography
- confidence of the verdict presentation

The result should look less like a prototype and more like a finished consumer product.
