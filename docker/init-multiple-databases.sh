#!/bin/bash
set -e

# Create all databases for FTGO microservices
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE ftgo_customer;
    CREATE DATABASE ftgo_restaurant;
    CREATE DATABASE ftgo_order;
    CREATE DATABASE ftgo_kitchen;
    CREATE DATABASE ftgo_delivery;
    CREATE DATABASE ftgo_accounting;
    
    GRANT ALL PRIVILEGES ON DATABASE ftgo_customer TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE ftgo_restaurant TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE ftgo_order TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE ftgo_kitchen TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE ftgo_delivery TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE ftgo_accounting TO $POSTGRES_USER;
EOSQL

echo "All FTGO databases created successfully"

