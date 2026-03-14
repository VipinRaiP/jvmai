# JVMAI - AI-Assisted JVM Diagnostic Tool

JVMAI is a CLI-first AI-assisted JVM diagnostic tool built with Java 21, Picocli, and Gradle. It attaches directly to a running JVM process, collects runtime diagnostics (thread dumps, memory usage, JVM metadata) programmatically using JMX and the Attach API, performs deterministic rule-based analysis, and optionally uses local Large Language Models (LLMs) via Ollama for contextual reasoning and remediation suggestions.

## Features

- **Local JVM Attachment:** Automatically attach to any local JVM process using its PID.
- **Diagnostics Collection:** Extracts thread counts, states, deadlocks, heap/non-heap memory usage, and garbage collection metrics.
- **Rule Engine:** Built-in heuristics for detecting:
  - Deadlocks
  - High Thread Count
  - High Thread Contention
  - High Heap Memory Usage
  - Garbage Collection Thrashing
- **LangGraph Multi-Agent Reasoning (Optional):** Sends securely redacted diagnostics through a `langgraph4j` `StateGraph` powered by local LLMs via Ollama. It utilizes specialized "Thread" and "Memory" Agents that analyze specific metrics, and a "Synthesis" Lead Agent that outputs a cohesive root cause analysis and actionable recommendations.
- **Interactive Q&A:** Chat directly with the LLM about the collected JVM state to dive deeper into specific issues.
- **Multiple Output Formats:** View reports as plain text, Markdown, or structured JSON.

## Prerequisites

- **Java 21** or higher.
- **Ollama** installed and running locally on `http://localhost:11434` (if you plan to use the LLM features).
  - You will need to pull a model, e.g., `ollama pull llama3.2`.

## Build Instructions

To build the application and generate the distribution package, use the provided Gradle wrapper:

```bash
./gradlew build
```

## Usage

You can run the CLI via the Gradle `run` task:

```bash
# Basic diagnosis with default local LLM (llama3) and text output
./gradlew run --args="diagnose --pid <TARGET_PID>"

# Diagnosis by connecting to a remote JVM via JMX
./gradlew run --args="diagnose --host <REMOTE_HOST> --port <JMX_PORT>"

# Disable the LLM completely, rely only on the deterministic Rule Engine
./gradlew run --args="diagnose --pid <TARGET_PID> --no-llm"

# Output as Markdown and redact sensitive string values 
./gradlew run --args="diagnose --pid <TARGET_PID> --output markdown --redact"

# Test against a specific model
./gradlew run --args="diagnose --pid <TARGET_PID> --model llama3.2"

# Enter Interactive Q&A loop after generating the report
./gradlew run --args="diagnose --pid <TARGET_PID> --interactive"
```

### Supported Flags

| Flag | Description | Default |
|---|---|---|
| `-p`, `--pid` | Target JVM Process ID (Required for local attachment). | - |
| `--host` | Target JVM Hostname (Required for remote JMX attachment). | - |
| `--port` | Target JVM JMX Port (Required for remote JMX attachment). | - |
| `--model` | LLM model name to interact with via local Ollama. | `llama3` |
| `--output` | Format of the generated report (`text`, `markdown`, or `json`). | `text` |
| `--no-llm` | Disables the LangGraph AI reasoning layer. Only the static Rule Engine results are shown. | `false` |
| `--interactive` | Keeps the CLI open after the initial report, allowing you to ask follow-up questions to the LLM. | `false` |
| `--redact` | Attempts to scrub obvious secrets, IP addresses, and UUIDs from thread/lock names before sending data to the LLM. | `false` |
| `--timeout` | Connection timeout (in seconds) for the HTTP request to the local LLM. | `30` |

## Example Output

When running the diagnosis, JVMAI outputs a concise summary of the JVM state along with rule evaluations and LLM insights.

```text
Attaching to JVM with PID: 1234...
Successfully attached to JVM. Collecting diagnostics...
Diagnostics collected for JVM: OpenJDK 64-Bit Server VM (21.0.1+12-29)

Initializing Multi-Agent AI System via LangGraph4j (llama3.2)...
Starting Multi-Agent Diagnostic Graph (powered by LangGraph4j)...
  [Agent:Thread] Analyzing thread dump...
  [Agent:Thread] Complete.
  [Agent:Memory] Analyzing heap and GC usage...
  [Agent:Memory] Complete.
  [Agent:Synthesis] Synthesizing reports into Root Cause Analysis...
  [Agent:Synthesis] Complete.
Multi-Agent Graph Complete.

=== JVMAI Diagnostic Report ===

JVM Details:
  Name: OpenJDK 64-Bit Server VM
  Version: 21.0.1+12-29
  Uptime (ms): 840210
  GC Algo: G1 Young Generation

Resource Summary:
  Thread Count: 142
  Heap Usage: 89.1%

Rule Analysis:
  [❌] High Heap Usage: Heap usage is at 89.1%, exceeding threshold of 85.0%.
  [✅] Deadlock Detection: No deadlocked threads found.
  [✅] High Thread Count: Thread count is normal (142).
  [⚠️] GC Overhead Warning: 14.2% of JVM uptime has been spent in Garbage Collection.

AI Reasoning & Recommendations:
{
  "rootCauseAnalysis": {
    "description": "The JVM is encountering frequent Garbage Collection cycles without reclaiming sufficient heap space, causing severe memory pressure.",
    "confidenceLevel": 92,
    "severity": "High"
  },
  "recommendedActions": [
    {
      "action": "Capture a heap dump to identify memory leaks.",
      "probability": 90
    },
    {
      "action": "Evaluate increasing the -Xmx parameter.",
      "probability": 85
    }
  ]
}
```

## Project Structure
- `dev.jvmai.diagnostic`: Contains the JVM logic to attach to the target process and extract metrics.
- `dev.jvmai.analysis`: Deterministic Rule Engine containing rules and severity logic.
- `dev.jvmai.format`: Handles JSON, Text, and Markdown output generation.
- `dev.jvmai.llm`: `langchain4j-ollama` HTTP Client and Security data redactor.
- `dev.jvmai.llm.graph`: Multi-Agent `langgraph4j` nodes (Thread, Memory, Synthesis) and `StateGraph` execution.

## Credits

This project was proudly **"vibe-coded"**—a seamless collaboration between human intuition and Artificial Intelligence. 🚀🤖