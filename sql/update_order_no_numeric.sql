UPDATE orders SET order_no = CONCAT(
    LPAD(FLOOR(UNIX_TIMESTAMP(create_time) * 1000), 13, '0'),
    LPAD(id % 100000, 5, '0')
) WHERE order_no IS NOT NULL AND order_no LIKE 'ORD%';
