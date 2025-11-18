# Stage Gate Prompt: Planning Phase

## Purpose

This prompt is triggered when moving a feature from backlog (0_backlog) into planning (1_planning).

## Instructions

When a feature is ready for planning:

1. **Create Feature Directory**
   ```bash
   mkdir -p docs/1_planning/{feature-name}
   ```

2. **Create Initial Documents**
   - `README.md`: Feature overview and status
   - `spec.md`: Detailed specification (use template from `_templates/feature_spec_template.md`)
   - `design.md`: Design document (use template from `_templates/design_doc_template.md`)

3. **Populate README.md**

   Use this structure:
   ```markdown
   # {Feature Name}
   
   ## Status
   🚧 **Planning**
   
   ## Overview
   Brief description of the feature.
   
   ## Goals
   - Goal 1
   - Goal 2
   
   ## Documents
   - [Specification](spec.md)
   - [Design](design.md)
   
   ## Next Steps
   - [ ] Complete specification
   - [ ] Complete design document
   - [ ] Review with team
   - [ ] Move to in-progress
   ```

4. **Fill Specification Template**

   Copy `_templates/feature_spec_template.md` to `spec.md` and fill in:
   - Overview
   - Requirements (functional and non-functional)
   - User stories
   - Technical requirements
   - Dependencies
   - Risks
   - Testing strategy
   - Success criteria

5. **Fill Design Template**

   Copy `_templates/design_doc_template.md` to `design.md` and fill in:
   - Architecture overview
   - Component design
   - Design patterns
   - Data model
   - API design
   - Event design
   - Database schema
   - Sequence/class diagrams
   - Error handling
   - Security/performance considerations
   - Alternatives considered

6. **Update Reference Documents**

   If the feature affects architecture:
   - Update `_reference/architecture.md`
   - Update `_reference/completed_features_log.md` (add as planned)

## Checklist

Before moving to in-progress:

- [ ] Feature directory created
- [ ] README.md created with overview
- [ ] spec.md completed with all sections
- [ ] design.md completed with all sections
- [ ] Team review completed
- [ ] Dependencies identified
- [ ] Risks assessed
- [ ] Success criteria defined

## Questions to Answer

1. What problem does this feature solve?
2. Who are the stakeholders?
3. What are the technical requirements?
4. What services/components are affected?
5. What are the dependencies?
6. What are the risks?
7. How will success be measured?

## Moving to In-Progress

When planning is complete and approved:

1. Move feature directory: `mv docs/1_planning/{feature-name} docs/2_inprogress/{feature-name}`
2. Update README.md status to "In Progress"
3. Add implementation_notes.md
4. Follow `STAGE_GATE_PROMPT_PROG.md`

