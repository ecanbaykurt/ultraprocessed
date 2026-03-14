# Requirements

## Functional Requirements

### Core scanning flow

- Capture ingredient labels with CameraX
- Extract text with ML Kit OCR
- Normalize ingredients before classification
- Produce a NOVA-aligned verdict
- Save scans locally when history is enabled

### Classifier requirements

The app must support three classification paths:

1. local rules
2. on-device LLM
3. direct API LLM using a user-provided key

### RulesClassifier

- required on all devices
- works fully offline
- handles obvious NOVA 1 and NOVA 4 patterns
- provides low-confidence fallback behavior

### OnDeviceLLMClassifier

- optional capability-based enhancement
- only used when supported by the device
- must fail gracefully
- must not block the base product from working

### ApiLLMClassifier

- uses direct HTTPS requests with OkHttp
- uses a user-provided provider and API key
- stores secrets locally only
- must remain optional

## Storage Requirements

### Room

Store:

- scan history
- OCR text
- normalized ingredients
- verdict
- explanation
- confidence
- markers
- engine used

### DataStore

Store:

- engine mode
- preferred provider
- history preference
- accessibility settings
- API fallback preference

### Secrets

Store:

- user API keys in Keystore-backed secure storage

## Non-Functional Requirements

- privacy-first defaults
- offline usability through rules engine
- clean fallback behavior across engines
- pluggable architecture so engines can evolve independently
- simple UI despite multiple inference modes
