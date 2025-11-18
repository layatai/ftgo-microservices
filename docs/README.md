# FTGO Microservices Documentation

This directory contains all project documentation organized using a stage-gate workflow.

## Directory Structure

```
docs/
├── 0_backlog/              # Raw ideas and feature requests
├── 1_planning/             # Features being planned
│   └── STAGE_GATE_PROMPT_PLAN.md
├── 2_inprogress/           # Features being implemented
│   └── STAGE_GATE_PROMPT_PROG.md
├── 3_completed/            # Completed features
│   ├── STAGE_GATE_PROMPT_COMPL.md
│   └── orchestration-based-saga/
├── 4_archived/             # Old or superseded features
├── _reference/              # Reference documentation
│   ├── architecture.md
│   ├── api_documentation.md
│   ├── code_structure.md
│   ├── development_guide.md
│   ├── patterns.md
│   ├── saga_implementation.md
│   ├── ai_context.md
│   ├── completed_features_log.md
│   └── style_guide.md
└── _templates/              # Documentation templates
    ├── feature_spec_template.md
    └── design_doc_template.md
```

## Workflow

### 1. Backlog (0_backlog)

Raw ideas and feature requests start here. Create a simple markdown file describing the idea.

### 2. Planning (1_planning)

When ready to plan a feature:
- Create feature directory
- Use `STAGE_GATE_PROMPT_PLAN.md` for guidance
- Create `README.md`, `spec.md`, and `design.md`
- Use templates from `_templates/`

### 3. In Progress (2_inprogress)

When starting implementation:
- Move feature from planning
- Use `STAGE_GATE_PROMPT_PROG.md` for guidance
- Create `implementation_notes.md`
- Update tasks regularly

### 4. Completed (3_completed)

When feature is complete:
- Move feature from in-progress
- Use `STAGE_GATE_PROMPT_COMPL.md` for guidance
- Create `summary.md`
- Update reference documents

### 5. Archived (4_archived)

Old or superseded features can be moved here for historical reference.

## Reference Documentation

The `_reference/` directory contains comprehensive documentation:

- **architecture.md**: System architecture and design
- **api_documentation.md**: Complete API reference
- **code_structure.md**: Code organization and structure
- **development_guide.md**: Development setup and workflows
- **patterns.md**: Microservices patterns reference
- **saga_implementation.md**: Saga pattern implementation guide
- **ai_context.md**: Context for AI assistants
- **completed_features_log.md**: Log of completed features
- **style_guide.md**: Documentation style guide

## Templates

The `_templates/` directory contains templates for:

- **feature_spec_template.md**: Feature specification template
- **design_doc_template.md**: Design document template

## Quick Start

### Starting a New Feature

1. Create idea in `0_backlog/feature-idea.md`
2. When ready, move to planning:
   ```bash
   mkdir -p docs/1_planning/feature-name
   # Follow STAGE_GATE_PROMPT_PLAN.md
   ```

### Working on a Feature

1. Move to in-progress:
   ```bash
   mv docs/1_planning/feature-name docs/2_inprogress/feature-name
   # Follow STAGE_GATE_PROMPT_PROG.md
   ```

### Completing a Feature

1. Move to completed:
   ```bash
   mv docs/2_inprogress/feature-name docs/3_completed/feature-name
   # Follow STAGE_GATE_PROMPT_COMPL.md
   ```

## Best Practices

1. **Keep Documentation Updated**: Update docs as you work
2. **Use Templates**: Start with templates for consistency
3. **Cross-Reference**: Link related documents
4. **Update References**: Keep reference docs current
5. **Document Decisions**: Record important decisions
6. **Track Progress**: Update task lists regularly

## Stage Gate Prompts

Each stage has a prompt file that guides the process:

- `1_planning/STAGE_GATE_PROMPT_PLAN.md`: Planning phase guidance
- `2_inprogress/STAGE_GATE_PROMPT_PROG.md`: Implementation phase guidance
- `3_completed/STAGE_GATE_PROMPT_COMPL.md`: Completion phase guidance

## Related Documentation

- Main project README: `../README.md`
- Architecture: `_reference/architecture.md`
- Development Guide: `_reference/development_guide.md`

