# Reviq

> **AI Business Intelligence Layer for Small and Medium Enterprises**

Reviq is an AI chat service that connects to your existing business systems — CRM, loyalty platforms, ERP, accounting software, and banking APIs — and lets anyone in your organization ask questions about business data in plain language, without SQL queries, without dashboards, and without needing a data analyst.

---

## Table of Contents

- [What is Reviq](#what-is-reviq)
- [How it works](#how-it-works)
- [Architecture](#architecture)
- [Adapters and integrations](#adapters-and-integrations)
- [Capabilities](#capabilities)
- [Multi-tenant model](#multi-tenant-model)
- [Security and GDPR](#security-and-gdpr)
- [Tech stack](#tech-stack)
- [Deployment models](#deployment-models)
- [Roadmap](#roadmap)
- [Why Reviq](#why-reviq)

---

## What is Reviq

Reviq is not a CRM. It is not an ERP. It is not a dashboard tool. Reviq is an **intelligence layer** that sits on top of the systems your business already uses and turns data from those systems into a conversation.

Instead of opening Excel, filtering rows, and building pivot charts, a manager simply asks:

> *"Which customers haven't purchased anything in 60+ days but have a high RFM score?"*

Reviq finds the answer in real time from synchronized data, strips personal information before it reaches the AI model, and returns a concise answer with an actionable recommendation.

### Positioning

| Dimension | Description |
|-----------|-------------|
| **Product type** | AI Chat Intelligence Layer (SaaS) |
| **Target user** | Owners, managers, and analysts in SMBs |
| **Verticals** | Retail, e-commerce, services, finance |
| **Geography** | EU/CEE focus (GDPR-native) |
| **AI model** | Mistral AI (EU infrastructure) |
| **Deployment** | Cloud SaaS (shared or dedicated instance) |

---

## How it works

Reviq operates in four steps.

### 1. Data synchronization

Reviq connects to your client's systems through an **adapter library**. Each adapter knows how to communicate with a specific system (Salesforce, WooCommerce, Minimax, Tink Open Banking, etc.) and pulls data in a standardized format.

Synchronization runs periodically (every 15–60 minutes) or near-real-time via webhooks. Data is stored in an isolated tenant database that belongs exclusively to that client.

### 2. User asks a question

The user opens the Reviq chat and types a question in natural language:

```
"Which customer segment has the highest churn risk this month?"
"What does an average customer in segment B cost me?"
"When will I hit negative cash flow if I don't speed up collections?"
```

### 3. AI decides and executes

Reviq uses a **tool use** (function calling) architecture. The Mistral AI model reads the question, evaluates the available tools, and decides which query to execute:

```
User asks → AI reasons → Calls the right tool → Gets data → Formulates answer
```

All tools operate exclusively on anonymized data. The Mistral API **never receives** personal customer data such as names, email addresses, phone numbers, or IBANs.

### 4. Answer and recommendation

Reviq returns an answer in natural language with a concrete recommendation:

```
"I identified 94 customers at churn risk. Most belong to segment B
 with an average spend of €42. Recommendation: an SMS campaign with
 a 10% discount — this segment responds well to personalized offers."
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      Client systems                              │
│  CRM · Loyalty · ERP · E-commerce · Open Banking · Custom API   │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Reviq Sync Engine                             │
│          Adapter library · Normalization · Scheduling            │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│             Tenant database (per-client isolation)               │
│   Customers · Transactions · RFM · Cash flow · Financials · KPIs│
└──────────────────────────┬──────────────────────────────────────┘
                           │
                    ┌──────┴──────┐
                    │             │
                    ▼             ▼
          ┌─────────────┐  ┌──────────────────────────┐
          │ Anonymizer  │  │   Tool Registry            │
          │ (PII strip) │  │   @Tool annotations (Java) │
          └──────┬──────┘  └──────────────┬────────────┘
                 │                        │
                 └──────────┬─────────────┘
                            │
                            ▼
              ┌─────────────────────────┐
              │   Mistral AI API (EU)    │
              │   Function calling       │
              └─────────────┬───────────┘
                            │
                            ▼
              ┌─────────────────────────┐
              │     Reviq Chat UI        │
              │   WebSocket · React      │
              └─────────────────────────┘
```

### Core principles

**Data isolation** — Every client has its own PostgreSQL schema. Row-level security ensures that one client's data is never accessible to another.

**Anonymization before AI** — The Anonymizer layer intercepts all data before it reaches the Mistral API. Names, emails, phone numbers, and IBANs stay in the tenant database. The AI receives only statistical aggregates and internal IDs.

**Tool use architecture** — The AI never receives raw data. It receives a list of available tools with descriptions and decides which to call. This guarantees predictability and makes every interaction fully auditable.

**Adapter pattern** — Every integration is an isolated implementation of the `ReviqAdapter` interface. New systems are added without modifying the AI core.

---

## Adapters and integrations

Reviq uses a standardized adapter interface that enables integration with any system that exposes a REST API, a GraphQL endpoint, or direct database access.

### Planned adapters (by priority)

| Adapter | System | Vertical | Phase |
|---------|--------|----------|-------|
| `CustomRestAdapter` | Any REST API | All | Phase 1 |
| `WooCommerceAdapter` | WooCommerce | Retail / e-commerce | Phase 1 |
| `MinimaxAdapter` | Minimax | Accounting / CEE | Phase 2 |
| `TinkAdapter` | Tink Open Banking | Payments / EU | Phase 2 |
| `SalesforceAdapter` | Salesforce CRM | CRM / DACH | Phase 2 |
| `SapB1Adapter` | SAP Business One | ERP / Enterprise | Phase 3 |
| `PantheonAdapter` | Pantheon ERP | ERP / CEE | Phase 3 |
| `XeroAdapter` | Xero | Accounting / UK | Phase 3 |

### Adapter interface (Java)

```java
public interface ReviqAdapter {

    /**
     * Pulls data from the external system for a given time range.
     */
    AdapterData sync(TenantConfig config, SyncRange range);

    /**
     * Returns adapter metadata — name, version, supported entities.
     */
    AdapterMetadata describe();

    /**
     * Tests the connection and returns a status report.
     */
    ConnectionStatus testConnection(TenantConfig config);
}
```

Every adapter returns a standardized `AdapterData` object that the AI Core consumes without any knowledge of the underlying source system.

---

## Capabilities

### Customers and loyalty

| Question | What Reviq does |
|----------|----------------|
| "Who is likely to stop buying?" | Churn prediction based on RFM score and purchase frequency histogram |
| "Who are my VIP customers?" | RFM segmentation — Recency, Frequency, Monetary |
| "How many loyalty points expire unused?" | Loyalty engagement and redemption analytics |
| "Who drives the most long-term value?" | Customer Lifetime Value (CLV) calculation per customer |
| "What do customers buy together?" | Basket / association analysis for cross-sell opportunities |
| "Who hasn't purchased in 90+ days?" | Inactivity detection, win-back campaign list export |

### Sales and revenue

| Question | What Reviq does |
|----------|----------------|
| "How is sales trending this month?" | Revenue trend analysis with period-over-period comparison |
| "What are my best-selling products?" | Sales ranking by SKU, category, and time period |
| "Which channel performs better — online or in-store?" | Omnichannel revenue analysis |
| "Is there anything unusual in the data?" | Anomaly detection — statistical outlier identification |
| "What revenue should we expect next month?" | Sales forecasting based on historical patterns |
| "Why is the average basket smaller this week?" | AOV analysis by segment, period, and channel |

### Finance and accounting

| Question | What Reviq does |
|----------|----------------|
| "Which costs increased this quarter?" | Expense analysis with automatic anomaly flags |
| "Which customers are actually unprofitable?" | Customer profitability — revenue minus cost to serve |
| "Who is more than 30 days overdue on payment?" | AR aging analysis with a prioritized collections list |
| "Generate a P&L report for Q1." | Auto-generated narrative financial report |
| "Which products have the highest margin?" | Margin analysis by SKU, category, and channel |
| "Are we within the marketing budget?" | Budget vs. actual tracking with variance breakdown |

### Payments and cash flow

| Question | What Reviq does |
|----------|----------------|
| "Will I have enough to cover payroll on the 15th?" | Real-time cash flow monitoring from banking data |
| "When will I hit negative cash flow?" | 30/60/90-day cash flow forecast |
| "Are there any unusual transactions this month?" | Anomaly detection in transactions (Open Banking) |
| "Which vendors are taking the most money?" | Vendor spend analytics |
| "Which segment pays more reliably — A or B?" | Payment behavior analysis by customer segment |
| "What is my average collection period?" | Days Sales Outstanding (DSO) calculation |

### Reporting and automation

- **Automatic narrative reports** — monthly, quarterly, annual
- **Scheduled delivery** — email or Slack every Monday morning
- **Threshold alerts** — notify me when churn exceeds 5%
- **Segment export** — SMS campaign list directly from the chat
- **Period-over-period comparison** — this quarter vs. the same period last year
- **Ad-hoc queries** — any question, any time

---

## Multi-tenant model

Reviq uses a **tiered multi-tenancy** architecture that scales from a single client to thousands.

### Starter / Growth tier (Shared infrastructure)

Suitable for clients paying €149–€499 per month.

- Shared Spring Boot cluster with auto-scaling
- PostgreSQL with schema-per-tenant isolation
- Row-level security on every query
- Shared Mistral API pool with per-tenant rate limiting
- Instant onboarding, self-serve registration

### Enterprise tier (Dedicated instance)

Suitable for clients requiring full isolation, a custom SLA, or an on-premise deployment.

- Dedicated Spring Boot container
- Dedicated PostgreSQL with its own backup policies
- Custom Mistral API key — independent rate limit
- 99.9%+ SLA with dedicated support
- On-premise deployment option within the client's own infrastructure
- Custom configuration for sync intervals, data retention, and audit logs

### Tier migration

Migration from Shared to Enterprise is automated through the Control Plane and requires no downtime. Data is exported from the shared database, imported into the dedicated instance, and verified before the old connection is retired.

---

## Security and GDPR

### Data and privacy

**Personal customer data** (name, email, phone, address, IBAN) is stored exclusively in the tenant database and **never leaves the Reviq infrastructure**. The Mistral AI API receives only:

- Statistical aggregates (`count: 94`, `avg_value: 4200`)
- Internal IDs (`customer_id: 4821`)
- Timestamps and categories

The **Anonymizer Service** intercepts all data leaving the tenant database and strips PII before every AI call. This is an architectural mechanism, not a configuration option — it cannot be disabled.

### GDPR compliance

| Requirement | Implementation |
|-------------|----------------|
| Data residency | EU hosting (Hetzner Frankfurt / OVH) |
| AI model | Mistral AI — French company, EU infrastructure |
| Right to erasure | `DROP SCHEMA tenant_x CASCADE` on contract termination |
| Data Processing Agreement | Standard DPA included with every client contract |
| Encryption at rest | PostgreSQL TDE |
| Audit log | Every AI interaction is logged with timestamp, user_id, and tool_call metadata |

### EU AI Act readiness

Reviq is designed for compliance with the EU AI Act, which enters full effect in 2027:

- Every AI decision is traceable (which tool, which parameters, which data)
- Users can always inspect why the AI gave a specific answer
- No autonomous actions without explicit user approval (until Phase 3)

---

## Tech stack

### Backend

| Component | Technology |
|-----------|-----------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.x |
| AI integration | Spring AI + Mistral API |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Real-time communication | Spring WebSocket |
| Authentication | Spring Security + JWT |
| Scheduler | Spring Scheduler + Quartz |
| Build tool | Maven |

### Frontend

| Component | Technology |
|-----------|-----------|
| Framework | React 18 |
| WebSocket client | Native WebSocket API |
| Styling | TailwindCSS |
| Chat components | Custom implementation |

### Infrastructure

| Component | Technology |
|-----------|-----------|
| Containerization | Docker + Docker Compose |
| Orchestration (scale) | Kubernetes |
| Hosting | Hetzner EU / OVH Cloud |
| CI/CD | GitHub Actions |
| Monitoring | Prometheus + Grafana |

---

## Deployment models

### 1. Cloud SaaS (default)

Reviq hosts and manages the entire infrastructure. The client gains access through a web application or API key.

```
Client → Reviq Cloud (EU) → Mistral AI (EU)
```

Best for: the majority of SMB clients who do not want to manage infrastructure.

### 2. Dedicated Cloud

Reviq provisions a dedicated instance for the client within Reviq's cloud infrastructure. Full isolation, but Reviq still manages the servers.

```
Client → Reviq Dedicated Instance (EU) → Mistral AI (EU)
```

Best for: enterprise clients who require isolation but do not want their own servers.

### 3. On-premise (Enterprise)

Reviq is installed within the client's own infrastructure. Data never leaves the client's data center.

```
Client → Client's server (on-premise) → Mistral AI (EU)
                                        or local LLM
```

Best for: banks, insurance companies, and public institutions with strict data residency requirements.

---

## Roadmap

### Phase 1 — MVP (0–12 months)

Goal: product-market fit in the CEE region, first 10–50 paying clients.

- AI Core Service (Mistral, tool use, WebSocket chat)
- Custom REST API adapter (for in-house loyalty systems)
- WooCommerce adapter
- Tenant isolation (shared tier)
- Core use cases: churn, RFM, sales analytics, automated reports
- Self-serve onboarding
- Email alerts and scheduled report delivery

### Phase 2 — Scale (12–24 months)

Goal: CEE + DACH expansion, 200–500 clients, introduction of the financial vertical.

- Minimax adapter (accounting)
- Tink Open Banking adapter (6,000+ EU banks)
- Salesforce adapter
- Enterprise tier (dedicated instance)
- Financial use cases: cash flow, P&L, AR/AP analytics
- Multi-language support (English, German, Serbian)
- Slack / Teams integration for alert delivery

### Phase 3 — Platform (24–48 months)

Goal: EU-wide coverage, 2,000+ clients, agentic actions.

- SAP Business One adapter
- Pantheon ERP adapter
- Agentic actions — Reviq not only analyzes but acts (with explicit user approval)
- Benchmark analysis — anonymous comparison against industry averages
- Reviq API — clients embed Reviq within their own applications
- White-label option — partners build their own AI chat products on the Reviq platform
- On-premise deployment package

---

## Why Reviq

### The problem it solves

Small and medium-sized businesses in the EU have data spread across 3–5 different systems (CRM, loyalty, ERP, bank) but no data analyst to make sense of it. Excel reports are outdated the moment they are generated. Dashboards require technical expertise to build and maintain. Expensive enterprise tools such as Salesforce Einstein or Databricks Genie are out of reach for the SMB segment.

### Reviq's advantages

- **No migration** — Reviq connects to systems that already exist
- **No dashboards** — the user asks, Reviq answers
- **GDPR by design** — Mistral EU, anonymizer architecture, data residency in the EU
- **Vertical depth** — loyalty, sales, finance, and payments from a single chat interface
- **SMB pricing** — from €149/month, no setup fee, no annual contract for the starter tier
- **Adapter model** — every new adapter opens a new market without rewriting core logic

---

*Reviq is a project by E-Software d.o.o.*
# reviq-ai
