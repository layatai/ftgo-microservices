# Auto-Reload Configuration

This document describes the auto-reload setup for all FTGO microservices projects.

## Overview

Auto-reload is configured for both backend (Spring Boot) and frontend (React/Vite) projects to enable fast development cycles without manual restarts.

## Backend Services (Spring Boot)

### Spring Boot DevTools

All microservices have **Spring Boot DevTools** configured for automatic application restart when code changes are detected.

#### How It Works

- **Automatic Restart**: When you save a Java file, DevTools detects classpath changes and automatically restarts the application
- **LiveReload**: Browser pages can automatically refresh when backend changes are detected (requires LiveReload browser extension)
- **Fast Restart**: Only restarts the application context, not the full JVM, making restarts much faster

#### Services with DevTools

- ✅ Customer Service
- ✅ Restaurant Service
- ✅ Order Service
- ✅ Kitchen Service
- ✅ Delivery Service
- ✅ Accounting Service
- ✅ API Gateway

#### Configuration

DevTools is added as an optional dependency in each service's `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

#### How to Use

1. **Start services** using VS Code launch configurations or manually
2. **Make code changes** to any Java file
3. **Save the file** - DevTools will automatically detect the change
4. **Wait for restart** - The service will restart automatically (usually takes 5-10 seconds)

#### What Triggers Restart

- Changes to Java source files (`.java`)
- Changes to resources (`.yml`, `.properties`, `.xml`)
- Changes to templates (if using Thymeleaf, etc.)

#### What Doesn't Trigger Restart

- Changes to test files (`.java` in `src/test`)
- Changes to static resources (unless configured)

#### Disabling Auto-Restart

If you need to disable auto-restart temporarily:

1. Set environment variable: `SPRING_DEVTOOLS_RESTART_ENABLED=false`
2. Or add to `application.yml`:
   ```yaml
   spring:
     devtools:
       restart:
         enabled: false
   ```

#### LiveReload Browser Integration

To enable automatic browser refresh:

1. Install LiveReload browser extension (Chrome, Firefox, etc.)
2. Enable the extension
3. When backend restarts, the browser will automatically refresh

## Frontend (React + Vite)

### Vite Hot Module Replacement (HMR)

The frontend uses **Vite's built-in HMR** for instant updates without full page reloads.

#### How It Works

- **Instant Updates**: Changes to React components update immediately in the browser
- **State Preservation**: Component state is preserved during updates
- **Fast Refresh**: Only the changed component re-renders, not the entire page

#### Configuration

HMR is automatically enabled when running `npm run dev` in the frontend directory.

#### How to Use

1. **Start the frontend**: `npm run dev` or use VS Code launch configuration
2. **Make changes** to any React component, CSS, or TypeScript file
3. **See changes instantly** - No manual refresh needed

#### What Updates Instantly

- React component changes (`.tsx`, `.jsx`)
- CSS changes (`.css`, Tailwind classes)
- TypeScript changes (`.ts`)
- Asset changes (images, etc.)

## Best Practices

### Backend Development

1. **Use IDE auto-save** or save frequently to trigger restarts
2. **Watch the console** for restart notifications
3. **Be patient** - First restart after startup may take longer
4. **Check logs** if restart fails

### Frontend Development

1. **Keep browser DevTools open** to see HMR updates
2. **Use React DevTools** for component debugging
3. **Check browser console** for any HMR errors

## Troubleshooting

### Backend Not Restarting

1. **Check DevTools is included**: Verify `spring-boot-devtools` is in `pom.xml`
2. **Rebuild project**: Run `mvn clean install` to ensure classes are compiled
3. **Check IDE settings**: Ensure "Build automatically" is enabled
4. **Manual trigger**: Stop and restart the service manually

### Frontend Not Updating

1. **Check Vite is running**: Verify dev server is active
2. **Clear browser cache**: Hard refresh (Cmd+Shift+R / Ctrl+Shift+R)
3. **Check console errors**: Look for compilation errors in terminal
4. **Restart dev server**: Stop and restart `npm run dev`

### Performance Issues

- **Too many restarts**: DevTools has a quiet period (1 second) to batch changes
- **Slow restarts**: Consider excluding large dependencies from restart
- **Memory issues**: Increase JVM heap size if needed

## Configuration Files

### Backend

- `pom.xml` - DevTools dependency (all services)
- `application.yml` - Optional DevTools configuration

### Frontend

- `vite.config.ts` - Vite configuration (HMR is default)
- `package.json` - Vite scripts

## Notes

- DevTools is **only active in development** (not in production builds)
- HMR works best with **modern browsers**
- Some changes may still require **manual refresh** (e.g., routing changes)

