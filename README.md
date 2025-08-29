# Inshorts News API - Contextual News Data Retrieval System

A Spring Boot application that provides intelligent news retrieval using LLM integration, geospatial queries, and trending analysis.

## 🚀 Features

### Core Functionality
- **Intelligent News Retrieval**: Uses LLM to understand user intent and extract entities and categories
- **Geospatial Queries**: Find news articles near specific locations using MongoDB geospatial features
- **Multi-criteria Search**: Search by category, source, relevance score, or text content
- **Trending (Bonus)**: Location-based trending news with user engagement analytics

### Advanced Features
- **LLM Integration**: Hugging Face API integration (with safe handling)
- **Redis Caching**: High-performance caching for trending feeds and news queries
- **MongoDB**: Stores articles and supports geospatial queries

## 🏗️ Architecture

### Technology Stack
- **Java 17** with Spring Boot 3.4.9
- **MongoDB** for data persistence
- **Redis** for caching
- **Spring Data MongoDB** for database operations
- **Spring Cache** with Redis implementation
- **Lombok** for code generation

### Design Patterns
- **Service Layer Pattern**: Clean separation of business logic
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: Data transfer objects for API responses
- **Mapper Pattern**: Entity-DTO conversion
- **Strategy Pattern**: Intent-based query routing

## 🚀 API Endpoints

### News Endpoints
- `GET /api/news/all` - Get all news articles
- `GET /api/news/category/{category}` - Get news by category
- `GET /api/news/source/{source}` - Get news by source
- `GET /api/news/score/{threshold}` - Get news above relevance score
- `GET /api/news/search?q={query}` - Search news by text
- `GET /api/news/nearby?lat={lat}&lon={lon}&radius={km}` - Get nearby news
- `POST /api/news/query` - Process intelligent news query

### Trending Endpoints
- `GET /api/trending?lat={lat}&lon={lon}&limit={n}` - Get trending news
- `GET /api/trending/category/{category}?lat={lat}&lon={lon}&limit={n}` - Get trending by category
- `POST /api/trending/simulate-events` - Simulate user events
- `GET /api/trending/health` - Trending service health check

## 🗄️ Database Design

### News Collection
```json
{
  "_id": "uuid",
  "title": "Article Title",
  "description": "Article description...",
  "url": "https://example.com/article",
  "publication_date": "2024-01-01T00:00:00Z",
  "source_name": "News Source",
  "category": ["technology", "business"],
  "relevance_score": 0.85,
  "latitude": 37.7749,
  "longitude": -122.4194
}
```

### User Events Collection
```json
{
  "_id": "uuid",
  "article_id": "article-uuid",
  "user_id": "user-123",
  "event_type": "CLICK",
  "user_latitude": 37.7749,
  "user_longitude": -122.4194,
  "timestamp": "2024-01-01T00:00:00Z",
  "weight": 2.0
}
```

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- MongoDB team for geospatial capabilities
- Redis team for high-performance caching
- Hugging Face for LLM integration
- Lombok team for clean code generation

---

**Built with ❤️ for intelligent news discovery**
