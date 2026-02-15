# OpenAI Moderation API - Guardrail Implementation

## Overview
Successfully integrated OpenAI Moderation API as a multi-layer safety guardrail system.

## Architecture

### Defense-in-Depth Strategy (2 Layers)

**Layer 1: Input Guardrail** (Entry Point)
- Location: `ChatController`
- Validates user input BEFORE processing
- Blocks harmful content at the gate
- Prevents wasted LLM calls on policy violations

**Layer 2: Output Guardrail** (Exit Point)
- Location: `HallucinationGuard` → `SafetyValidator`
- Validates AI responses BEFORE returning to user
- Combines OpenAI Moderation + custom keyword filtering
- Ensures generated content is safe

## Components Created

### 1. `ModerationResult.java`
- Model class for OpenAI Moderation API response
- Parses categories: hate, harassment, self-harm, sexual, violence
- Provides helper methods to identify flagged content

### 2. `OpenAiModerationService.java`
- REST client for OpenAI Moderation API endpoint
- Handles API calls with error resilience (fail-open)
- Configurable enable/disable via properties
- Free to use (no additional cost)

### 3. `InputGuardrail.java`
- Validates user input before orchestration
- Throws `AgentException` on policy violations
- Can be disabled independently

### 4. `SafetyValidator.java` (Updated)
- **Primary**: OpenAI Moderation API check
- **Secondary**: Domain-specific keyword filtering
- Both layers must pass

### 5. `ChatController.java` (Updated)
- Added input validation call **inside reactive stream** (Flux.defer)
- All validation errors handled reactively (no 400/403 HTTP errors for SSE)
- Gracefully handles errors in reactive stream with `.onErrorResume()`
- Returns user-friendly error messages as part of SSE stream
- Frontend never sees HTTP error codes - only friendly messages

### 6. `GlobalExceptionHandler.java` (New)
- Global exception handler for WebFlux
- Converts AgentException to proper HTTP status codes
- Maps error codes: INPUT_SAFETY_VIOLATION → 400, SAFETY_GUARDRAIL_BLOCKED → 403
- Provides structured error responses

### 7. `AgentOrchestrator.java` (Updated)
- Wrapped processing in Flux.defer() for reactive error handling
- Converts synchronous exceptions to reactive error signals
- Enables proper error propagation through stream

## Configuration

```properties
# Enable/disable moderation API calls
agentic.guardrails.moderation.enabled=true

# Enable/disable input validation
agentic.guardrails.input.enabled=true
```

## Content Categories Checked

The OpenAI Moderation API checks for:
- **hate**: Content expressing hatred toward groups
- **hate/threatening**: Hateful content with violence
- **harassment**: Content meant to harass, threaten, or bully
- **harassment/threatening**: Harassment with violence/harm
- **self-harm**: Content promoting self-harm
- **self-harm/intent**: Content where user expresses intent
- **self-harm/instructions**: Content with instructions
- **sexual**: Sexual content
- **sexual/minors**: Sexual content involving minors
- **violence**: Content depicting violence
- **violence/graphic**: Graphic violent content

## Testing

### 1. Test Input Guardrail (Harmful User Input)

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "I want to hurt people", "sessionId": "test-session"}'
```

**Expected**: Error message in stream (not a 400 status):
```
data:❌ Safety Check Failed: Your message violates our content policy. Please rephrase and try again.
```

**How it works:**
1. User sends harmful message
2. InputGuardrail calls OpenAI Moderation API
3. Content flagged for "violence" category
4. AgentException thrown inside reactive stream
5. Error caught by `.onErrorResume()` and converted to friendly message
6. Frontend receives message in SSE stream format (not JSON error)

### 2. Test Output Guardrail (AI Response Filtering)

Try queries that might generate unsafe operational commands:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "How do I delete all production data?", "sessionId": "test-session"}'
```

**Expected**: Error message in stream (not a 403 status):
```
data:❌ Safety Check Failed: Response contains unsafe operational guidance.
```

**How it works:**
1. User input passes OpenAI Moderation (not flagged as harmful)
2. LLM generates a response about deleting data
3. SafetyValidator detects keyword "delete all" in output
4. AgentException thrown inside reactive stream
5. Error caught by `.onErrorResume()` and converted to friendly message
6. Response blocked, user sees safety message in chat stream

### 3. Test Normal Operations (Should Pass)

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "How do I restart a Kubernetes pod?", "sessionId": "test-session"}'
```

**Expected**: Normal streamed response with runbook steps (RB-001):
```
data:To restart a Kubernetes pod, follow these steps:
data:1. Check pod status using kubectl get pods
data:2. Inspect logs using kubectl logs <pod>
...
```

### 4. Verify Logs

Check logs for moderation activity:
```powershell
Get-Content logs\agent-platform.log | Select-String "Moderation"
Get-Content logs\agent-platform.log | Select-String "flagged"
```

## API Usage & Cost

- **OpenAI Moderation API is FREE** (no cost per request)
- Endpoint: `https://api.openai.com/v1/moderations`
- Model: `text-moderation-latest` (auto-updated)
- Rate limit: Same as other OpenAI APIs

## Error Handling

The implementation uses **fail-open** pattern for API errors:
- If Moderation API is unavailable → allows content (logs error)
- If API returns error → allows content (logs error)
- This prevents service outages from blocking all traffic

### Reactive Error Handling

Since the chat endpoint uses **Server-Sent Events (SSE)** streaming, errors are handled gracefully within the stream:
The Problem:**
- SSE endpoints expect `Content-Type: text/event-stream`
- Traditional exception handlers return JSON with HTTP error codes (400, 403, 500)
- Frontend fetch API fails on non-2xx status codes
- User sees generic "Chat request failed with status 400" instead of helpful message

**The Solution:**
- Move ALL validation (input + output) inside reactive stream using `Flux.defer()`
- Catch ALL exceptions with `.onErrorResume()` before they escape
- Convert exceptions to user-friendly messages in SSE format
- Frontend always receives 200 OK with streaming data
- Error messages displayed as chat messages: `❌ Safety Check Failed: <reason>`

**Code Pattern:**
```java
public Flux<String> chat(@RequestBody ChatRequest request) {
    return Flux.defer(() -> {
        // Input validation inside reactive context
        inputGuardrail.validateInput(request.message());
        
        // Processing (may throw output validation errors)
        return orchestrator.process(request);
    })
    .onErrorResume(ex -> {
        // Convert ANY exception to friendly message
        return Flux.just("❌ Safety Check Failed: " + ex.getMessage());
    });
}
```

**Result:**
- No HTTP errors for guardrail violations
- Clean user experience in chat widget
- Errors appear as normal chat messages
- Frontend doesn't need special error handlingas part of the chat
- No 500 errors, clean user experience

### HTTP Status Codes

When using the REST API (non-streaming), these status codes are returned:

- `400 Bad Request` - INPUT_SAFETY_VIOLATION (user input blocked)
- `403 Forbidden` - SAFETY_GUARDRAIL_BLOCKED (AI response blocked)
- `422 Unprocessable Entity` - GROUNDING_GUARDRAIL_BLOCKED (hallucination detected)
- `503 Service Unavailable` - MODERATION_ERROR (API unavailable, fail-closed mode)
- `502 Bad Gateway` - TOOL_EXECUTION_FAILED (external tool error)

### Changing to Fail-Closed (More Strict)

To change to **fail-closed** (block on API errors):
```java
// In OpenAiModerationService.moderate()
catch (Exception e) {
    log.error("Moderation API failed. BLOCKING content.", e);
    throw new AgentException("MODERATION_ERROR", "Unable to verify content safety");
}
```

## Disabling Guardrails

For testing or specific environments:

```properties
# Disable all moderation checks
agentic.guardrails.moderation.enabled=false
agentic.guardrails.input.enabled=false
```

## Flow Diagram

```
User Input
    ↓
InputGuardrail (OpenAI Moderation)
    ↓
AgentOrchestrator
    ↓
Planner → RAG → Tools → ResponseComposer
    ↓
HallucinationGuard
    ↓
SafetyValidator (OpenAI Moderation + Keywords)
    ↓
Response to User
```

## Monitoring Recommendations

1. **Log Analysis**: Track flagged content frequency
2. **Metrics**: Count of input/output rejections
3. **Alerts**: Spike in policy violations may indicate attack
4. **Review**: Periodic review of blocked content for false positives

## Next Steps

### Optional Enhancements:
1. **Custom Thresholds**: Use `category_scores` for fine-tuned control
2. **Audit Trail**: Log all moderation decisions to database
3. **User Feedback**: Allow users to appeal false positives
4. **A/B Testing**: Test different guardrail configurations
5. **Analytics Dashboard**: Visualize safety metrics

## References

- [OpenAI Moderation API Docs](https://platform.openai.com/docs/guides/moderation)
- [Spring RestClient](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
