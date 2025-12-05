# Transaction Sync Service - Implementation Guide

## Project Structure

```
transaction-sync-service/
├── src/
│   ├── main/
│   │   ├── java/com/transactionsync/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/      # Exception handlers
│   │   │   ├── integration/    # External API clients
│   │   │   ├── model/          # JPA entities
│   │   │   ├── repository/     # Spring Data repositories
│   │   │   ├── scheduler/      # Scheduled jobs
│   │   │   └── service/        # Business logic services
│   │   └── resources/
│   │       ├── application.yml # Application configuration
│   │       └── db/migration/   # Flyway migrations
│   └── test/                   # Test files
├── frontend/                   # Angular frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── dashboard/      # Dashboard module
│   │   │   ├── transactions/   # Transaction module
│   │   │   ├── merchants/      # Merchant module
│   │   │   ├── retry-queue/    # Retry queue module
│   │   │   ├── logs/          # Logs module
│   │   │   └── shared/        # Shared components/services
│   │   └── styles.scss
│   ├── angular.json
│   └── package.json
├── pom.xml                     # Maven configuration
├── Dockerfile                  # Docker configuration
└── docker-compose.yml         # Docker Compose setup
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 15+
- Node.js 18+ (for Angular frontend)
- Docker (optional, for containerized deployment)

### Backend Setup

1. **Configure Database**
   - Update `application.yml` with your PostgreSQL credentials
   - Or use environment variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`

2. **Configure API Credentials**
   - Set environment variables:
     - `PRIVVY_API_URL`
     - `PRIVVY_EMAIL`
     - `PRIVVY_PASSWORD`
     - `REDFYNN_API_URL`
     - `REDFYNN_API_KEY`

3. **Run Database Migrations**
   - Flyway will automatically run migrations on startup

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   Or use Docker:
   ```bash
   docker-compose up -d
   ```

### Frontend Setup

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Run Development Server**
   ```bash
   npm start
   ```

   The application will be available at `http://localhost:4200`

3. **Build for Production**
   ```bash
   npm run build
   ```

## API Endpoints

### Transactions
- `POST /api/v1/transactions/sync` - Sync all transactions
- `POST /api/v1/transactions/sync/{mid}` - Sync specific merchant
- `GET /api/v1/transactions` - List transactions
- `GET /api/v1/transactions/{id}` - Get transaction details
- `GET /api/v1/transactions/merchant/{mid}` - Get merchant transactions

### Merchants
- `GET /api/v1/merchants` - List all merchants
- `GET /api/v1/merchants/{mid}` - Get merchant details
- `POST /api/v1/merchants/sync` - Sync merchants
- `PUT /api/v1/merchants/{mid}` - Update merchant
- `GET /api/v1/merchants/stats` - Merchant statistics

### Retry Queue
- `GET /api/v1/retry-queue` - List retry queue entries
- `POST /api/v1/retry-queue/process` - Process pending retries
- `DELETE /api/v1/retry-queue/{id}` - Delete retry entry
- `GET /api/v1/retry-queue/stats` - Retry queue statistics

### Dashboard
- `GET /api/v1/dashboard/stats` - System statistics
- `GET /api/v1/dashboard/health` - Health check

## API Contracts

### Transaction Endpoints

#### POST /api/v1/transactions/sync
**Description**: Manually trigger synchronization of all transactions for all merchants.

**Request Body**:
```json
{
  "startDate": "2024-01-15",
  "endDate": "2024-01-15"
}
```

**Request Parameters** (Optional):
- `startDate` (string, ISO date format): Start date for transaction sync
- `endDate` (string, ISO date format): End date for transaction sync

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Transaction sync initiated",
  "jobId": "sync-job-12345",
  "merchantsProcessed": 0,
  "merchantsTotal": 150,
  "estimatedCompletionTime": "2024-01-15T23:45:00Z"
}
```

**Error Response** (400 Bad Request):
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid date format",
  "path": "/api/v1/transactions/sync"
}
```

#### POST /api/v1/transactions/sync/{mid}
**Description**: Sync transactions for a specific merchant.

**Path Parameters**:
- `mid` (string, required): Merchant ID

**Request Body** (Optional):
```json
{
  "startDate": "2024-01-15",
  "endDate": "2024-01-15"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Transaction sync completed for merchant",
  "mid": "476200404889",
  "transactionsProcessed": 25,
  "transactionsSynced": 25,
  "depositsCreated": 3,
  "duration": "2.5s"
}
```

**Error Response** (404 Not Found):
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Merchant not found: 476200404889",
  "path": "/api/v1/transactions/sync/476200404889"
}
```

#### GET /api/v1/transactions
**Description**: List transactions with pagination and filtering.

**Query Parameters**:
- `page` (integer, default: 0): Page number (0-indexed)
- `size` (integer, default: 20): Page size
- `mid` (string, optional): Filter by merchant ID
- `startDate` (string, optional): Filter by start date (ISO format)
- `endDate` (string, optional): Filter by end date (ISO format)
- `cardType` (string, optional): Filter by card type (VI, MC, AX, DC)
- `sort` (string, default: "transactionDate,desc"): Sort field and direction

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 12345,
      "mid": "476200404889",
      "transactionId": "TXN-123456",
      "batchNumber": "BATCH-001",
      "amount": 125.50,
      "transactionDate": "2024-01-15",
      "authCode": "AUTH123",
      "cardType": "VI",
      "cardLast4": "1234",
      "cardFirst6": "411111",
      "posEntryMode": "05",
      "transactionType": "SALE",
      "isVoided": false,
      "redfynnDepositId": "DEP-789",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

#### GET /api/v1/transactions/{id}
**Description**: Get detailed information about a specific transaction.

**Path Parameters**:
- `id` (long, required): Transaction ID

**Response** (200 OK):
```json
{
  "id": 12345,
  "mid": "476200404889",
  "transactionId": "TXN-123456",
  "batchNumber": "BATCH-001",
  "amount": 125.50,
  "transactionDate": "2024-01-15",
  "authCode": "AUTH123",
  "cardType": "VI",
  "cardLast4": "1234",
  "cardFirst6": "411111",
  "posEntryMode": "05",
  "transactionType": "SALE",
  "isVoided": false,
  "redfynnDepositId": "DEP-789",
  "createdAt": "2024-01-15T10:30:00Z",
  "merchant": {
    "mid": "476200404889",
    "name": "Example Merchant"
  }
}
```

#### GET /api/v1/transactions/merchant/{mid}
**Description**: Get all transactions for a specific merchant.

**Path Parameters**:
- `mid` (string, required): Merchant ID

**Query Parameters**:
- `page` (integer, default: 0): Page number
- `size` (integer, default: 20): Page size
- `startDate` (string, optional): Filter by start date
- `endDate` (string, optional): Filter by end date

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 12345,
      "mid": "476200404889",
      "transactionId": "TXN-123456",
      "amount": 125.50,
      "transactionDate": "2024-01-15",
      "cardType": "VI",
      "cardLast4": "1234"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  },
  "merchant": {
    "mid": "476200404889",
    "name": "Example Merchant"
  }
}
```

### Merchant Endpoints

#### GET /api/v1/merchants
**Description**: List all merchants with pagination.

**Query Parameters**:
- `page` (integer, default: 0): Page number
- `size` (integer, default: 20): Page size
- `status` (string, optional): Filter by status (active, inactive)
- `search` (string, optional): Search by MID or name

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "mid": "476200404889",
      "name": "Example Merchant",
      "status": "active",
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-15T10:30:00Z",
      "lastSyncedAt": "2024-01-15T01:00:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

#### GET /api/v1/merchants/{mid}
**Description**: Get detailed information about a specific merchant.

**Path Parameters**:
- `mid` (string, required): Merchant ID

**Response** (200 OK):
```json
{
  "id": 1,
  "mid": "476200404889",
  "name": "Example Merchant",
  "status": "active",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "lastSyncedAt": "2024-01-15T01:00:00Z",
  "statistics": {
    "totalTransactions": 1250,
    "totalAmount": 125000.50,
    "lastTransactionDate": "2024-01-15"
  }
}
```

#### POST /api/v1/merchants/sync
**Description**: Manually trigger merchant synchronization from Privvy API.

**Request Body**: None

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchant sync completed",
  "merchantsFetched": 150,
  "merchantsCreated": 5,
  "merchantsUpdated": 145,
  "duration": "15.2s"
}
```

#### PUT /api/v1/merchants/{mid}
**Description**: Update merchant information.

**Path Parameters**:
- `mid` (string, required): Merchant ID

**Request Body**:
```json
{
  "name": "Updated Merchant Name",
  "status": "active"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "mid": "476200404889",
  "name": "Updated Merchant Name",
  "status": "active",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

#### GET /api/v1/merchants/stats
**Description**: Get merchant statistics.

**Response** (200 OK):
```json
{
  "totalMerchants": 150,
  "activeMerchants": 145,
  "inactiveMerchants": 5,
  "merchantsWithTransactions": 120,
  "lastSyncDate": "2024-01-15T01:00:00Z"
}
```

### Retry Queue Endpoints

#### GET /api/v1/retry-queue
**Description**: List retry queue entries with filtering.

**Query Parameters**:
- `page` (integer, default: 0): Page number
- `size` (integer, default: 20): Page size
- `status` (string, optional): Filter by status (pending, completed, max_retries_reached)
- `mid` (string, optional): Filter by merchant ID

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "mid": "476200404889",
      "processDate": "2024-01-14",
      "retryCount": 1,
      "lastRetryAt": "2024-01-15T03:00:00Z",
      "nextRetryAt": "2024-01-16T03:00:00Z",
      "status": "pending",
      "errorMessage": "Empty response from Privvy API",
      "createdAt": "2024-01-14T23:30:00Z",
      "updatedAt": "2024-01-15T03:00:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 10,
    "totalPages": 1
  }
}
```

#### POST /api/v1/retry-queue/process
**Description**: Manually trigger processing of pending retries.

**Request Body**: None

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Retry processing completed",
  "retriesProcessed": 5,
  "retriesSuccessful": 3,
  "retriesFailed": 2,
  "duration": "45.5s"
}
```

#### DELETE /api/v1/retry-queue/{id}
**Description**: Delete a retry queue entry.

**Path Parameters**:
- `id` (long, required): Retry queue entry ID

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Retry queue entry deleted"
}
```

**Error Response** (404 Not Found):
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Retry queue entry not found: 1",
  "path": "/api/v1/retry-queue/1"
}
```

#### GET /api/v1/retry-queue/stats
**Description**: Get retry queue statistics.

**Response** (200 OK):
```json
{
  "totalRetries": 25,
  "pendingRetries": 10,
  "completedRetries": 12,
  "maxRetriesReached": 3,
  "averageRetryCount": 1.5,
  "oldestPendingRetry": "2024-01-13T03:00:00Z"
}
```

### Dashboard Endpoints

#### GET /api/v1/dashboard/stats
**Description**: Get overall system statistics.

**Response** (200 OK):
```json
{
  "transactions": {
    "total": 50000,
    "today": 1250,
    "thisWeek": 8750,
    "thisMonth": 35000
  },
  "merchants": {
    "total": 150,
    "active": 145,
    "inactive": 5
  },
  "retryQueue": {
    "pending": 10,
    "completed": 500,
    "maxRetriesReached": 5
  },
  "syncStatus": {
    "lastTransactionSync": "2024-01-15T23:30:00Z",
    "lastMerchantSync": "2024-01-15T01:00:00Z",
    "lastRetryProcess": "2024-01-15T03:00:00Z"
  },
  "systemHealth": {
    "status": "healthy",
    "database": "connected",
    "privvyApi": "connected",
    "redfynnApi": "connected"
  }
}
```

#### GET /api/v1/dashboard/health
**Description**: Health check endpoint.

**Response** (200 OK):
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "SELECT 1"
      }
    },
    "privvyApi": {
      "status": "UP"
    },
    "redfynnApi": {
      "status": "UP"
    }
  }
}
```

**Error Response** (503 Service Unavailable):
```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN",
      "details": {
        "error": "Connection refused"
      }
    }
  }
}
```

### Common Error Response Format

All endpoints may return the following error responses:

**400 Bad Request**:
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/endpoint",
  "errors": [
    {
      "field": "startDate",
      "message": "Date cannot be in the future"
    }
  ]
}
```

**401 Unauthorized**:
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/api/v1/endpoint"
}
```

**500 Internal Server Error**:
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/v1/endpoint",
  "requestId": "abc123"
}
```

## Database Schema

### merchants
Stores merchant information synced from Privvy API.

```sql
CREATE TABLE merchants (
    id BIGSERIAL PRIMARY KEY,
    mid VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    last_synced_at TIMESTAMPTZ,
    CONSTRAINT chk_status CHECK (status IN ('active', 'inactive', 'suspended')),
    INDEX idx_status (status),
    INDEX idx_mid (mid)
);
```

**Columns**:
- `id`: Primary key (auto-increment)
- `mid`: Merchant ID (unique, not null)
- `name`: Merchant name
- `status`: Merchant status (active, inactive, suspended)
- `created_at`: Record creation timestamp
- `updated_at`: Last update timestamp
- `last_synced_at`: Last synchronization timestamp from Privvy

### transactions
Stores transaction data synced from Privvy and sent to Redfynn.

```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    mid VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100),
    batch_number VARCHAR(50),
    amount DECIMAL(10,2) NOT NULL,
    transaction_date DATE NOT NULL,
    auth_code VARCHAR(50),
    card_type VARCHAR(10),
    card_last4 VARCHAR(4),
    card_first6 VARCHAR(6),
    pos_entry_mode VARCHAR(2),
    transaction_type VARCHAR(20),
    is_voided BOOLEAN DEFAULT FALSE,
    redfynn_deposit_id VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_merchant FOREIGN KEY (mid) REFERENCES merchants(mid),
    INDEX idx_mid_date (mid, transaction_date),
    INDEX idx_batch (batch_number),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_redfynn_deposit (redfynn_deposit_id)
);
```

**Columns**:
- `id`: Primary key (auto-increment)
- `mid`: Merchant ID (foreign key to merchants)
- `transaction_id`: Unique transaction identifier from Privvy
- `batch_number`: Batch number for grouping transactions
- `amount`: Transaction amount (decimal with 2 decimal places)
- `transaction_date`: Date of the transaction
- `auth_code`: Authorization code
- `card_type`: Card type (VI, MC, AX, DC)
- `card_last4`: Last 4 digits of card number
- `card_first6`: First 6 digits of card number
- `pos_entry_mode`: POS entry mode (05=Chip, 02=Swipe, 01=Manual, 07=Contactless)
- `transaction_type`: Type of transaction (SALE, REFUND, etc.)
- `is_voided`: Whether transaction is voided
- `redfynn_deposit_id`: Deposit ID from Redfynn CRM
- `created_at`: Record creation timestamp

### retry_queue
Stores failed or empty transaction sync attempts for retry processing.

```sql
CREATE TABLE retry_queue (
    id BIGSERIAL PRIMARY KEY,
    mid VARCHAR(50) NOT NULL,
    process_date DATE NOT NULL,
    retry_count INT DEFAULT 0 NOT NULL,
    last_retry_at TIMESTAMPTZ,
    next_retry_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' NOT NULL,
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_retry_merchant FOREIGN KEY (mid) REFERENCES merchants(mid),
    CONSTRAINT chk_retry_status CHECK (status IN ('pending', 'completed', 'max_retries_reached', 'failed')),
    CONSTRAINT chk_retry_count CHECK (retry_count >= 0 AND retry_count <= 3),
    UNIQUE (mid, process_date),
    INDEX idx_status_next_retry (status, next_retry_at),
    INDEX idx_pending (status, next_retry_at, retry_count),
    INDEX idx_mid_date (mid, process_date)
);
```

**Columns**:
- `id`: Primary key (auto-increment)
- `mid`: Merchant ID (foreign key to merchants)
- `process_date`: Date for which transaction sync failed
- `retry_count`: Number of retry attempts (0-3)
- `last_retry_at`: Timestamp of last retry attempt
- `next_retry_at`: Timestamp for next retry attempt
- `status`: Retry status (pending, completed, max_retries_reached, failed)
- `error_message`: Error message from failed attempt
- `created_at`: Record creation timestamp
- `updated_at`: Last update timestamp

### processed_transactions
Tracks processed transactions to prevent duplicate processing (deduplication).

```sql
CREATE TABLE processed_transactions (
    id BIGSERIAL PRIMARY KEY,
    mid VARCHAR(50) NOT NULL,
    process_date DATE NOT NULL,
    transaction_id VARCHAR(100),
    privvy_batch_number VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    processed_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_processed_merchant FOREIGN KEY (mid) REFERENCES merchants(mid),
    CONSTRAINT chk_processed_status CHECK (status IN ('processing', 'completed', 'failed')),
    UNIQUE (mid, process_date, transaction_id, privvy_batch_number),
    INDEX idx_mid_date (mid, process_date),
    INDEX idx_status (status)
);
```

**Columns**:
- `id`: Primary key (auto-increment)
- `mid`: Merchant ID (foreign key to merchants)
- `process_date`: Date of transaction processing
- `transaction_id`: Transaction identifier
- `privvy_batch_number`: Batch number from Privvy
- `status`: Processing status (processing, completed, failed)
- `processed_at`: Timestamp when transaction was processed

### audit_logs
Stores audit trail for all system operations.

```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(100),
    user_id VARCHAR(100),
    action VARCHAR(50),
    details JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    INDEX idx_event_type (event_type),
    INDEX idx_created_at (created_at),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_user_id (user_id)
);
```

**Columns**:
- `id`: Primary key (auto-increment)
- `event_type`: Type of event (TRANSACTION_SYNC, MERCHANT_SYNC, RETRY_PROCESS, etc.)
- `entity_type`: Type of entity (merchant, transaction, retry_queue)
- `entity_id`: ID of the entity
- `user_id`: User who performed the action
- `action`: Action performed (CREATE, UPDATE, DELETE, SYNC)
- `details`: Additional details in JSON format
- `ip_address`: IP address of the requester
- `created_at`: Timestamp of the audit event

### application_logs
Stores application logs in the database for centralized logging.

```sql
CREATE TABLE application_logs (
    id BIGSERIAL PRIMARY KEY,
    log_level VARCHAR(20) NOT NULL,
    logger_name VARCHAR(255),
    message TEXT NOT NULL,
    exception_stack TEXT,
    thread_name VARCHAR(100),
    request_id VARCHAR(100),
    user_id VARCHAR(100),
    ip_address VARCHAR(45),
    endpoint VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT chk_log_level CHECK (log_level IN ('TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR')),
    INDEX idx_log_level (log_level),
    INDEX idx_created_at (created_at),
    INDEX idx_request_id (request_id),
    INDEX idx_endpoint (endpoint)
);
```

**Columns**:
- `id`: Primary key (auto-increment)
- `log_level`: Log level (TRACE, DEBUG, INFO, WARN, ERROR)
- `logger_name`: Name of the logger
- `message`: Log message
- `exception_stack`: Exception stack trace (if applicable)
- `thread_name`: Thread name
- `request_id`: Request ID for correlation
- `user_id`: User ID (if applicable)
- `ip_address`: IP address of the requester
- `endpoint`: API endpoint
- `created_at`: Timestamp of the log entry

## Scheduled Jobs

- **Transaction Sync**: Daily at 23:30 PST
- **Merchant Sync**: Daily at 01:00 PST
- **Retry Processing**: Daily at 03:00 PST

## Configuration

All configuration is in `src/main/resources/application.yml`. Key settings:

- `transaction-sync.processing.batch-size`: Number of merchants per batch (default: 5)
- `transaction-sync.processing.batch-delay-seconds`: Delay between batches (default: 2)
- `transaction-sync.processing.request-delay-seconds`: Delay between requests (default: 0.5)
- `transaction-sync.processing.max-retries`: Maximum retry attempts (default: 3)
- `transaction-sync.processing.retry-delay-hours`: Hours between retries (default: 24)

## Testing

Run backend tests:
```bash
mvn test
```

Run frontend tests:
```bash
cd frontend
npm test
```

## Deployment

### Docker Deployment

1. Build the image:
   ```bash
   docker build -t transaction-sync-service .
   ```

2. Run with docker-compose:
   ```bash
   docker-compose up -d
   ```

### Production Considerations

1. **Security**: Implement proper JWT authentication (currently disabled for development)
2. **Credentials**: Use AWS Secrets Manager or HashiCorp Vault for credentials
3. **Monitoring**: Set up Prometheus metrics and Grafana dashboards
4. **Logging**: Configure centralized logging (ELK stack, CloudWatch, etc.)
5. **Database**: Use managed PostgreSQL (RDS, Cloud SQL, etc.)
6. **Scaling**: Consider horizontal scaling with load balancer

## Troubleshooting

### Database Connection Issues
- Verify PostgreSQL is running
- Check connection credentials in `application.yml`
- Ensure database exists

### API Integration Issues
- Verify API credentials are set correctly
- Check network connectivity
- Review logs for detailed error messages

### Frontend Issues
- Ensure backend is running on port 8080
- Check browser console for errors
- Verify CORS configuration if accessing from different origin

## Next Steps

1. Implement JWT authentication
2. Add comprehensive unit and integration tests
3. Set up CI/CD pipeline
4. Configure monitoring and alerting
5. Add API documentation (Swagger/OpenAPI)
6. Implement advanced features from README.md



