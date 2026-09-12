# Planner Improvements

This change makes the deterministic planner retain tool intent at subtask level.

Each decomposed subtask is classified independently and mapped to an available
specialized tool when possible. The first available specialized tool remains the
plan's primary `toolId` for compatibility, while `subtaskToolIds` preserves the
full mapping for future multi-tool execution.
