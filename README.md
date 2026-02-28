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
- **LLM Reasoning (Optional):** Sends securely redacted diagnostics to a local LLM to get a root cause analysis and actionable recommendations.
- **Interactive Q&A:** Chat directly with the LLM about the collected JVM state to dive deeper into specific issues.
- **Multiple Output Formats:** View reports as plain text, Markdown, or structured JSON.

## Prerequisites

- **Java 21** or higher.
- **Ollama** installed and running locally on `http://localhost:11434` (if you plan to use the LLM features).
  - You will need to pull a model, e.g., `ollama pull llama3`.

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

# Disable the LLM completely, rely only on the deterministic Rule Engine
./gradlew run --args="diagnose --pid <TARGET_PID> --no-llm"

# Output as Markdown and redact sensitive string values 
./gradlew run --args="diagnose --pid <TARGET_PID> --output markdown --redact"

# Enter Interactive Q&A loop after generating the report
./gradlew run --args="diagnose --pid <TARGET_PID> --interactive"
```

### Supported Flags

| Flag | Description | Default |
|---|---|---|
| `-p`, `--pid` | **(Required)** Target JVM Process ID. | - |
| `--model` | LLM model name to interact with via local Ollama. | `llama3` |
| `--output` | Format of the generated report (`text`, `markdown`, or `json`). | `text` |
| `--no-llm` | Disables the LLM reasoning layer. Only the Rule Engine results are shown. | `false` |
| `--interactive` | Keeps the CLI open after the initial report, allowing you to ask follow-up questions to the LLM. | `false` |
| `--redact` | Attempts to scrub obvious secrets, IP addresses, and UUIDs from thread/lock names before sending data to the LLM. | `false` |
| `--timeout` | Connection timeout (in seconds) for the HTTP request to the local LLM. | `30` |

## Project Structure
- `dev.jvmai.diagnostic`: Contains the JVM logic to attach to the target process and extract metrics.
- `dev.jvmai.analysis`: Deterministic Rule Engine containing rules and severity logic.
- `dev.jvmai.format`: Handles JSON, Text, and Markdown output generation.
- `dev.jvmai.llm`: LLM HTTP Client and Security data redactor.

## TODO / Future Work
- **LangGraph Integration:** Explore integrating an agentic workflow framework (like LangGraph via Python sidecar, or LangChain4j for a Java-native approach) to support multi-agent diagnostic RAG cycles and automated remediation execution.