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

# Create a payment intent
curl -X POST http://localhost:8081/payment-intents \
  -H "Content-Type: application/json" \
  -d '{
    "traceId":"550e8400-e29b-41d4-a716-446655440003",
    "endToEndId":"e2e-014",
    "amountInMinor":128,
    "currency":"EUR",
    "provider":"mock-payments-fr-redirect"
  }' | jq .

echo ""
echo "✅ Test completed!"

