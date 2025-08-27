# Contextual News Data Retrieval System

A sophisticated backend system that can fetch and organize news articles from a data source, simulating different API functionalities, and enrich these articles with LLM-generated insights.

## Features

- **LLM-Powered Query Processing**: Automatically extracts entities, concepts, and intent from natural language queries
- **Contextual Data Retrieval**: Routes queries to appropriate data retrieval strategies based on detected intent
- **Multiple Retrieval Strategies**: Supports category, source, score, search, and nearby location-based retrieval
- **Geographic Awareness**: Considers user location for relevant news delivery
- **Relevance Scoring**: Ranks articles by relevance score for optimal user experience
- **RESTful API**: Clean, intuitive API endpoints for easy integration

## Technical Stack

- **Backend**: Spring Boot 3.4.9 with Java 17
- **Database**: H2 In-Memory Database (can be easily switched to PostgreSQL/MySQL)
- **Data Access**: Spring Data JPA with custom queries
- **API**: RESTful endpoints with JSON responses
- **Validation**: Bean Validation for request validation
- **Documentation**: Comprehensive API documentation

## System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │   LLM Service   │    │   News Service  │
│   Controller    │◄──►│   (NLP Logic)   │◄──►│   (Business     │
│                 │    │                 │    │    Logic)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Validation    │    │   Entity        │    │   Repository    │
│   Layer         │    │   Extraction    │    │   Layer         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                                                       ▼
                                              ┌─────────────────┐
                                              │   H2 Database   │
                                              │   (In-Memory)   │
                                              └─────────────────┘
```

## API Endpoints

### Main Query Endpoint
- **POST** `/api/news/query` - Process contextual news queries

### Specific Retrieval Endpoints
- **GET** `/api/news/category/{category}` - Get news by category
- **GET** `/api/news/source/{source}` - Get news by source
- **GET** `/api/news/score?threshold={value}` - Get news by relevance score
- **GET** `/api/news/search?q={query}` - Search news by text
- **GET** `/api/news/nearby?latitude={lat}&longitude={lng}&radius={km}` - Get nearby news
- **GET** `/api/news/all` - Get all news articles

### Data Management Endpoints
- **POST** `/api/news/save` - Save a single news article
- **POST** `/api/news/save-all` - Save multiple news articles
- **GET** `/api/news/health` - Health check

## Usage Examples

### 1. Contextual Query Processing

**Request:**
```json
POST /api/news/query
{
  "query": "Latest developments in the Elon Musk Twitter acquisition near Palo Alto",
  "latitude": 37.4419,
  "longitude": -122.1430
}
```

**Expected Response:**
```json
{
  "articles": [...],
  "totalCount": 2,
  "query": "Latest developments in the Elon Musk Twitter acquisition near Palo Alto",
  "intent": "nearby",
  "entities": "Elon Musk, Twitter/X, Palo Alto",
  "concepts": "business acquisition, social media, development",
  "processingTimeMs": 45
}
```

### 2. Category-Based Retrieval

**Request:**
```json
POST /api/news/query
{
  "query": "Top technology news from the New York Times",
  "latitude": 40.7128,
  "longitude": -74.0060
}
```

**Expected Response:**
```json
{
  "articles": [...],
  "totalCount": 3,
  "query": "Top technology news from the New York Times",
  "intent": "source",
  "entities": "New York Times",
  "concepts": "technology, news",
  "processingTimeMs": 32
}
```

## Intent Detection

The system automatically detects user intent from natural language queries:

- **category**: Technology, Business, Sports, Politics, Entertainment, Health, Science, General
- **source**: New York Times, Reuters, BBC, CNN, Fox, NBC, ABC, CBS, DW
- **score**: High relevance score articles (threshold-based)
- **nearby**: Location-based news within specified radius
- **search**: General text search in titles and descriptions

## Data Model

### News Article Structure
```json
{
  "id": "unique-identifier",
  "title": "Article Title",
  "description": "Article description...",
  "url": "https://article-url.com",
  "publication_date": "2025-03-24T11:08:11",
  "source_name": "Source Name",
  "category": ["Category1", "Category2"],
  "relevance_score": 0.85,
  "latitude": 37.7749,
  "longitude": -122.4194
}
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- OpenAI API Key (optional, for real LLM functionality)

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd inshorts
   ```

2. **Set Java 17 as active**
   ```bash
   export JAVA_HOME=/usr/local/opt/openjdk@17
   export PATH=$JAVA_HOME/bin:$PATH
   ```

3. **(Optional) Configure OpenAI API**
   - Add your OpenAI API key to `src/main/resources/application.properties`:
   ```properties
   openai.api.key=your-openai-api-key-here
   ```
   - If no API key is provided, the system will use the fallback implementation

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8080`
   - H2 Console: `http://localhost:8080/h2-console`
   - Health Check: `http://localhost:8080/api/news/health`

### Database Access
- **URL**: `jdbc:h2:mem:newsdb`
- **Username**: `sa`
- **Password**: `password`

## Sample Data

The system comes pre-loaded with sample news articles covering various categories:
- Technology news (Apple, Tesla)
- Business updates (earnings, acquisitions)
- Sports coverage (football, championships)
- Political developments (climate agreements)
- Entertainment updates (Marvel movies)
- Health breakthroughs (cancer treatment)
- Scientific discoveries (exoplanets)
- Local news (Palo Alto startups)

## Testing the System

### 1. Health Check
```bash
curl http://localhost:8080/api/news/health
```

### 2. Get All News
```bash
curl http://localhost:8080/api/news/all
```

### 3. Test Contextual Query
```bash
curl -X POST http://localhost:8080/api/news/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Latest technology news",
    "latitude": 37.7749,
    "longitude": -122.4194
  }'
```

### 4. Test Category Retrieval
```bash
curl http://localhost:8080/api/news/category/Technology
```

### 5. Test Nearby News
```bash
curl "http://localhost:8080/api/news/nearby?latitude=37.7749&longitude=-122.4194&radius=10"
```

## Customization

### Adding New Categories
Update the `CATEGORY_KEYWORDS` set in `LLMServiceImpl.java`

### Adding New Sources
Update the `SOURCE_KEYWORDS` set in `LLMServiceImpl.java`

### Modifying Intent Detection
Enhance the `determineIntent` method in `LLMServiceImpl.java`

### Database Configuration
Modify `application.properties` to use different databases (PostgreSQL, MySQL, etc.)

## Performance Features

- **In-Memory Database**: Fast data access for development and testing
- **Efficient Queries**: Optimized JPA queries with custom SQL for complex operations
- **Response Time Tracking**: Built-in processing time measurement
- **Sorted Results**: Articles automatically sorted by relevance score

## LLM Integration

The system includes **real OpenAI API integration** with automatic fallback to a simulated LLM service:

### OpenAI Integration (Primary)
- **Real AI Processing**: Uses GPT-3.5-turbo for entity extraction, concept identification, and intent determination
- **Automatic Fallback**: If OpenAI API is unavailable, automatically switches to simulated implementation
- **Configurable**: Add your OpenAI API key to enable real AI functionality

### Fallback Implementation (Simulated)
- **Intent Detection**: Category, Source, Nearby, Score, Search
- **Entity Extraction**: People names, organizations, locations, events
- **Concept Identification**: Business terms, technology concepts, news categories

## Future Enhancements

- **Enhanced LLM Models**: Support for GPT-4, Claude, and other advanced models
- **Caching Layer**: Redis integration for improved performance
- **User Authentication**: JWT-based authentication system
- **Rate Limiting**: API rate limiting and throttling
- **Analytics Dashboard**: User query analytics and insights
- **News Aggregation**: Real-time news fetching from multiple sources

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For questions and support, please open an issue in the repository.
