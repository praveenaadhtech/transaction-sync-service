# Transaction Sync Service - AWS Architecture

## Overview
This document describes the AWS architecture for the Transaction Sync Service, consisting of a Spring Boot backend and Angular frontend.

---

## Backend Architecture (Spring Boot)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           INTERNET / USERS                                   │
└───────────────────────────────────┬───────────────────────────────────────────┘
                                    │
                                    │ HTTPS (Port 443)
                                    │
┌───────────────────────────────────▼───────────────────────────────────────────┐
│                         AWS ROUTE 53 (DNS)                                     │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  Domain: api.transactionsync.com                                       │  │
│  │  Type: A Record (points to Elastic IP of EC2 instance)                │  │
│  │  TTL: 300 seconds                                                       │  │
│  │  Health Checks: Enabled (monitors /actuator/health endpoint)          │  │
│  │  Failover: Optional (can configure backup instance)                    │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────┬───────────────────────────────────────────┘
                                    │
                                    │ DNS Resolution
                                    │
┌───────────────────────────────────▼───────────────────────────────────────────┐
│                         AWS VPC (Virtual Private Cloud)                       │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  Region: us-east-1                                                       │  │
│  │  CIDR: 10.0.0.0/16                                                      │  │
│  │  Availability Zone: us-east-1a                                          │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    PUBLIC SUBNET                                        │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  Public Subnet                                                    │  │  │
│  │  │  10.0.1.0/24                                                       │  │  │
│  │  │  AZ: us-east-1a                                                    │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  │                                                                           │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  Internet Gateway (IGW)                                           │  │  │
│  │  │  - Routes traffic to/from internet                               │  │  │
│  │  │  - Attached to VPC                                                │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    PRIVATE SUBNET                                       │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  Private Subnet                                                   │  │  │
│  │  │  10.0.11.0/24                                                      │  │  │
│  │  │  AZ: us-east-1a                                                    │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  │                                                                           │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  NAT Gateway (for outbound internet access)                       │  │  │
│  │  │  - Allows private resources to access internet                    │  │  │
│  │  │  - Required for downloading dependencies, API calls              │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    SECURITY GROUPS                                       │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  Elastic Beanstalk Security Group                                   │  │  │
│  │  │  Inbound: Port 80 (HTTP), Port 443 (HTTPS) from 0.0.0.0/0        │  │  │
│  │  │  Outbound: All traffic                                             │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  RDS Security Group                                                │  │  │
│  │  │  Inbound: Port 5432 (PostgreSQL) from Beanstalk SG only          │  │  │
│  │  │  Outbound: None                                                    │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────┬───────────────────────────────────────────┘
                                    │
                                    │ Direct Connection (No Load Balancer)
                                    │
┌───────────────────────────────────▼───────────────────────────────────────────┐
│                    ELASTIC BEANSTALK (Spring Boot)                            │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  Application Name: transaction-sync-backend                             │  │
│  │  Environment: Production                                                 │  │
│  │  Platform: Java (Corretto 17)                                            │  │
│  │  Solution Stack: 64bit Amazon Linux 2023 v3.5.0                          │  │
│  │  Environment Type: Single Instance                                      │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    SINGLE EC2 INSTANCE                                   │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  EC2 Instance                                                     │  │  │
│  │  │  Instance Type: t3.medium (2 vCPU, 4 GB RAM)                     │  │  │
│  │  │  Elastic IP: Enabled (Static IP address)                          │  │  │
│  │  │  Spring Boot Application                                          │  │  │
│  │  │  Port: 8080 (HTTP), 8443 (HTTPS)                                  │  │  │
│  │  │  Health: Monitored via Route53 Health Checks                      │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    SPRING BOOT APPLICATION                                │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  - REST API Endpoints                                              │  │  │
│  │  │  - Transaction Processing                                          │  │  │
│  │  │  - Data Transformation                                              │  │  │
│  │  │  - Business Logic                                                   │  │  │
│  │  │  - Spring Security (JWT Authentication)                            │  │  │
│  │  │  - Actuator Endpoints (/health, /metrics)                          │  │  │
│  │  │  - Application Logging (to Database)                              │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    SPRING BOOT APPLICATION                                │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  - REST API Endpoints                                              │  │  │
│  │  │  - Transaction Processing                                          │  │  │
│  │  │  - Data Transformation                                              │  │  │
│  │  │  - Business Logic                                                   │  │  │
│  │  │  - Spring Security (JWT Authentication)                            │  │  │
│  │  │  - Actuator Endpoints (/health, /metrics)                          │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────┬───────────────────────────────────────────┘
                                    │
                                    │ Database Connection (JDBC)
                                    │ Port 5432
                                    │
┌───────────────────────────────────▼───────────────────────────────────────────┐
│                    AMAZON RDS POSTGRESQL                                      │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  Engine: PostgreSQL 15.4                                                 │  │
│  │  Instance Class: db.t3.medium (2 vCPU, 4 GB RAM)                        │  │
│  │  Storage: 100 GB (GP3, Auto-scaling up to 500 GB)                       │  │
│  │  Multi-AZ: Enabled (High Availability)                                   │  │
│  │  Backup Retention: 7 days                                                │  │
│  │  Automated Backups: Daily (3 AM UTC)                                      │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    DATABASE FEATURES                                      │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  - Encryption at Rest: Enabled (AWS KMS)                           │  │  │
│  │  │  - Encryption in Transit: SSL/TLS Required                          │  │  │
│  │  │  - Automated Backups                                                │  │  │
│  │  │  - Point-in-Time Recovery                                            │  │  │
│  │  │  - Read Replicas: Optional (for read scaling)                       │  │  │
│  │  │  - Performance Insights: Enabled                                    │  │  │
│  │  │  - Enhanced Monitoring: Enabled                                     │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    DATABASE SCHEMA                                        │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  Tables:                                                            │  │  │
│  │  │  - transactions (transaction data)                                  │  │  │
│  │  │  - merchants (merchant information)                                 │  │  │
│  │  │  - retry_queue (failed transaction retries)                         │  │  │
│  │  │  - audit_logs (system audit trail)                                  │  │  │
│  │  │  - application_logs (application logs stored in database)          │  │  │
│  │  │  - users (authentication & authorization)                           │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  │                                                                           │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  APPLICATION_LOGS TABLE STRUCTURE                                  │  │  │
│  │  │  ┌──────────────────────────────────────────────────────────────┐  │  │
│  │  │  │  CREATE TABLE application_logs (                             │  │  │
│  │  │  │    id BIGSERIAL PRIMARY KEY,                                 │  │  │
│  │  │  │    log_level VARCHAR(20) NOT NULL,                           │  │  │
│  │  │  │    logger_name VARCHAR(255),                                 │  │  │
│  │  │  │    message TEXT NOT NULL,                                   │  │  │
│  │  │  │    exception_stack TEXT,                                     │  │  │
│  │  │  │    thread_name VARCHAR(100),                                 │  │  │
│  │  │  │    created_at TIMESTAMPTZ DEFAULT NOW(),                    │  │  │
│  │  │  │    request_id VARCHAR(100),                                  │  │  │
│  │  │  │    user_id VARCHAR(100),                                     │  │  │
│  │  │  │    ip_address VARCHAR(45),                                   │  │  │
│  │  │  │    endpoint VARCHAR(255)                                     │  │  │
│  │  │  │  );                                                           │  │  │
│  │  │  │                                                                │  │  │
│  │  │  │  CREATE INDEX idx_log_level ON application_logs(log_level);  │  │  │
│  │  │  │  CREATE INDEX idx_created_at ON application_logs(created_at);│  │  │
│  │  │  │  CREATE INDEX idx_request_id ON application_logs(request_id); │  │  │
│  │  │  └──────────────────────────────────────────────────────────────┘  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────────┐
│                    AMAZON S3 (Object Storage - Optional)                    │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  Bucket Name: transaction-sync-service-storage-{environment}            │  │
│  │  Region: us-east-1                                                       │  │
│  │  Versioning: Enabled                                                     │  │
│  │  Encryption: AES-256 (Server-Side Encryption)                            │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    S3 BUCKET STRUCTURE (Optional)                         │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  /backups/ (database backups)                                      │  │  │
│  │  │  /exports/ (data exports)                                           │  │  │
│  │  │  /temp/ (temporary files)                                           │  │  │
│  │  │  /attachments/ (file uploads)                                       │  │  │
│  │  │                                                                      │  │  │
│  │  │  Note: Application logs are stored in database, not S3              │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Frontend Architecture (Angular)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           INTERNET / USERS                                   │
└───────────────────────────────────┬───────────────────────────────────────────┘
                                    │
                                    │ HTTPS (Port 443)
                                    │
┌───────────────────────────────────▼───────────────────────────────────────────┐
│                         AWS ROUTE 53 (DNS)                                     │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  Domain: app.transactionsync.com                                         │  │
│  │  Type: A Record (Alias)                                                  │  │
│  │  TTL: 300 seconds                                                         │  │
│  │  Health Checks: Enabled                                                   │  │
│  │  Failover: Active-Passive (Multi-Region)                                  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────┬───────────────────────────────────────────┘
                                    │
                                    │ DNS Resolution
                                    │
┌───────────────────────────────────▼───────────────────────────────────────────┐
│                         AWS CLOUDFRONT (CDN)                                   │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  Distribution ID: E1234567890ABC                                         │  │
│  │  Origin: S3 Bucket (transaction-sync-frontend)                            │  │
│  │  SSL Certificate: ACM (app.transactionsync.com)                          │  │
│  │  Caching Behavior: Optimize for static content                           │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    CLOUDFRONT FEATURES                                    │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  - Global Edge Locations (200+ locations worldwide)                │  │  │
│  │  │  - DDoS Protection (AWS Shield Standard)                            │  │  │
│  │  │  - SSL/TLS Termination                                              │  │  │
│  │  │  - Compression (Gzip/Brotli)                                         │  │  │
│  │  │  - Cache Invalidation Support                                       │  │  │
│  │  │  - Custom Error Pages                                                │  │  │
│  │  │  - Geo-Restriction (Optional)                                        │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    CACHE BEHAVIOR                                         │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  Static Assets (JS, CSS, Images):                                    │  │  │
│  │  │    - Cache-Control: max-age=31536000 (1 year)                      │  │  │
│  │  │    - Edge Cache TTL: 86400 seconds (24 hours)                       │  │  │
│  │  │                                                                      │  │  │
│  │  │  HTML Files:                                                        │  │  │
│  │  │    - Cache-Control: max-age=0 (no cache)                            │  │  │
│  │  │    - Edge Cache TTL: 0 seconds                                      │  │  │
│  │  │                                                                      │  │  │
│  │  │  API Proxy (to Backend):                                            │  │  │
│  │  │    - No caching                                                     │  │  │
│  │  │    - Forward all headers                                             │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────┬───────────────────────────────────────────┘
                                    │
                                    │ Origin Requests
                                    │
┌───────────────────────────────────▼───────────────────────────────────────────┐
│                         AMAZON S3 (Static Hosting)                            │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  Bucket Name: transaction-sync-frontend                                  │  │
│  │  Region: us-east-1                                                       │  │
│  │  Versioning: Enabled                                                     │  │
│  │  Encryption: AES-256 (Server-Side Encryption)                            │  │
│  │  Public Access: Blocked (CloudFront only)                               │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    S3 BUCKET CONFIGURATION                               │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  Website Hosting: Disabled (using CloudFront)                      │  │  │
│  │  │  Static Website Hosting: Disabled                                  │  │  │
│  │  │  Bucket Policy: CloudFront Origin Access Identity (OAI)           │  │  │
│  │  │  CORS Configuration: Enabled for API calls                         │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    ANGULAR APPLICATION FILES                              │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  /index.html (entry point)                                         │  │  │
│  │  │  /main.js (Angular bootstrap)                                       │  │  │
│  │  │  /polyfills.js (browser compatibility)                              │  │  │
│  │  │  /runtime.js (webpack runtime)                                      │  │  │
│  │  │  /styles.css (global styles)                                        │  │  │
│  │  │  /assets/ (images, fonts, icons)                                    │  │  │
│  │  │  /{feature-modules}/ (lazy-loaded modules)                           │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────┬───────────────────────────────────────────┘
                                    │
                                    │ API Calls (REST)
                                    │
┌───────────────────────────────────▼───────────────────────────────────────────┐
│                    BACKEND API (via Route53)                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  API Endpoint: https://api.transactionsync.com                          │  │
│  │  Authentication: JWT Token (stored in HttpOnly cookies)               │  │
│  │  CORS: Enabled for app.transactionsync.com                             │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────────┐
│                    ANGULAR APPLICATION STRUCTURE                              │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  Framework: Angular 17+                                                  │  │
│  │  Language: TypeScript                                                    │  │
│  │  Build: Production build (ng build --configuration production)          │  │
│  │  Output: Static files (HTML, CSS, JS)                                   │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    FRONTEND FEATURES                                     │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  - Transaction Dashboard                                           │  │  │
│  │  │  - Real-time Log Viewer                                            │  │  │
│  │  │  - Analytics & Reports                                             │  │  │
│  │  │  - System Monitoring                                               │  │  │
│  │  │  - User Authentication (Login/Logout)                               │  │  │
│  │  │  - Responsive Design (Mobile/Tablet/Desktop)                       │  │  │
│  │  │  - Progressive Web App (PWA) Support                                │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                    DEPLOYMENT PROCESS                                    │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  1. Build Angular app: ng build --configuration production         │  │  │
│  │  │  2. Upload dist/ folder to S3 bucket                               │  │  │
│  │  │  3. Invalidate CloudFront cache (if needed)                        │  │  │
│  │  │  4. New version available globally within minutes                   │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Component Explanations

### 1. AWS Route 53 (DNS Service)

**Purpose:**
- Domain name system (DNS) service that routes user requests to the correct AWS resources
- Provides domain registration and DNS resolution

**Key Features:**
- **Domain Management**: Register and manage domain names (e.g., transactionsync.com)
- **DNS Resolution**: Converts domain names to IP addresses
- **Health Checks**: Monitors endpoint health and routes traffic only to healthy resources
- **Failover**: Automatically routes traffic to backup resources if primary fails
- **Geolocation Routing**: Routes users to the nearest endpoint based on location
- **Weighted Routing**: Distributes traffic across multiple resources

**Configuration:**
- **Backend API**: `api.transactionsync.com` → Points directly to EC2 instance Elastic IP
- **Frontend App**: `app.transactionsync.com` → Points to CloudFront Distribution
- **TTL**: 300 seconds (5 minutes) for fast DNS updates
- **Record Types**: A records pointing to Elastic IP address
- **Health Checks**: Monitors `/actuator/health` endpoint for instance health

**Benefits:**
- High availability and reliability
- Low latency DNS resolution
- Automatic failover for disaster recovery
- Cost-effective DNS management

---

### 2. AWS VPC (Virtual Private Cloud)

**Purpose:**
- Creates an isolated virtual network in AWS where you can launch AWS resources
- Provides network-level security and control

**Key Components:**

**Subnets:**
- **Public Subnets**: Resources that need direct internet access (e.g., Load Balancers)
  - Route table includes Internet Gateway
  - Used for Elastic Beanstalk instances
- **Private Subnets**: Resources that should not be directly accessible from internet
  - Route table includes NAT Gateway for outbound access
  - Used for RDS databases

**Internet Gateway (IGW):**
- Allows communication between VPC resources and the internet
- Provides bidirectional internet access
- One IGW per VPC

**NAT Gateway:**
- Allows resources in private subnets to access the internet (outbound only)
- Required for downloading updates, API calls, etc.
- Provides high availability and automatic scaling
- Charges apply for data transfer

**Security Groups:**
- Virtual firewalls that control inbound and outbound traffic
- Stateful (return traffic automatically allowed)
- Rules based on IP addresses, ports, and protocols
- Applied at instance level

**Network ACLs:**
- Additional layer of security at subnet level
- Stateless (must define both inbound and outbound rules)
- Can block specific IP addresses

**Benefits:**
- Network isolation and security
- Control over IP address ranges
- Multiple availability zones for high availability
- Custom routing tables

---

### 3. AWS Elastic Beanstalk

**Purpose:**
- Platform-as-a-Service (PaaS) that simplifies deploying and managing applications
- Automatically handles capacity provisioning and application health monitoring
- Single instance deployment (no load balancer or auto-scaling)

**Key Features:**

**Application Management:**
- **Application**: Container for environments, versions, and configurations
- **Environment**: Running version of your application (e.g., Production, Staging)
- **Environment Type**: Single Instance (no load balancer)
- **Application Version**: Deployable code package (JAR/WAR file)
- **Configuration**: Environment-specific settings

**Single Instance Configuration:**
- **Instance Type**: t3.medium (2 vCPU, 4 GB RAM) - configurable
- **Elastic IP**: Static IP address assigned to instance
- **Direct Access**: Application accessible directly via Elastic IP
- **Health Monitoring**: Route53 health checks monitor `/actuator/health` endpoint
- **SSL/TLS**: Handled at application level (Spring Boot embedded server)

**Platform Support:**
- **Java Platform**: Corretto 17 (OpenJDK)
- **Solution Stack**: Amazon Linux 2023
- **Build Tools**: Maven or Gradle
- **Application Server**: Embedded Tomcat (Spring Boot)
- **Port Configuration**: HTTP (8080), HTTPS (8443)

**Deployment:**
- **Immutable Deployments**: Replace entire environment for updates
- **Rolling Deployments**: Not applicable (single instance)
- **CI/CD Integration**: Works with CodePipeline, GitHub Actions
- **Zero-Downtime**: Requires blue/green deployment strategy if needed

**Monitoring:**
- **CloudWatch Integration**: Automatic metrics collection
- **Log Streaming**: Real-time log access (logs stored in database)
- **Health Dashboard**: Visual status of environment
- **Event History**: Track all environment changes

**Benefits:**
- No infrastructure management required
- Simplified architecture (no load balancer overhead)
- Cost-effective (single EC2 instance)
- Easy deployment and rollback
- Built-in monitoring and logging
- Direct instance access for debugging

---

### 4. Amazon RDS PostgreSQL

**Purpose:**
- Managed relational database service running PostgreSQL
- Handles database administration tasks automatically

**Key Features:**

**High Availability:**
- **Multi-AZ Deployment**: Synchronous replication to standby in different AZ
- **Automatic Failover**: Promotes standby to primary in case of failure (< 60 seconds)
- **Read Replicas**: Asynchronous replication for read scaling (up to 5 replicas)

**Backup & Recovery:**
- **Automated Backups**: Daily snapshots with point-in-time recovery
- **Backup Retention**: Configurable (1-35 days, default 7 days)
- **Backup Window**: Configurable time window (e.g., 3 AM UTC)
- **Manual Snapshots**: On-demand backups retained indefinitely

**Security:**
- **Encryption at Rest**: AWS KMS encryption for data and backups
- **Encryption in Transit**: SSL/TLS connections required
- **Network Isolation**: VPC and security groups
- **IAM Database Authentication**: Use IAM roles instead of passwords
- **Database Audit Logging**: Track all database access

**Performance:**
- **Instance Classes**: Various sizes (t3.micro to r6g.16xlarge)
- **Storage Types**: 
  - **GP3**: General Purpose SSD (recommended)
  - **GP2**: General Purpose SSD (legacy)
  - **IO1/IO2**: Provisioned IOPS SSD (high performance)
- **Storage Auto-Scaling**: Automatically increases storage when needed
- **Performance Insights**: Database performance monitoring and analysis

**Monitoring:**
- **CloudWatch Metrics**: CPU, memory, storage, connections
- **Enhanced Monitoring**: Detailed metrics at 1-60 second intervals
- **Performance Insights**: Identify performance bottlenecks
- **Event Notifications**: Alerts for important events

**Maintenance:**
- **Automatic Patching**: OS and database engine updates
- **Maintenance Window**: Configurable time for updates
- **Minor Version Upgrades**: Automatic or manual
- **Major Version Upgrades**: Manual with downtime

**Application Logs Storage:**
- **Table**: `application_logs` - Stores all application logs in database
- **Benefits**: 
  - Centralized log storage with database
  - Easy querying and filtering via SQL
  - Integrated with application data
  - Automatic backup with database backups
  - No separate log management system needed
- **Log Levels**: ERROR, WARN, INFO, DEBUG, TRACE
- **Indexes**: Optimized for querying by log level, timestamp, and request ID
- **Retention**: Managed via database cleanup jobs or partitioning

**Benefits:**
- Fully managed service (no server management)
- High availability and durability
- Automated backups and point-in-time recovery
- Security best practices built-in
- Scalable storage and compute
- Cost-effective compared to self-managed databases
- Centralized log storage in database

---

### 5. Amazon S3 (Simple Storage Service)

**Purpose:**
- Object storage service for storing and retrieving any amount of data
- Used for static website hosting, backups, logs, and file storage

**Key Features:**

**Storage Classes:**
- **Standard**: General-purpose storage for frequently accessed data
- **Standard-IA**: Infrequent Access (lower cost, retrieval fee)
- **One Zone-IA**: Infrequent Access in single AZ (lower cost)
- **Glacier Instant Retrieval**: Archive with instant access
- **Glacier Flexible Retrieval**: Archive (3-5 hours retrieval)
- **Glacier Deep Archive**: Lowest cost (12 hours retrieval)
- **Intelligent-Tiering**: Automatically moves data to optimal tier

**Security:**
- **Encryption**: 
  - **Server-Side Encryption (SSE)**: AES-256 or AWS KMS
  - **Client-Side Encryption**: Encrypt before uploading
- **Access Control**:
  - **Bucket Policies**: Resource-based permissions
  - **ACLs**: Legacy access control lists
  - **IAM Policies**: User/role-based permissions
  - **CORS**: Cross-origin resource sharing configuration
- **Public Access**: Can be blocked entirely

**Versioning:**
- **Enabled**: Keeps multiple versions of objects
- **MFA Delete**: Require MFA to delete objects
- **Lifecycle Policies**: Automatically transition or delete old versions

**Lifecycle Management:**
- **Transitions**: Move objects between storage classes
- **Expiration**: Automatically delete objects after specified time
- **Cost Optimization**: Move to cheaper storage classes automatically

**Use Cases in This Architecture:**

**Backend S3 Bucket (Optional):**
- **Database Backups**: RDS snapshot exports
- **Data Exports**: CSV/JSON exports for reporting
- **Temporary Files**: Processing temporary files
- **File Attachments**: User-uploaded files (if needed)
- **Note**: Application logs are stored in PostgreSQL database (application_logs table), not in S3

**Frontend S3 Bucket:**
- **Static Website Hosting**: Angular application files
- **Assets**: Images, fonts, icons
- **Build Artifacts**: Compiled JavaScript, CSS, HTML

**Benefits:**
- Virtually unlimited storage
- 99.999999999% (11 9's) durability
- High availability (99.99% uptime SLA)
- Cost-effective storage
- Easy integration with other AWS services
- Versioning and lifecycle management
- Global accessibility via CloudFront

---

## Data Flow

### Backend Request Flow:
1. **User** → Route53 → Resolves `api.transactionsync.com` to EC2 Elastic IP
2. **Route53** → EC2 Instance (Elastic Beanstalk) → Spring Boot application processes request
3. **Spring Boot** → RDS PostgreSQL → Queries/updates database (including application logs)
4. **Spring Boot** → S3 → Retrieves files (if needed, optional)
5. **Response** → EC2 Instance → User

### Frontend Request Flow:
1. **User** → Route53 → Resolves `app.transactionsync.com` to CloudFront
2. **Route53** → CloudFront → Checks edge cache for content
3. **CloudFront** → S3 Bucket → Retrieves Angular files (if not cached)
4. **Browser** → Loads Angular application → Makes API calls to backend
5. **Angular** → Backend API (via Route53) → Receives data and displays

---

## Security Considerations

### Network Security:
- **VPC Isolation**: Resources isolated in private subnets
- **Security Groups**: Restrictive inbound/outbound rules
- **Network ACLs**: Additional subnet-level protection
- **Private Subnets**: Database not directly accessible from internet

### Data Security:
- **Encryption at Rest**: RDS and S3 encrypted
- **Encryption in Transit**: SSL/TLS for all connections
- **Secrets Management**: Use AWS Secrets Manager for credentials
- **IAM Roles**: Least privilege access

### Application Security:
- **Spring Security**: JWT authentication and authorization
- **CORS**: Configured for frontend domain only
- **Input Validation**: Server-side validation
- **SQL Injection Prevention**: Parameterized queries

---

## Cost Optimization

### Elastic Beanstalk:
- Use appropriate instance types (t3.medium for single instance)
- Single instance deployment reduces costs (no load balancer charges)
- Consider Reserved Instances for predictable workloads (up to 40% savings)

### RDS:
- Right-size instance based on workload
- Use Reserved Instances for predictable workloads (up to 75% savings)
- Enable storage auto-scaling to avoid over-provisioning
- Use Multi-AZ only for production

### S3:
- Use lifecycle policies to move old data to cheaper storage classes
- Enable S3 Intelligent-Tiering for automatic cost optimization
- Use S3 Transfer Acceleration only if needed

### CloudFront:
- Optimize cache headers to reduce origin requests
- Use appropriate TTL values
- Compress content (Gzip/Brotli)

---

## Monitoring & Logging

### CloudWatch Metrics:
- **Elastic Beanstalk**: CPU, memory, request count, latency
- **RDS**: CPU, memory, storage, connections, read/write IOPS
- **S3**: Request metrics, storage metrics
- **CloudFront**: Requests, data transfer, error rates

### CloudWatch Logs:
- **Application Logs**: Stored in PostgreSQL database (application_logs table)
- **Database Logs**: PostgreSQL logs from RDS
- **CloudFront Logs**: Access logs from CloudFront
- **System Logs**: EC2 instance system logs via CloudWatch agent

### Alarms:
- High CPU utilization
- High memory usage
- Database connection errors
- Application errors (5xx responses)
- Low disk space

---

## Disaster Recovery

### Backup Strategy:
- **RDS**: Automated daily backups with 7-day retention
- **S3**: Versioning enabled for all buckets
- **Elastic Beanstalk**: Application versions stored in S3

### Recovery Procedures:
- **Database**: Point-in-time recovery from automated backups
- **Application**: Rollback to previous version via Elastic Beanstalk
- **Frontend**: Restore from S3 versioning or redeploy

### High Availability:
- **Multi-AZ**: RDS in multiple availability zones (optional)
- **Single Instance**: EC2 instance in single availability zone
- **Route53 Health Checks**: Monitor instance health and alert on failures
- **Backup Strategy**: Regular database backups and application version storage
- **Recovery Time**: Manual failover to backup instance if needed

---

## Deployment Process

### Backend Deployment:
1. Build Spring Boot application (Maven/Gradle)
2. Create application version (JAR/WAR file)
3. Upload to Elastic Beanstalk
4. Deploy to environment (immutable deployment - replaces instance)
5. Monitor deployment via CloudWatch
6. Verify health endpoint (`/actuator/health`)

### Frontend Deployment:
1. Build Angular application (`ng build --configuration production`)
2. Upload `dist/` folder to S3 bucket
3. Invalidate CloudFront cache (if needed)
4. Verify deployment via browser

### CI/CD Integration:
- **GitHub Actions**: Automated builds and deployments
- **AWS CodePipeline**: End-to-end CI/CD pipeline
- **CodeBuild**: Build and test applications
- **CodeDeploy**: Deploy to Elastic Beanstalk

---

This architecture provides a scalable, secure, and cost-effective solution for the Transaction Sync Service with clear separation between frontend and backend components.

