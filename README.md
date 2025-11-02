# **Real-Time Alarm & Telemetry Intelligence Platform (RATIP)**

RATIP is a **serverless, event-driven platform** for ingesting, correlating, and analyzing **high-volume telemetry metrics** and **alarm events** in real time. It provides **context-aware operational insights** via an **AI-powered query interface**, helping engineers reduce incident response time, optimize costs, and improve reliability.

---

## **Table of Contents**

1. [Architecture Overview](#architecture-overview)
2. [Core Features](#core-features)
3. [Technology Stack](#technology-stack)
4. [Data Models](#data-models)
5. [API Endpoints](#api-endpoints)
6. [Deployment](#deployment)
7. [Getting Started](#getting-started)
8. [Testing](#testing)
9. [Future Enhancements](#future-enhancements)

---

## **Architecture Overview**

```
Services → Kinesis Streams → Lambda Validation → Kinesis Processing
                                                          ↓
                                    Spring Boot Consumers ← DynamoDB
                                            ↓
                                    Event Correlator
                                            ↓
                        ┌───────────────────┴────────────────────┐
                        ↓                                        ↓
                SNS Notifications                          AI Query Layer
                        ↓                                        ↓
                Slack/Email/PagerDuty                    ChatGPT API
```

**Key Flow:**

1. Services emit telemetry (API latency, Lambda metrics, DynamoDB usage).
2. CloudWatch alarms and custom alerts trigger EventBridge → Lambda.
3. Lambda functions validate, normalize, enrich, and push events to Kinesis streams.
4. Spring Boot microservices consume streams, correlate alarms with telemetry, aggregate metrics, and store results in DynamoDB.
5. Critical events trigger SNS notifications.
6. Users can query correlated events using **ChatGPT API (free tier GPT-4o / GPT-4.1 mini)**.

---

## **Core Features**

* **Event Ingestion Pipeline**

  * High-throughput Kinesis consumers with automatic shard management
  * Idempotent processing and retry logic
  * Dead-letter queue support

* **Correlation Engine**

  * Sliding window algorithm (default 15-minute correlation window)
  * Confidence scoring for correlations
  * Pattern detection: latency spikes, throttling, resource exhaustion

* **AI Query Processing**

  * Natural language queries mapped to structured telemetry queries
  * Context building from correlated events
  * OpenAI API integration with rate limiting

* **Real-Time Notifications**

  * SNS notifications for critical events
  * Priority-based routing and deduplication

* **Observability**

  * OpenTelemetry-ready instrumentation
  * CloudWatch dashboards for ingestion, processing, and error tracking

---

## **Technology Stack**

* **Framework**: Spring Boot 3.2.0
* **Language**: Java 17
* **Cloud**: AWS Lambda, Kinesis, DynamoDB, SNS, EventBridge
* **AI**: ChatGPT API (GPT-4o, GPT-4.1 mini free tier)
* **Observability**: OpenTelemetry, CloudWatch
* **Serialization**: Jackson with Java 8 Time support
* **Build Tool**: Maven

---

## **Data Models**

* **TelemetryEvent**: Composite key `serviceName#metricType` + `timestamp`
* **AlarmEvent**: Composite key `serviceName#severity` + `timestamp`
* **CorrelatedEvent**: Stores correlation results and confidence score

---

## **API Endpoints**

```
POST /api/v1/query
- Body: { "query": "Which services had throttling errors?" }
- Returns: AI-generated insights

GET /api/v1/health
- Returns: Service health status
```

---

## **Deployment**

1. **Lambda Functions**: Deploy as separate JARs with AWS Lambda runtime.
2. **Spring Boot Service**: Deploy to ECS, EKS, or EC2.
3. **Environment Variables**:

   * `OPENAI_API_KEY` for ChatGPT API
   * `AWS_SNS_TOPIC_ARN` for notifications
4. **IAM Permissions**:

   * Kinesis read/write
   * DynamoDB read/write
   * SNS publish

---

## **Getting Started**

1. Clone the repository:

```bash
git clone https://github.com/yourusername/ratip.git
```

2. Build with Maven:

```bash
mvn clean install
```

3. Configure `application.yml` with your AWS resources and OpenAI API key.
4. Deploy Lambda JARs and start Spring Boot microservice.
5. Test ingestion by sending sample telemetry and alarm events to Kinesis streams.

---

## **Testing**

* Unit tests for Lambda handlers and Spring Boot services
* Integration tests for event ingestion, correlation, and AI query processing
* Use **LocalStack** or **SAM Local** for local AWS emulation
* Simulate high-volume telemetry and alarm events to validate scaling

---

## **Future Enhancements**

* Multi-tenancy support for multiple service groups
* Historical trend dashboards and reporting
* AI query caching to reduce API costs
* Advanced alert suppression and dynamic thresholds
* SLA monitoring and automatic incident prioritization

---

**RATIP enables teams to**: reduce MTTR, optimize cloud costs, and gain actionable insights from telemetry and alarms in real time — all with AI-assisted intelligence. 🚀
