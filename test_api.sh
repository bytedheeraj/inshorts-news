#!/bin/bash

# Test script for the Contextual News Data Retrieval System
# Make sure the application is running on localhost:8080

echo "🧪 Testing Contextual News Data Retrieval System"
echo "================================================"
echo

# Wait for application to start
echo "⏳ Waiting for application to start..."
sleep 5

# Test 1: Health Check
echo "1️⃣ Testing Health Check..."
curl -s http://localhost:8083/api/v1/news/health
echo -e "\n"

# Test 2: Get All News
echo "2️⃣ Testing Get All News..."
curl -s http://localhost:8083/api/v1/news/all | jq '. | length' 2>/dev/null || echo "No articles found or jq not installed"
echo -e "\n"

# Test 3: Contextual Query - Technology News
echo "3️⃣ Testing Contextual Query - Technology News..."
curl -s -X POST http://localhost:8083/api/v1/news/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Latest technology news",
    "latitude": 37.7749,
    "longitude": -122.4194
  }' | jq '.intent, .entities, .concepts, .totalCount' 2>/dev/null || echo "Query failed"
echo -e "\n"

# Test 4: Contextual Query - Elon Musk Twitter
echo "4️⃣ Testing Contextual Query - Elon Musk Twitter..."
curl -s -X POST http://localhost:8083/api/v1/news/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Latest developments in the Elon Musk Twitter acquisition near Palo Alto",
    "latitude": 37.4419,
    "longitude": -122.1430
  }' | jq '.intent, .entities, .concepts, .totalCount' 2>/dev/null || echo "Query failed"
echo -e "\n"

# Test 5: Category Retrieval
echo "5️⃣ Testing Category Retrieval - Technology..."
curl -s http://localhost:8083/api/v1/news/category/Technology | jq '. | length' 2>/dev/null || echo "Category retrieval failed"
echo -e "\n"

# Test 6: Source Retrieval
echo "6️⃣ Testing Source Retrieval - Reuters..."
curl -s http://localhost:8083/api/v1/news/source/Reuters | jq '. | length' 2>/dev/null || echo "Source retrieval failed"
echo -e "\n"

# Test 7: Score-based Retrieval
echo "7️⃣ Testing Score-based Retrieval (threshold: 0.8)..."
curl -s "http://localhost:8083/api/v1/news/score?threshold=0.8" | jq '. | length' 2>/dev/null || echo "Score retrieval failed"
echo -e "\n"

# Test 8: Text Search
echo "8️⃣ Testing Text Search - 'Apple'..."
curl -s "http://localhost:8083/api/v1/news/search?q=Apple" | jq '. | length' 2>/dev/null || echo "Text search failed"
echo -e "\n"

# Test 9: Nearby News
echo "9️⃣ Testing Nearby News (Palo Alto area)..."
curl -s "http://localhost:8083/api/v1/news/nearby?latitude=37.4419&longitude=-122.1430&radius=10" | jq '. | length' 2>/dev/null || echo "Nearby news failed"
echo -e "\n"

echo "✅ API Testing Complete!"
echo "📊 Check the responses above to verify system functionality"
echo "🌐 H2 Console available at: http://localhost:8080/h2-console"
echo "📚 API Documentation available in README.md"
