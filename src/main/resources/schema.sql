-- 会員情報テーブル
CREATE TABLE IF NOT EXISTS member_info (
    transaction_code      VARCHAR(20)  NOT NULL,
    store_name            VARCHAR(100),
    store_name_kana       VARCHAR(100),
    member_type           VARCHAR(20),
    parent_store_code     VARCHAR(20),
    parent_store_name     VARCHAR(100),
    new_transaction_code  VARCHAR(20),
    prev_transaction_code VARCHAR(20),
    middle_code           VARCHAR(20),
    block_code            VARCHAR(20),
    join_date             VARCHAR(20),
    corporation_flag      VARCHAR(20),
    cooperative_flag      VARCHAR(20),
    representative_name   VARCHAR(100),
    representative_kana   VARCHAR(100),
    postal_code           VARCHAR(20),
    address               VARCHAR(200),
    phone_number          VARCHAR(20),
    CONSTRAINT pk_member_info PRIMARY KEY (transaction_code)
);
