# Stage Gate Prompt: Completed Phase

## Purpose

This prompt is triggered when moving a feature from in-progress (2_inprogress) to completed (3_completed).

## Instructions

When feature is complete:

1. **Create Summary Document**

   Create `summary.md`:
   ```markdown
   # {Feature Name} - Summary
   
   ## What Was Done
   Brief description of what was implemented.
   
   ## Key Achievements
   - Achievement 1
   - Achievement 2
   - Achievement 3
   
   ## Technical Implementation
   - Component 1: Description
   - Component 2: Description
   
   ## Impact
   - Impact 1
   - Impact 2
   
   ## Lessons Learned
   - Lesson 1
   - Lesson 2
   
   ## Next Steps
   - Next step 1
   - Next step 2
   ```

2. **Update README.md**

   Update status:
   ```markdown
   ## Status
   ✅ **Completed** - YYYY-MM-DD
   ```

3. **Finalize All Documents**

   - Ensure spec.md reflects final implementation
   - Ensure design.md reflects final design
   - Complete implementation_notes.md
   - Create summary.md

4. **Update Reference Documents**

   - Update `_reference/architecture.md` if architecture changed
   - Update `_reference/completed_features_log.md` with feature entry
   - Update any affected reference documents

5. **Archive if Needed**

   If feature is old or superseded:
   - Move to `docs/4_archived/{feature-name}`

## Completion Checklist

- [ ] All implementation tasks completed
- [ ] All tests passing
- [ ] Code reviewed and merged
- [ ] Documentation complete
- [ ] README.md updated with completion status
- [ ] summary.md created
- [ ] Reference documents updated
- [ ] completed_features_log.md updated

## Reference Document Updates

### Architecture Updates

If architecture changed, update `_reference/architecture.md`:
- New services/components
- Changed communication patterns
- Updated infrastructure
- New patterns implemented

### Completed Features Log

Add entry to `_reference/completed_features_log.md`:
```markdown
#### Feature Name
- **Feature**: Brief description
- **Status**: ✅ Completed
- **Location**: `docs/3_completed/feature-name/`
- **Key Components**: List of components
- **Date**: YYYY-MM-DD
```

## Post-Completion

After marking as completed:

1. **Review**
   - Conduct post-mortem if significant
   - Document lessons learned
   - Update best practices

2. **Communication**
   - Announce completion
   - Share summary with team
   - Update project status

3. **Follow-up**
   - Plan next related features
   - Address any technical debt
   - Update roadmap

## Questions to Answer

1. What was actually implemented?
2. What were the key achievements?
3. What was the technical approach?
4. What was the impact?
5. What lessons were learned?
6. What are the next steps?

