# Stage Gate Prompt: In-Progress Phase

## Purpose

This prompt is triggered when moving a feature from planning (1_planning) into in-progress (2_inprogress) or when actively working on a feature.

## Instructions

When starting implementation:

1. **Update README.md**

   Update status and add task list:
   ```markdown
   ## Status
   🚧 **In Progress**
   
   ## Implementation Tasks
   - [ ] Task 1
   - [ ] Task 2
   - [ ] Task 3
   ```

2. **Create Implementation Notes**

   Create `implementation_notes.md`:
   ```markdown
   # Implementation Notes: {Feature Name}
   
   ## Progress
   - Started: YYYY-MM-DD
   - Current Status: Description
   
   ## Decisions Made
   - Decision 1: Rationale
   - Decision 2: Rationale
   
   ## Issues Encountered
   - Issue 1: Description and resolution
   - Issue 2: Description and resolution
   
   ## Code Changes
   - File 1: Description of changes
   - File 2: Description of changes
   
   ## Testing
   - Test 1: Status
   - Test 2: Status
   ```

3. **Update Documents as Needed**

   - Update `spec.md` if requirements change
   - Update `design.md` if design evolves
   - Document decisions in `implementation_notes.md`

4. **Track Progress**

   - Update task list in README.md
   - Update implementation_notes.md regularly
   - Document any blockers or issues

## Daily Updates

During implementation, update:

- Task completion status
- New decisions made
- Issues encountered and resolutions
- Code changes made
- Test progress

## Code Review Checklist

Before code review:

- [ ] All tasks completed
- [ ] Code follows project conventions
- [ ] Tests written and passing
- [ ] Documentation updated
- [ ] Implementation notes complete

## Moving to Completed

When implementation is complete:

1. Complete all tasks in README.md
2. Finalize implementation_notes.md
3. Update spec.md and design.md with final state
4. Move feature directory: `mv docs/2_inprogress/{feature-name} docs/3_completed/{feature-name}`
5. Follow `STAGE_GATE_PROMPT_COMPL.md`

## Blockers

If blocked:

1. Document blocker in implementation_notes.md
2. Update README.md with blocker status
3. Escalate if needed
4. Consider moving back to planning if major changes needed

## Questions to Answer

1. What progress has been made?
2. What decisions were made?
3. What issues were encountered?
4. What code changes were made?
5. What tests were written?
6. What remains to be done?

