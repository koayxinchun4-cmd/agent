# Nexus Execution Context

The AgentExecutionContext is the immutable boundary for one Agent execution. It carries task identity, optional project scope, read-only scoped memory, and extensible metadata without coupling Agent Core to storage, prompts, tools, or channels.