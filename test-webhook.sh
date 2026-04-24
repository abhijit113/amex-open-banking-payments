#!/bin/bash
# TrueLayer Webhook Testing Script
# This script tests the TrueLayer webhook integration with real credentials

set -e

# Export the TrueLayer credentials
export TRUELAYER_CLIENT_ID="sandbox-amexrealtime-04a8f4"
export TRUELAYER_CLIENT_SECRET="6b9ebadc-985b-4921-97fc-2b2badda8fd4"
export TRUELAYER_MERCHANT_ACCOUNT_ID="a6ced445-2495-33f7-f83c-69d97f88eef6"
export TRUELAYER_SIGNING_KEY_ID="bd2fc0ac-ff1b-488e-b1de-81d1abe638e5"
export TRUELAYER_PRIVATE_KEY_PATH="$HOME/.truelayer/ec512-private-key.pem"

echo "=== TrueLayer Webhook Testing Script ==="
echo ""

# Check if PostgreSQL is running
echo "0. Checking PostgreSQL status..."
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
  echo "⏳ PostgreSQL is not running. Starting it..."

  # Set locale environment variables before starting PostgreSQL
  export LC_ALL=en_US.UTF-8
  export LANG=en_US.UTF-8

  # Kill any stray PostgreSQL processes
  pkill -f "postgres -D" > /dev/null 2>&1 || true
  sleep 1

  # Remove stale lock file if it exists
  rm -f /opt/homebrew/var/postgresql@16/postmaster.pid > /dev/null 2>&1 || true

  # Start PostgreSQL directly (more reliable than brew services)
  /opt/homebrew/opt/postgresql@16/bin/pg_ctl -D /opt/homebrew/var/postgresql@16 -l /tmp/postgres.log start > /dev/null 2>&1 || true

  # Wait for PostgreSQL to start (with retries)
  MAX_RETRIES=10
  RETRY_COUNT=0
  while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
      echo "✅ PostgreSQL started successfully"
      break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    sleep 1
  done

  if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ Failed to start PostgreSQL. Check logs:"
    tail -10 /tmp/postgres.log
    exit 1
  fi
else
  echo "✅ PostgreSQL is already running"
fi
echo ""

# Check if the amex_payments database exists, create it if needed
echo "   Checking database 'amex_payments'..."
if psql -U abhijitsen -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='amex_payments'" | grep -q 1; then
  echo "   ✅ Database 'amex_payments' exists"
else
  echo "   ⏳ Creating database 'amex_payments'..."
  createdb -U abhijitsen amex_payments
  echo "   ✅ Database 'amex_payments' created"
fi
echo ""

# Check if the application is running
if ! curl -s http://localhost:8081/ > /dev/null 2>&1; then
  echo "⏳ Application is not running. Starting it..."

  # Get the project directory
  PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

  # Start the application in the background
  cd "$PROJECT_DIR"
  export LC_ALL=en_US.UTF-8
  export LANG=en_US.UTF-8

  # Start Quarkus dev mode in the background and redirect output to a log file
  ./gradlew quarkusDev > /tmp/quarkus-app.log 2>&1 &
  APP_PID=$!

  # Wait for the application to start (with retries)
  MAX_RETRIES=30
  RETRY_COUNT=0
  while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s http://localhost:8081/ > /dev/null 2>&1; then
      echo "✅ Application started successfully (PID: $APP_PID)"
      break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    sleep 1
  done

  if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ Failed to start application. Check logs:"
    tail -20 /tmp/quarkus-app.log
    kill $APP_PID > /dev/null 2>&1 || true
    exit 1
  fi
else
  echo "✅ Application is already running on localhost:8081"
fi
echo ""
echo "1. Getting access token from TrueLayer auth server..."

# Get access token
export ACCESS_TOKEN=$(curl -s -X POST 'https://auth.truelayer-sandbox.com/connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode "grant_type=client_credentials" \
  --data-urlencode "client_id=$TRUELAYER_CLIENT_ID" \
  --data-urlencode "client_secret=$TRUELAYER_CLIENT_SECRET" \
  --data-urlencode "scope=payments" | jq -r '.access_token // empty')

if [ -z "$ACCESS_TOKEN" ]; then
  echo "❌ Failed to obtain access token"
  exit 1
fi

echo "✅ Access token obtained: ${ACCESS_TOKEN:0:20}..."
echo ""

echo "2. Fetching debug signature from local app..."

# Get debug signature from the local app
export TL_DEBUG_JSON=$(curl -s http://localhost:8081/debug/truelayer/signature)

if [ $? -ne 0 ]; then
  echo "❌ Failed to fetch debug signature. Is the app running on localhost:8081?"
  exit 1
fi

export TL_SIGNATURE=$(echo "$TL_DEBUG_JSON" | jq -r '.signature')
export TL_BODY=$(echo "$TL_DEBUG_JSON" | jq -r '.body')
export TL_IDEMPOTENCY_KEY=$(echo "$TL_DEBUG_JSON" | jq -r '.idempotencyKey')

if [ -z "$TL_SIGNATURE" ] || [ "$TL_SIGNATURE" = "null" ]; then
  echo "❌ Failed to get signature from app. Response:"
  echo "$TL_DEBUG_JSON" | jq .
  exit 1
fi

echo "✅ Debug signature obtained"
echo "   Idempotency Key: $TL_IDEMPOTENCY_KEY"
echo ""

echo "3. Testing signature verification with TrueLayer API..."

# Test the signature with TrueLayer
VERIFY_RESPONSE=$(curl -s -i -X POST 'https://api.truelayer-sandbox.com/test-signature' \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Idempotency-Key: $TL_IDEMPOTENCY_KEY" \
  -H "Tl-Signature: $TL_SIGNATURE" \
  -H "Content-Type: application/json" \
  --data "$TL_BODY")

echo "$VERIFY_RESPONSE"
echo ""

echo "4. Creating a payment intent..."

# Generate random traceId in UUID format and endToEndId with timestamp
TRACE_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
END_TO_END_ID="e2e-$(date +%s)"

# Create a payment intent
PAYMENT_RESPONSE=$(curl -s -X POST http://localhost:8081/payment-intents \
  -H "Content-Type: application/json" \
  -d '{
    "traceId":"'"$TRACE_ID"'",
    "endToEndId":"'"$END_TO_END_ID"'",
    "amountInMinor":111,
    "currency":"EUR",
    "provider":"mock-payments-it-redirect"
  }')

# Check if the response is valid JSON
if echo "$PAYMENT_RESPONSE" | jq . > /dev/null 2>&1; then
  echo "$PAYMENT_RESPONSE" | jq .
else
  echo "❌ Payment intent creation failed with response:"
  echo "$PAYMENT_RESPONSE"
  exit 1
fi

echo ""
echo "✅ Test completed!"

