# Reviq Management - Entity Structure

## Inheritance Hierarchy

```
BaseEntity (abstract)
├── id: UUID (PK, auto-generated)
├── version: Long (optimistic locking)
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime

SyncableEntity extends BaseEntity (abstract)
├── externalId: String        -- identifier from external system
└── syncedAt: LocalDateTime
```

The business context (RETAIL, FINANCE, etc.) is defined at the Tenant level
via `TenantType`, not per entity. A tenant has exactly one business context.

---

## Domain Packages

```
com.reviq.management.domain
├── location/        -- physical/logical locations (stores, warehouses, branches)
├── product/         -- catalog (products, services, financial products, brands, categories, promotions)
├── transaction/     -- transactions, payments, register sessions
├── account/         -- financial accounts (FINANCE context)
├── partner/         -- business partners and contacts (people)
├── loyalty/         -- loyalty cards, tiers, tier history
└── inventory/       -- stock levels and movements
```

---

## TenantType Coverage

The model is designed to support all tenant types with a single schema.
Not all entities are used by every tenant type.

| Entity | RETAIL | WHOLESALE | ECOMMERCE | FINANCE | SERVICES |
|--------|:------:|:---------:|:---------:|:-------:|:--------:|
| Location | stores | warehouses | fulfillment | branches | offices |
| Product | goods | goods | goods | fin. products | services |
| Category | product groups | product groups | product groups | instruments | service types |
| Brand | brands | brands | brands | - | - |
| Transaction | receipts | invoices | orders | fin. transactions | invoices |
| TransactionLine | items sold | items sold | items ordered | - (optional) | services billed |
| TransactionPayment | payments | payments | payments | payments | payments |
| PaymentMethod | cash/card/etc | bank transfer | online payment | transfer type | invoice/card |
| RegisterSession | POS sessions | - | - | - | - |
| Account | - | - | - | bank accounts | - |
| Partner | suppliers/customers | B2B partners | customers | clients | clients |
| Contact | loyal customers | - | customers | account holders | clients |
| LoyaltyCard | loyalty | - | loyalty | - | - |
| Inventory | stock levels | stock levels | stock levels | account positions | - |

---

## Entity Descriptions

### Location Domain

#### Location

Represents any physical or logical place where a tenant conducts business.
The meaning varies by tenant type: a retail store, a warehouse for wholesale
distribution, a fulfillment center for ecommerce, a bank branch, or a
service office. Locations form a hierarchy through self-referencing parent
relationships, enabling modeling of organizational structures like regional
groupings or multi-level warehouse networks.

```
┌──────────────────────────────────┐
│ Location (extends SyncableEntity)│
│ table: location                  │
├──────────────────────────────────┤
│ code: String (unique, not null)  │
│ name: String (not null)          │
│ type: LocationType               │
│ parent: Location (self-ref, FK)  │
│ children: List<Location>         │
│ street: String                   │
│ city: String                     │
│ zone: String                     │
│ active: Boolean (default true)   │
└──────────────────────────────────┘
```

**LocationType:** `STORE`, `WAREHOUSE`, `BRANCH`, `OFFICE`, `ONLINE`

| Type | When to use |
|------|-------------|
| STORE | Physical retail point of sale |
| WAREHOUSE | Storage/distribution center (retail, wholesale) |
| BRANCH | Bank or company branch office (finance) |
| OFFICE | Service provider office, headquarters |
| ONLINE | Virtual storefront, ecommerce channel |

---

### Product Catalog Domain

#### Product

A universal catalog item representing anything a tenant offers or trades in.
For retail/wholesale/ecommerce this is a physical product (goods) or service.
For finance, this represents a financial instrument or product offering — a
type of loan, deposit plan, or insurance policy. For services tenants, this
is the service being delivered and billed.

Every product belongs to a category and optionally to a brand. The
`purchasePrice` tracks the cost basis for margin analysis.

```
┌───────────────────────────────────┐
│ Product (extends SyncableEntity)  │
│ table: product                    │
├───────────────────────────────────┤
│ code: String (not null)           │
│ name: String (not null)           │
│ type: ProductType                 │
│ manufacturer: String              │
│ baseCode: String                  │
│ brand: Brand (FK)                 │
│ category: Category (FK)           │
│ purchasePrice: BigDecimal(16,2)   │
│ active: Boolean (default true)    │
└───────────────────────────────────┘
```

**ProductType:** `GOODS`, `SERVICE`, `FINANCIAL_PRODUCT`

| Type | When to use |
|------|-------------|
| GOODS | Physical products with inventory (retail, wholesale, ecommerce) |
| SERVICE | Intangible services billed by time, unit, or flat fee |
| FINANCIAL_PRODUCT | Financial instruments: loans, deposits, insurance policies, investment funds |

#### Brand

Groups products by their manufacturer or label. Primarily relevant for
retail, wholesale, and ecommerce tenants. Tracks whether a product is sold
under the tenant's own private label, which is important for margin analysis
and merchandising decisions.

```
┌───────────────────────────┐
│ Brand (extends BaseEntity)│
│ table: brand              │
├───────────────────────────┤
│ code: String (unique)     │
│ name: String (not null)   │
│ unitOfMeasure: String     │
│ privateLabel: Boolean     │
└───────────────────────────┘
```

#### Category

Hierarchical classification of products. Supports unlimited nesting through
self-referencing parent relationships. In retail this maps to product groups
(e.g., "Medications > Antibiotics"). In finance this could represent
instrument categories (e.g., "Loans > Consumer > Personal"). In services,
these are service categories (e.g., "Consulting > IT").

```
┌────────────────────────────────┐
│ Category (extends BaseEntity)  │
│ table: category                │
├────────────────────────────────┤
│ code: String (unique, not null)│
│ name: String (not null)        │
│ parent: Category (self-ref, FK)│
│ children: List<Category>       │
└────────────────────────────────┘
```

#### Promotion

A time-bound promotional campaign that applies special pricing or discounts
to specific products. Each promotion contains a list of items with their
discounted prices. Used primarily in retail and ecommerce contexts.

```
┌──────────────────────────────────────┐     ┌───────────────────────────────────┐
│ Promotion (extends SyncableEntity)   │     │ PromotionItem (extends BaseEntity)│
│ table: promotion                     │     │ table: promotion_item             │
├──────────────────────────────────────┤     ├───────────────────────────────────┤
│ validFrom: LocalDateTime (not null)  │◀─1M─│ promotion: Promotion (FK)         │
│ validTo: LocalDateTime (not null)    │     │ product: Product (FK)             │
│ items: List<PromotionItem>           │     │ price: BigDecimal(16,2)           │
└──────────────────────────────────────┘     │ discount: BigDecimal(16,2)        │
                                             └───────────────────────────────────┘
```

#### PromotionItem

A single product entry within a promotion, specifying the promotional price
and/or discount percentage for that product during the promotion period.

---

### Account Domain

#### Account

Represents a financial account — the central entity for FINANCE tenants.
An account is a persistent relationship between a partner (account holder)
and the business, with a running balance in a specific currency.

Optionally linked to a Product (the financial product, e.g., "Premium Savings
Account") and a Location (the branch where the account was opened).

Not used by RETAIL, WHOLESALE, ECOMMERCE, or SERVICES tenants.

```
┌──────────────────────────────────────────┐
│ Account (extends SyncableEntity)         │
│ table: account                           │
├──────────────────────────────────────────┤
│ accountNumber: String (unique, not null)  │
│ type: AccountType (not null)             │
│ currency: String(3) (not null)           │
│ partner: Partner (FK)                    │──MN──▶ Partner (account holder)
│ location: Location (FK)                  │──MN──▶ Location (branch)
│ product: Product (FK)                    │──MN──▶ Product (financial product)
│ balance: BigDecimal(16,2) (default 0)    │
│ active: Boolean (default true)           │
└──────────────────────────────────────────┘
```

**AccountType:** `CHECKING`, `SAVINGS`, `CREDIT`, `LOAN`, `INVESTMENT`

| Type | Description |
|------|-------------|
| CHECKING | Day-to-day transactional account |
| SAVINGS | Interest-bearing deposit account |
| CREDIT | Credit card or revolving credit line |
| LOAN | Term loan (personal, mortgage, business) |
| INVESTMENT | Investment or brokerage account |

---

### Transaction Domain

#### Transaction

The core event entity — represents any business event that involves the
exchange or movement of value. This is the most universal entity in the model:

- **RETAIL:** a receipt from a POS register
- **WHOLESALE:** an invoice or purchase order
- **ECOMMERCE:** an online order
- **FINANCE:** a deposit, withdrawal, or transfer between accounts
- **SERVICES:** an invoice for services rendered

Each transaction has a type, amount, timestamp, and optional relationships
to an account (finance), register session (retail POS), loyalty card,
and partner. The `channel` field identifies how the transaction originated
(POS terminal, web, mobile app, API integration).

```
┌──────────────────────────────────────────┐
│ Transaction (extends SyncableEntity)     │
│ table: transaction                       │
├──────────────────────────────────────────┤
│ transactionNumber: Long (not null)       │
│ type: TransactionType (not null)         │
│ channel: String                          │
│ documentType: Integer                    │
│ discount: BigDecimal(16,2)               │
│ amount: BigDecimal(16,2) (not null)      │
│ netAmount: BigDecimal(16,2)              │
│ refund: BigDecimal(16,2)                 │
│ transactionTime: LocalDateTime (not null)│
│ createdTime: LocalDateTime (not null)    │
│ account: Account (FK)                    │──MN──▶ Account (FINANCE only)
│ registerSession: RegisterSession (FK)    │──MN──▶ RegisterSession (RETAIL only)
│ loyaltyCard: LoyaltyCard (FK)            │──MN──▶ LoyaltyCard
│ partner: Partner (FK)                    │──MN──▶ Partner
│ loyaltyPointsMaster: Integer             │
│ loyaltyPointsPromo: Integer              │
│ isPreorder: Boolean (default false)      │
│ onlineOrderId: String                    │
│ lines: List<TransactionLine>             │
│ payments: List<TransactionPayment>       │
└──────────────────────────────────────────┘
```

**TransactionType:** `SALE`, `RETURN`, `REFUND`, `PAYMENT`, `TRANSFER`, `ADJUSTMENT`

| Type | Description |
|------|-------------|
| SALE | Standard sale of goods or services |
| RETURN | Customer returns goods, reversing a previous sale |
| REFUND | Money returned to customer (may not involve physical goods) |
| PAYMENT | Payment received or made (e.g., invoice payment, loan installment) |
| TRANSFER | Movement of value between accounts or locations |
| ADJUSTMENT | Manual correction of amounts, quantities, or balances |

**Channel values (String):** `"POS"`, `"WEB"`, `"MOBILE"`, `"API"`

#### TransactionLine

A single line item within a transaction — the detail of what was transacted.
In retail, this is a product sold with its quantity and price. In services,
this is a service rendered with its billing amount. In finance, lines are
optional — a simple transfer may have no line items.

The `product` field is nullable to support finance transactions that don't
involve catalog items.

```
┌──────────────────────────┐
│ TransactionLine          │
│ (extends BaseEntity)     │
│ table: transaction_line  │
├──────────────────────────┤
│ transaction: (FK)        │
│ lineNumber: Integer      │
│ product: Product (FK)*   │  * nullable for FINANCE
│ quantity: BigDecimal     │
│ price: BigDecimal        │
│ listPrice: BigDecimal    │
│ discount: BigDecimal     │
│ purchasePrice: BigDecimal│
│ retailAmount: BigDecimal │
│ taxAmount: BigDecimal    │
│ warehouse: String        │
│ salesType: String        │
│ earnLoyaltyPoints: Bool  │
│ lineType: TransLineType  │
│ refund: BigDecimal       │
└──────────────────────────┘
```

**TransactionLineType:** `SALE`, `RETURN`, `VOID`, `ADJUSTMENT`

#### TransactionPayment

Records how a transaction was paid. A single transaction may be split across
multiple payment methods (e.g., part cash, part card). Links the transaction
to the payment method used and the amount applied.

```
┌─────────────────────────────────────┐
│ TransactionPayment                  │
│ (extends BaseEntity)                │
│ table: transaction_payment          │
├─────────────────────────────────────┤
│ transaction: (FK)                   │
│ paymentMethod: PaymentMethod (FK)   │──MN──▶ PaymentMethod
│ warehouse: String                   │
│ amount: BigDecimal(16,2) (not null) │
│ salesType: String                   │
└─────────────────────────────────────┘
```

#### PaymentMethod

Defines the available ways to settle a transaction. Examples: cash, credit
card, bank transfer, mobile payment, invoice. Each tenant configures their
own set of payment methods depending on what their business accepts.

```
┌──────────────────────────────────────┐
│ PaymentMethod (extends SyncableEntity│
│ table: payment_method                │
├──────────────────────────────────────┤
│ code: String (unique, not null)      │
│ name: String (not null)              │
└──────────────────────────────────────┘
```

#### RegisterSession

Represents a POS cash register session — a specific register at a specific
location on a specific date. Used exclusively by RETAIL tenants to group
transactions by register and shift. Enables analysis of cashier performance,
register reconciliation, and shift-level reporting.

Not used by WHOLESALE, ECOMMERCE, FINANCE, or SERVICES tenants.

```
┌──────────────────────────────────────────┐
│ RegisterSession (extends SyncableEntity) │
│ table: register_session                  │
├──────────────────────────────────────────┤
│ location: Location (FK, not null)        │
│ registerNumber: Integer (not null)       │
│ sessionDate: LocalDate (not null)        │
└──────────────────────────────────────────┘
```

---

### Partner & Contact Domain

#### Partner

Represents a legal entity (company, organization) that the tenant does
business with. The `type` field distinguishes the role:

- **CUSTOMER** — buys from the tenant (B2B client, retail chain buyer)
- **SUPPLIER** — sells to the tenant (product vendor, service provider)
- **BOTH** — acts as both customer and supplier (common in wholesale)

In FINANCE context, partners are the institutional clients or corporate
account holders. In SERVICES, they are the companies being billed.

```
┌───────────────────────────────────────┐
│ Partner (extends SyncableEntity)      │
│ table: partner                        │
├───────────────────────────────────────┤
│ type: PartnerType (not null)          │
│ name: String (not null)               │
│ taxNumber: String                     │
│ registrationNumber: String            │
│ active: Boolean (default true)        │
│ address: String                       │
│ city: String                          │
│ postalCode: String                    │
│ phone: String                         │
│ fax: String                           │
└───────────────────────────────────────┘
```

**PartnerType:** `CUSTOMER`, `SUPPLIER`, `BOTH`

#### Contact

Represents a natural person (individual) connected to the business. In
retail, contacts are loyalty program members. In ecommerce, they are
registered customers. In finance, they are individual account holders.
In services, they are client representatives or end-users.

Contacts are distinct from Partners: a Partner is a legal entity (company),
while a Contact is a person. A Contact may be associated with a Partner
(e.g., a company employee who is the point of contact) through loyalty
cards or transactions.

```
┌───────────────────────────────────┐
│ Contact (extends SyncableEntity)  │
│ table: contact                    │
├───────────────────────────────────┤
│ firstName: String (not null)      │
│ lastName: String (not null)       │
│ gender: Gender                    │
│ dateOfBirth: LocalDate            │
│ street: String                    │
│ city: String                      │
│ postalCode: String                │
│ phone: String                     │
│ mobile: String                    │
│ email: String                     │
└───────────────────────────────────┘
```

**Gender:** `MALE`, `FEMALE`, `OTHER`

---

### Loyalty Domain

#### LoyaltyCard

Represents a customer loyalty card linked to a Contact. Tracks the card's
current point/balance, status, and tier. Used primarily by RETAIL and
ECOMMERCE tenants that run loyalty programs. Each card belongs to a single
contact and maintains a history of tier changes over time.

```
┌─────────────────────────────────────┐
│ LoyaltyCard (extends SyncableEntity)│
│ table: loyalty_card                 │
├─────────────────────────────────────┤
│ barcode: String                     │
│ balance: BigDecimal(16,2)           │
│ status: CardStatus (not null)       │
│ contact: Contact (FK)               │──MN──▶ Contact
│ currentTier: LoyaltyTier (FK)       │──MN──▶ LoyaltyTier
│ tierHistory: List<CardTierHistory>  │
└─────────────────────────────────────┘
```

**CardStatus:** `ACTIVE`, `INACTIVE`, `BLOCKED`, `EXPIRED`

#### LoyaltyTier

Defines a level within a loyalty program (e.g., Silver, Gold, Platinum).
Each tier has a discount percentage, a validity period, and an optional
threshold amount that a customer must spend to qualify. Tiers can be
amount-based or points-based depending on the loyalty program design.

```
┌──────────────────────────────────────┐
│ LoyaltyTier (extends SyncableEntity) │
│ table: loyalty_tier                  │
├──────────────────────────────────────┤
│ code: String (not null)              │
│ name: String (not null)              │
│ discount: BigDecimal(7,5) (not null) │
│ validFrom: LocalDateTime (not null)  │
│ validTo: LocalDateTime (not null)    │
│ thresholdAmount: BigDecimal(16,2)    │
│ pointsBased: Boolean (default false) │
│ loyaltyProgram: String (not null)    │
└──────────────────────────────────────┘
```

#### CardTierHistory

An audit trail of tier changes for a loyalty card. Every time a card moves
to a new tier (upgrade, downgrade, or renewal), a new history record is
created with the effective date range. Enables analysis of customer tier
progression and loyalty program effectiveness.

```
┌────────────────────────────────────────┐
│ CardTierHistory (extends BaseEntity)   │
│ table: card_tier_history               │
├────────────────────────────────────────┤
│ card: LoyaltyCard (FK, not null)       │
│ tier: LoyaltyTier (FK, not null)       │
│ validFrom: LocalDateTime (not null)    │
│ validTo: LocalDateTime (not null)      │
└────────────────────────────────────────┘
```

---

### Inventory Domain

#### Inventory

Tracks the current quantity of a product at a specific location. In retail
and wholesale, this is physical stock on hand. In ecommerce, this is
available inventory at a fulfillment center. The unique constraint on
(location, product) ensures one record per product per location.

For FINANCE tenants, inventory can optionally represent aggregated positions
(e.g., total deposits of a financial product at a branch), though account
balances are primarily tracked through the Account entity.

Not typically used by SERVICES tenants.

```
┌──────────────────────────────────────────┐
│ Inventory (extends BaseEntity)           │
│ table: inventory                         │
│ unique: (location_id, product_id)        │
├──────────────────────────────────────────┤
│ location: Location (FK, not null)        │
│ product: Product (FK, not null)          │
│ quantity: BigDecimal(16,3) (not null)    │
│ syncedAt: LocalDateTime                  │
└──────────────────────────────────────────┘
```

#### InventoryMovement

Records a change in inventory quantity — every increase or decrease is
logged as a movement with a type, quantity, and timestamp. This is the
audit trail for inventory changes. Enables analysis of stock flow,
shrinkage, transfer patterns, and supply chain efficiency.

```
┌──────────────────────────────────────────────┐
│ InventoryMovement (extends BaseEntity)       │
│ table: inventory_movement                    │
├──────────────────────────────────────────────┤
│ location: Location (FK, not null)            │
│ product: Product (FK, not null)              │
│ movementType: InventoryMovementType (not null│
│ quantity: BigDecimal(16,3) (not null)        │
│ recordedAt: LocalDateTime (not null)         │
└──────────────────────────────────────────────┘
```

**InventoryMovementType:** `RECEIPT`, `SALE`, `TRANSFER_IN`, `TRANSFER_OUT`, `ADJUSTMENT`, `WRITE_OFF`

| Type | Description |
|------|-------------|
| RECEIPT | Goods received from supplier |
| SALE | Quantity reduced due to sale |
| TRANSFER_IN | Stock received from another location |
| TRANSFER_OUT | Stock sent to another location |
| ADJUSTMENT | Manual inventory correction (count discrepancy) |
| WRITE_OFF | Stock removed due to damage, expiry, or loss |

---

## Shared Enums (reviq-shared)

| Enum | Values | Used By |
|------|--------|---------|
| `TenantType`* | RETAIL, WHOLESALE, ECOMMERCE, FINANCE, SERVICES | Tenant (reviq-tenancy) |
| `LocationType` | STORE, WAREHOUSE, BRANCH, OFFICE, ONLINE | Location |
| `ProductType` | GOODS, SERVICE, FINANCIAL_PRODUCT | Product |
| `AccountType` | CHECKING, SAVINGS, CREDIT, LOAN, INVESTMENT | Account |
| `TransactionType` | SALE, RETURN, REFUND, PAYMENT, TRANSFER, ADJUSTMENT | Transaction |
| `TransactionLineType` | SALE, RETURN, VOID, ADJUSTMENT | TransactionLine |
| `InventoryMovementType` | RECEIPT, SALE, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT, WRITE_OFF | InventoryMovement |
| `PartnerType` | CUSTOMER, SUPPLIER, BOTH | Partner |
| `CardStatus` | ACTIVE, INACTIVE, BLOCKED, EXPIRED | LoyaltyCard |
| `Gender` | MALE, FEMALE, OTHER | Contact |
| `SyncStatus` | PENDING, IN_PROGRESS, COMPLETED, FAILED | SyncJob |

*`TenantType` is in `reviq-tenancy-shared`, not `reviq-shared`

---

## Entity Count Summary

| Domain | Entity | Extends | Table |
|--------|--------|---------|-------|
| location | **Location** | SyncableEntity | `location` |
| product | **Product** | SyncableEntity | `product` |
| product | **Brand** | BaseEntity | `brand` |
| product | **Category** | BaseEntity | `category` |
| product | **Promotion** | SyncableEntity | `promotion` |
| product | **PromotionItem** | BaseEntity | `promotion_item` |
| account | **Account** | SyncableEntity | `account` |
| transaction | **Transaction** | SyncableEntity | `transaction` |
| transaction | **TransactionLine** | BaseEntity | `transaction_line` |
| transaction | **TransactionPayment** | BaseEntity | `transaction_payment` |
| transaction | **PaymentMethod** | SyncableEntity | `payment_method` |
| transaction | **RegisterSession** | SyncableEntity | `register_session` |
| partner | **Partner** | SyncableEntity | `partner` |
| partner | **Contact** | SyncableEntity | `contact` |
| loyalty | **LoyaltyCard** | SyncableEntity | `loyalty_card` |
| loyalty | **LoyaltyTier** | SyncableEntity | `loyalty_tier` |
| loyalty | **CardTierHistory** | BaseEntity | `card_tier_history` |
| inventory | **Inventory** | BaseEntity | `inventory` |
| inventory | **InventoryMovement** | BaseEntity | `inventory_movement` |

**Total: 19 entities** across 7 domain packages.
