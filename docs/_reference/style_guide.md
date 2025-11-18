# Documentation Style Guide

## Purpose

This style guide ensures consistency across all documentation in the FTGO microservices project.

## General Principles

1. **Clarity**: Write clearly and concisely
2. **Completeness**: Include all necessary information
3. **Consistency**: Use consistent formatting and terminology
4. **Accessibility**: Make documentation accessible to all team members

## Markdown Formatting

### Headers

- Use `#` for main title (H1)
- Use `##` for major sections (H2)
- Use `###` for subsections (H3)
- Use `####` for sub-subsections (H4)

### Code Blocks

- Use triple backticks with language identifier:
  ```java
  public class Example {
      // code here
  }
  ```

### Lists

- Use `-` for unordered lists
- Use `1.` for ordered lists
- Use `- [ ]` for task lists
- Use `- [x]` for completed tasks

### Emphasis

- Use `**bold**` for strong emphasis
- Use `*italic*` for emphasis
- Use `` `code` `` for inline code

### Links

- Use `[text](url)` for external links
- Use `[text](relative/path)` for internal links

## Terminology

### Service Names

- Use full service names: "Customer Service", "Order Service"
- Use code references: `ftgo-customer-service`, `ftgo-order-service`

### Technical Terms

- **Saga**: Always capitalize when referring to the pattern
- **Orchestrator**: The central coordinator (SagaManager)
- **Step**: Individual saga step
- **Compensation**: Compensating transaction

### Code References

- Class names: `SagaManager`, `OrderService`
- Package names: `com.ftgo.orderservice.saga`
- File paths: `ftgo-order-service/src/main/java/...`
- Database names: `ftgo_order`, `ftgo_customer`

## Document Structure

### Feature Documentation

Each feature should have:

1. **README.md**: Overview, status, summary
2. **spec.md**: Detailed specification
3. **design.md**: Design decisions and architecture
4. **implementation_notes.md**: Implementation details (for in-progress)
5. **summary.md**: Final summary (for completed)

### Reference Documentation

Reference docs should be:
- Comprehensive
- Well-organized
- Cross-referenced
- Updated regularly

## Code Examples

### Java Code

- Include package declarations
- Include imports when relevant
- Use meaningful variable names
- Add comments for complex logic

### Configuration Examples

- Use YAML format for Spring Boot configs
- Include comments for clarity
- Show complete sections when relevant

### SQL Examples

- Use proper SQL formatting
- Include table names
- Add comments for complex queries

## Diagrams

### Mermaid Diagrams

- Use Mermaid for flowcharts, class diagrams, sequence diagrams
- Keep diagrams simple and readable
- Include descriptions

### Text Diagrams

- Use ASCII art for simple diagrams
- Keep formatting consistent

## Status Indicators

- ✅ Completed
- 🚧 In Progress
- 📋 Planned
- ❌ Cancelled
- ⚠️ Blocked

## Date Formats

- Use ISO 8601 format: `YYYY-MM-DD`
- For months: `November 2024`
- For ranges: `November 2024 - December 2024`

## File Naming

- Use lowercase with underscores: `feature_name.md`
- Use descriptive names
- Avoid special characters
- Keep names concise

## Sections

### Standard Sections

1. **Overview**: High-level description
2. **Status**: Current status
3. **Summary**: Brief summary
4. **Details**: Detailed information
5. **Implementation**: Implementation details
6. **Testing**: Testing information
7. **References**: Related documents

## Best Practices

1. **Update Regularly**: Keep documentation up to date
2. **Cross-Reference**: Link to related documents
3. **Examples**: Include practical examples
4. **Diagrams**: Use diagrams for complex concepts
5. **Code**: Include code examples when relevant
6. **Status**: Keep status indicators current

## Review Checklist

Before marking documentation as complete:

- [ ] All sections are filled
- [ ] Code examples are correct
- [ ] Links are working
- [ ] Diagrams are clear
- [ ] Status is accurate
- [ ] Cross-references are correct
- [ ] Formatting is consistent

