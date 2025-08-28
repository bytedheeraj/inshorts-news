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

## 📁 Project Structure

```
src/main/java/com/example/inshorts/
├── config/                 # Configuration classes
│   ├── CacheConfig.java    # Redis caching configuration
│   └── RedisConfig.java    # Redis connection configuration
├── controller/             # REST API controllers
│   ├── NewsController.java # Core news endpoints
│   └── TrendingController.java # Trending news endpoints
├── dto/                    # Data Transfer Objects
│   ├── News.java          # News response DTO
│   ├── NewsQueryRequest.java # Query request DTO
│   ├── NewsResponse.java  # Query response DTO
│   └── TrendingArticle.java # Trending article DTO
├── entity/                 # MongoDB entities
│   ├── NewsEntity.java    # News article entity
│   └── UserEvent.java     # User interaction events
├── enums/                  # Enumerations
│   └── IntentType.java    # Query intent types
├── repository/             # Data access layer
│   ├── NewsRepository.java # News data operations
│   └── UserEventRepository.java # User event operations
├── service/                # Business logic layer
│   ├── NewsMapper.java    # Entity-DTO mapper
│   ├── NewsService.java   # News service interface
│   ├── TrendingService.java # Trending service interface
│   └── impl/              # Service implementations
│       ├── NewsServiceImpl.java
│       ├── RealLLMServiceImpl.java
│       └── TrendingServiceImpl.java
└── InshortsApplication.java # Main application class
```

## 🔧 Configuration

### Application Properties
```properties
# Server Configuration
server.port=8080

# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=inshorts
spring.data.mongodb.collection=news_data

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis
spring.cache.redis.time-to-live=300000

# LLM Configuration
llm.huggingface.url=https://api-inference.huggingface.co/models
llm.huggingface.token=${HUGGINGFACE_TOKEN:}
```

### Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

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

## 🧠 LLM Integration

### Intent Detection
The system automatically detects user intent from natural language queries:
- **Category**: "Show me technology news"
- **Source**: "News from Reuters"
- **Nearby**: "What's happening near me?"
- **Score**: "High quality news articles"
- **Search**: General text search

### Entity & Concept Extraction
- **Entities**: People, places, organizations
- **Concepts**: Topics, themes, categories
- **Fallback**: Intelligent fallback when LLM is unavailable

## 📊 Trending Algorithm

### Scoring Factors
1. **Event Weight**: VIEW (1.0), CLICK (2.0), SHARE (3.0), BOOKMARK (2.5)
2. **Recency**: Time decay with 24-hour half-life
3. **Geographic Relevance**: Distance-based bonus
4. **User Engagement**: Volume of interactions

### Caching Strategy
- **Trending Cache**: 2-minute TTL for real-time data
- **News Cache**: 10-minute TTL for static content
- **Geographic Segmentation**: Location-based cache keys

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

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB 5.0+
- Redis 6.0+

### Installation
1. **Clone the repository**
   ```bash
   git clone https://github.com/bytedheeraj/inshorts-news.git
   cd inshorts-news
   ```

2. **Start MongoDB**
   ```bash
   mongod --dbpath /path/to/data/db
   ```

3. **Start Redis**
   ```bash
   redis-server
   ```

4. **Set environment variables** (optional)
   ```bash
   export HUGGINGFACE_TOKEN=your_token_here
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

### Sample Data
The application automatically loads sample news data from `src/main/resources/news_data.json` on startup.

## 🧪 Testing

### Manual Testing
```bash
# Test trending API
curl -X GET "http://localhost:8080/api/trending?lat=20.0&lon=75.0&limit=3"

# Test news query
curl -X POST "http://localhost:8080/api/news/query" \
  -H "Content-Type: application/json" \
  -d '{"query": "Show me technology news"}'

# Simulate user events
curl -X POST "http://localhost:8080/api/trending/simulate-events"
```

## 🔍 Monitoring

### Health Checks
- **Application Health**: `/actuator/health`
- **Trending Service**: `/api/trending/health`
- **MongoDB Connection**: Automatic health monitoring
- **Redis Connection**: Automatic health monitoring

### Logging
- **Log Level**: DEBUG for development
- **Structured Logging**: JSON format with correlation IDs
- **Performance Metrics**: Query processing time tracking

## 🚀 Production Deployment

### Docker Support
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/inshorts-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables
- `MONGODB_URI`: MongoDB connection string
- `REDIS_URL`: Redis connection string
- `HUGGINGFACE_TOKEN`: LLM API token
- `SERVER_PORT`: Application port

### Scaling Considerations
- **Horizontal Scaling**: Stateless application design
- **Cache Distribution**: Redis cluster for high availability
- **Database Sharding**: MongoDB sharding for large datasets
- **Load Balancing**: Multiple application instances

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- MongoDB team for geospatial capabilities
- Redis team for high-performance caching
- Hugging Face for LLM integration
- Lombok team for clean code generation

---

**Built with ❤️ for intelligent news discovery**
