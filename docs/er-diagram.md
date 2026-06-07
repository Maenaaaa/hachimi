# Campus Exchange — Database ER Diagram

## Entities & Relationships

```
┌──────────────────┐       ┌──────────────────┐
│      user        │       │    user_auth     │
├──────────────────┤       ├──────────────────┤
│ id (PK)          │──┐    │ id (PK)          │
│ username (UQ)    │  │    │ user_id (FK)     │──────┐
│ password         │  │    │ real_name        │      │
│ nickname         │  │    │ student_id       │      │
│ avatar           │  │    │ id_card_image    │      │
│ phone            │  │    │ status           │      │
│ email            │  │    │ reject_reason    │      │
│ school           │  │    │ create_time      │      │
│ role             │  │    │ update_time      │      │
│ status           │  │    │ deleted          │      │
│ credit_score     │  │    └──────────────────┘      │
│ real_name        │  │                              │
│ student_id       │  │    ┌──────────────────┐      │
│ create_time      │  │    │    category      │      │
│ update_time      │  │    ├──────────────────┤      │
│ deleted          │  │    │ id (PK)          │      │
└──────────────────┘  │    │ name             │      │
       │              │    │ icon             │      │
       │ 1:N          │    │ sort_order       │      │
       ▼              │    │ create_time      │      │
┌──────────────────┐  │    │ update_time      │      │
│     goods        │  │    │ deleted          │      │
├──────────────────┤  │    └──────────────────┘      │
│ id (PK)          │  │           │                  │
│ title            │  │           │ 1:N              │
│ description      │  │           ▼                  │
│ original_price   │  │    ┌──────────────────┐      │
│ price            │  │    │  goods_image     │      │
│ condition        │  │    ├──────────────────┤      │
│ campus           │  │    │ id (PK)          │      │
│ category_id (FK) │──┼────│ goods_id (FK)    │      │
│ user_id (FK)     │──┘    │ image_url        │      │
│ status           │       │ sort_order       │      │
│ view_count       │       │ create_time      │      │
│ favorite_count   │       └──────────────────┘      │
│ create_time      │                                  │
│ update_time      │       ┌──────────────────┐      │
│ deleted          │       │   goods_view     │      │
└──────────────────┘       ├──────────────────┤      │
       │                   │ id (PK)          │      │
       │ 1:N               │ goods_id (FK)    │      │
       ▼                   │ user_id          │      │
┌──────────────────┐       │ view_time        │      │
│    favorite      │       └──────────────────┘      │
├──────────────────┤                                  │
│ id (PK)          │       ┌──────────────────┐      │
│ user_id (FK)     │───────│     follow       │      │
│ goods_id (FK)    │       ├──────────────────┤      │
│ create_time      │       │ id (PK)          │      │
└──────────────────┘       │ follower_id (FK) │──────┘
                            │ followee_id (FK) │──────┘
┌──────────────────┐       │ create_time      │
│  conversation    │       └──────────────────┘
├──────────────────┤
│ id (PK)          │       ┌──────────────────┐
│ goods_id (FK)    │       │    message       │
│ buyer_id (FK)    │───────├──────────────────┤
│ seller_id (FK)   │───────│ id (PK)          │
│ last_message     │       │ conversation_id  │
│ last_msg_time    │       │ sender_id (FK)   │
│ create_time      │       │ receiver_id (FK) │
│ update_time      │       │ content          │
│ deleted          │       │ message_type     │
└──────────────────┘       │ is_read          │
                            │ create_time      │
┌──────────────────┐       └──────────────────┘
│     orders       │
├──────────────────┤       ┌──────────────────┐
│ id (PK)          │       │   order_log      │
│ goods_id (FK)    │       ├──────────────────┤
│ buyer_id (FK)    │───────│ id (PK)          │
│ seller_id (FK)   │───────│ order_id (FK)    │
│ status           │       │ action           │
│ amount           │       │ operator_id      │
│ remark           │       │ remark           │
│ create_time      │       │ create_time      │
│ update_time      │       └──────────────────┘
│ deleted          │
└──────────────────┘       ┌──────────────────┐
                            │     review       │
┌──────────────────┐       ├──────────────────┤
│     report       │       │ id (PK)          │
├──────────────────┤       │ order_id (FK)    │
│ id (PK)          │       │ reviewer_id (FK) │
│ reporter_id (FK) │───────│ reviewee_id (FK) │
│ type             │       │ rating           │
│ target_id        │       │ content          │
│ reason           │       │ create_time      │
│ description      │       │ update_time      │
│ status           │       │ deleted          │
│ handler_id       │       └──────────────────┘
│ handle_note      │
│ handle_time      │       ┌──────────────────┐
│ create_time      │       │  notification    │
│ update_time      │       ├──────────────────┤
│ deleted          │       │ id (PK)          │
└──────────────────┘       │ user_id (FK)     │──────┐
                            │ type             │      │
┌──────────────────┐       │ title            │      │
│  announcement    │       │ content          │      │
├──────────────────┤       │ related_id       │      │
│ id (PK)          │       │ is_read          │      │
│ title            │       │ create_time      │      │
│ content          │       └──────────────────┘      │
│ publisher_id (FK)│───────                            │
│ create_time      │                                  │
│ update_time      │                                  │
│ deleted          │                                  │
└──────────────────┘                                  │
                                                       │
                 ALL FK references point to user.id ───┘
```

## Relationship Summary

| From | To | Cardinality | Description |
|------|-----|-------------|-------------|
| user | user_auth | 1:N | One user can have multiple verification attempts |
| user | goods | 1:N | One user publishes many goods |
| category | goods | 1:N | One category contains many goods |
| goods | goods_image | 1:N | One goods has many images |
| goods | goods_view | 1:N | One goods has many view records |
| user + goods | favorite | M:N | User favorites many goods |
| user + user | follow | M:N | User follows many users (self-referential) |
| user + user + goods | conversation | M:N | Two users have conversation about a goods |
| conversation | message | 1:N | One conversation has many messages |
| user | message | 1:N | User sends many messages |
| user + goods + user | orders | — | Buyer orders seller's goods |
| orders | order_log | 1:N | One order has many log entries |
| user + user + orders | review | — | Reviewer reviews reviewee for an order |
| user | report | 1:N | User submits many reports |
| user | notification | 1:N | User receives many notifications |
| user | announcement | 1:N | Admin publishes many announcements |

## Status Enums

**user.status**: ACTIVE, DISABLED
**user.role**: USER, ADMIN
**user_auth.status**: PENDING, APPROVED, REJECTED
**goods.status**: PENDING_REVIEW, ACTIVE, INACTIVE, REJECTED, TAKEN_DOWN
**goods.condition**: BRAND_NEW(全新), LIKE_NEW(几乎全新), MINOR_WEAR(轻微使用), VISIBLE_WEAR(明显使用), HEAVILY_USED(使用痕迹较重)
**orders.status**: PENDING(待确认), IN_PROGRESS(交易中), COMPLETED(已完成), CANCELLED(已取消)
**message.message_type**: TEXT, IMAGE
**report.type**: GOODS, USER
**report.status**: PENDING, APPROVED, REJECTED
**report.reason**: FALSE_INFO(虚假信息), FRAUD(欺诈行为), AD(广告内容), VIOLATION(违规内容)
**notification.type**: SYSTEM, ORDER, REVIEW
