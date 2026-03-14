# JVMAI - Future Enhancements / TODOs

This document tracks potential features and enhancements for the JVMAI diagnostic tool to make it a more powerful automated troubleshooting platform for JVM applications.

## Planned Features

- [ ] **Generate Flame Graph**
  - Integrate tools like `async-profiler` or Java Flight Recorder (JFR) to collect stack traces over a period of time.
  - Automatically generate and serve or output a Flame Graph (HTML/SVG) for CPU or Allocation profiling.
  - Provide AI analysis on the generated flame profile to identify hot paths.

- [ ] **Debug Remote JVM**
  - Allow attaching to a remote JVM using JMX over RMI, SSH tunneling, or a lightweight agent running on the target machine.
  - Extend the existing `--pid` functionality to support remote connections (e.g., `--host <host> --port <jmx_port>`).

- [ ] **Method Sampling & Bottleneck Troubleshooting**
  - Implement a mechanism to sample execution times of a specific method (e.g., using bytecode instrumentation/Java Agents or `async-profiler`).
  - Send the sampled data to the LLM to analyze and troubleshoot specific method-level bottlenecks.

- [ ] **Resource Leak Detection**
  - Monitor OS-level resources for the target JVM (e.g., file descriptors, sockets, native memory tracking).
  - Enhance the memory agent to cross-reference heap data with native resource limits and usage to spot resource leaks beyond just heap memory.
