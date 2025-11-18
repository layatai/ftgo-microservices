# Seed Scripts

This directory contains scripts for initializing test data in the FTGO microservices application.

## Seed Data Script

The `seed-data.js` script populates the database with test data including:
- 5 restaurants with menus (35 menu items total)
- 3 test customers
- Test orders (when using `--orders-only` flag)

### Prerequisites

1. All backend services must be running
2. API Gateway must be accessible at `http://localhost:8080`
3. Node.js must be installed
4. Install dependencies: `cd scripts && npm install`

### Usage

**Option 1: Using npm (Recommended)**
```bash
cd scripts
npm install  # First time only

# Seed everything (restaurants, menus, customers)
npm run seed

# Seed menus only (for existing restaurants)
npm run seed:menus

# Seed orders only (for testing order flow)
npm run seed:orders
```

**Option 2: Direct node execution**
```bash
# Seed everything
node scripts/seed-data.js

# Seed menus only
node scripts/seed-data.js --menus-only
# or
node scripts/seed-data.js -m

# Seed orders only
node scripts/seed-data.js --orders-only
# or
node scripts/seed-data.js -o

# Specify custom API URL
node scripts/seed-data.js --api-url=http://localhost:8080/api

# Or use environment variable
API_BASE_URL=http://localhost:8080/api node scripts/seed-data.js
```

**Option 3: Using VS Code Task**
1. Press `Cmd+Shift+P` (Mac) or `Ctrl+Shift+P` (Windows/Linux)
2. Type "Tasks: Run Task"
3. Select "seed-data"

### What Gets Created

#### Restaurants

1. **Pizza Palace** - Italian pizzeria with 7 menu items
2. **Burger House** - American burger joint with 7 menu items
3. **Sushi Express** - Japanese restaurant with 7 menu items
4. **Taco Fiesta** - Mexican restaurant with 7 menu items
5. **Pasta Paradise** - Italian pasta restaurant with 7 menu items

#### Customers

1. **John Doe** - john.doe@example.com
2. **Jane Smith** - jane.smith@example.com
3. **Bob Johnson** - bob.johnson@example.com

#### Test Orders

When using `--orders-only` or `npm run seed:orders`, the script will:
- Create 2-3 orders per customer
- Use random menu items from available restaurants
- Set delivery time to 1 hour from creation
- Use customer's address as delivery address
- Include idempotency keys for safe re-runs

### Example Output

```
🌱 Starting data seeding...

API Base URL: http://localhost:8080/api

📦 Creating restaurants...

Creating restaurant: Pizza Palace...
✓ Created restaurant: Pizza Palace (ID: abc-123-def)
  Adding 7 menu items...
  ✓ Menu updated
...

👥 Creating customers...

Creating customer: John Doe...
✓ Created customer: John Doe (ID: xyz-789-ghi)
...

==================================================
📊 Seeding Summary
==================================================
✓ Restaurants created: 5/5
✓ Customers created: 3/3

✅ Seeding completed!
```

### Troubleshooting

**Error: "ECONNREFUSED"**
- Make sure all backend services are running
- Verify API Gateway is accessible at the specified URL

**Error: "Restaurant not found"**
- The restaurant was created but menu update failed
- Try running the script again (it's safe to run multiple times)

**Error: "Customer already exists"**
- The script will continue and create other entities
- Check the error summary at the end

### Integration with VS Code

You can add this as a VS Code task. Add to `.vscode/tasks.json`:

```json
{
  "label": "seed-data",
  "type": "shell",
  "command": "node scripts/seed-data.js",
  "options": {
    "cwd": "${workspaceFolder}"
  },
  "problemMatcher": [],
  "presentation": {
    "reveal": "always",
    "panel": "new"
  }
}
```

Then run it via: `Cmd+Shift+P` → "Tasks: Run Task" → "seed-data"

