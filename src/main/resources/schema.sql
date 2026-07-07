-- 社員マスタ
CREATE TABLE IF NOT EXISTS m_employee (
    user_id              VARCHAR(255) NOT NULL,
    email                VARCHAR(255),
    password             VARCHAR(255),
    password_error_count INTEGER      NOT NULL DEFAULT 0,
    employee_name        VARCHAR(255),
    employee_name_kana   VARCHAR(255),
    department           VARCHAR(255),
    authority_code       VARCHAR(255),
    phone_number         VARCHAR(255),
    fax_number           VARCHAR(255),
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    update_user_id       VARCHAR(255),
    CONSTRAINT pk_m_employee PRIMARY KEY (user_id)
);

-- 会員情報テーブル
CREATE TABLE IF NOT EXISTS m_member_info (
    trade_code      VARCHAR(20)  NOT NULL,
    store_name            VARCHAR(100),
    store_name_kana       VARCHAR(100),
    member_type           VARCHAR(20),
    parent_store_code     VARCHAR(20),
    parent_store_name     VARCHAR(100),
    new_trade_code  VARCHAR(20),
    prev_trade_code VARCHAR(20),
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
    create_date           DATE         NOT NULL DEFAULT CURRENT_DATE,
    updated_date          DATE,
    update_employee       VARCHAR(50),
    CONSTRAINT pk_m_member_info PRIMARY KEY (trade_code)
);
DO $$
BEGIN
    ALTER TABLE m_member_info RENAME COLUMN registered_date TO create_date;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_member_info RENAME COLUMN updated_by TO update_employee;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_member_info RENAME COLUMN transaction_code TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_member_info RENAME COLUMN new_transaction_code TO new_trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_member_info RENAME COLUMN prev_transaction_code TO prev_trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS qualification_type          VARCHAR(20);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS branch_supplement_period_from DATE;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS branch_supplement_period_to   DATE;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS store_name_kana_short        VARCHAR(30);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS store_name_short             VARCHAR(30);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS pref_code                    SMALLINT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS city_code                    INTEGER;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS city_name                    VARCHAR(50);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_zip                     VARCHAR(7);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_pref                    VARCHAR(20);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_pref_kana               VARCHAR(40);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_city                    VARCHAR(50);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_city_kana               VARCHAR(100);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_town                    VARCHAR(50);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_town_kana               VARCHAR(100);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_block                   VARCHAR(50);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_block_kana              VARCHAR(100);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_building                VARCHAR(100);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_building_kana           VARCHAR(200);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_tel                     VARCHAR(20);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS addr_fax                     VARCHAR(20);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS mail_zip                     VARCHAR(7);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS mail_address                 VARCHAR(200);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS mail_tel                     VARCHAR(20);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS business_hours_weekday       VARCHAR(100);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS business_hours_weekday_note  TEXT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS business_hours_other         VARCHAR(100);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS business_hours_other_note    TEXT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS regular_holiday              VARCHAR(200);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS handling_items               TEXT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS closure_received_date        DATE;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS closure_start_date           DATE;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS closure_end_date             DATE;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS closure_contact              VARCHAR(100);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS closure_reason               TEXT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS closure_approver             VARCHAR(100);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS delivery_area_status         TEXT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS free_delivery_area_1         TEXT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS paid_delivery_area_1         TEXT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS free_delivery_area_2         TEXT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS paid_delivery_area_2         TEXT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS remarks                      TEXT;
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS account_holder_kana          VARCHAR(100);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS account_holder               VARCHAR(50);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS account_holder_birth         VARCHAR(8);
ALTER TABLE m_member_info ADD COLUMN IF NOT EXISTS mgmt_type                    VARCHAR(20);

-- 取り込みバッチ管理
CREATE TABLE IF NOT EXISTS m_import_batch (
    batch_id          SERIAL          NOT NULL,
    settlement_month  CHAR(6)         NOT NULL DEFAULT '',
    payment_type      VARCHAR(30)     NOT NULL,
    file_name         VARCHAR(255)    NOT NULL,
    imported_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    record_count      INTEGER,
    error_count       INTEGER,
    update_employee   VARCHAR(50),
    create_date       DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date      DATE,
    CONSTRAINT pk_m_import_batch PRIMARY KEY (batch_id)
);
DO $$
BEGIN
    ALTER TABLE m_import_batch ADD COLUMN settlement_month CHAR(6) NOT NULL DEFAULT '';
EXCEPTION WHEN duplicate_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_import_batch RENAME COLUMN updated_by TO update_employee;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_import_batch RENAME COLUMN registered_date TO create_date;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
ALTER TABLE m_import_batch ADD COLUMN IF NOT EXISTS error_count INTEGER;
-- trade_codeは1ファイルに複数店舗の行が混在するため、バッチ単位では代表値を
-- 一意に決められないケースがある。代わりにerror_countで正常／エラーを判別できる
-- ようにしたため廃止する（各明細テーブル側で行ごとにtrade_codeを保持している）。
ALTER TABLE m_import_batch DROP COLUMN IF EXISTS trade_code;
-- JFTD統合振込CSV作成で「既に振込CSVに含めたインポート分」を除外するためのマーカー。
-- NULL＝未処理（今回の集計対象）、値あり＝m_jftd_transfer_batch.transfer_batch_idで
-- 確定済みの振込バッチを指す（確定処理自体は別イテレーションで実装）。
ALTER TABLE m_import_batch ADD COLUMN IF NOT EXISTS transfer_batch_id INTEGER;
CREATE INDEX IF NOT EXISTS idx_import_transfer ON m_import_batch(transfer_batch_id);

-- JCB売上明細
CREATE TABLE IF NOT EXISTS m_jcb_sales_detail (
    jcb_sales_id      SERIAL          NOT NULL,
    trade_code  VARCHAR(50)     NOT NULL,
    batch_id          INTEGER         NOT NULL,
    store_name        VARCHAR(100),
    store_number      VARCHAR(50),
    card_company      VARCHAR(20),
    payment_method    VARCHAR(30),
    card_name         VARCHAR(50),
    payment_type      VARCHAR(10),
    sales_method      VARCHAR(5),
    sales_date        VARCHAR(10),
    sales_count       INTEGER         NOT NULL DEFAULT 0,
    sales_amount      INTEGER         NOT NULL DEFAULT 0,
    update_employee   VARCHAR(50),
    create_date       DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date      DATE,
    CONSTRAINT pk_m_jcb_sales_detail PRIMARY KEY (jcb_sales_id)
);
DO $$
BEGIN
    ALTER TABLE m_jcb_sales_detail RENAME COLUMN member_no TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_jcb_sales_detail RENAME COLUMN transaction_code TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_jcb_sales_detail RENAME COLUMN updated_by TO update_employee;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_jcb_sales_detail RENAME COLUMN registered_date TO create_date;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
CREATE INDEX IF NOT EXISTS idx_jcb_batch ON m_jcb_sales_detail(batch_id);
CREATE INDEX IF NOT EXISTS idx_jcb_store ON m_jcb_sales_detail(store_number, sales_date);

-- VISA/Master 加盟店ヘッダ（住信SBI 区分1）
CREATE TABLE IF NOT EXISTS m_visa_master_store_header (
    store_header_id        SERIAL          NOT NULL,
    trade_code       VARCHAR(50)     NOT NULL,
    batch_id               INTEGER         NOT NULL,
    file_created_date      DATE,
    sales_summary_date     DATE,
    parent_merchant_id     VARCHAR(20),
    parent_merchant_name   VARCHAR(100),
    merchant_id            VARCHAR(20),
    merchant_name          VARCHAR(100),
    transfer_date          DATE,
    total_sales_count      INTEGER         NOT NULL DEFAULT 0,
    total_sales_amount     INTEGER         NOT NULL DEFAULT 0,
    total_fee_amount_1     INTEGER         NOT NULL DEFAULT 0,
    total_payment_amount_1 INTEGER         NOT NULL DEFAULT 0,
    update_employee        VARCHAR(50),
    create_date            DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date           DATE,
    CONSTRAINT pk_m_visa_master_store_header PRIMARY KEY (store_header_id)
);
DO $$
BEGIN
    ALTER TABLE m_visa_master_store_header RENAME COLUMN member_no TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_visa_master_store_header RENAME COLUMN transaction_code TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_visa_master_store_header RENAME COLUMN updated_by TO update_employee;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_visa_master_store_header RENAME COLUMN registered_date TO create_date;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
CREATE INDEX IF NOT EXISTS idx_visa_hdr_batch    ON m_visa_master_store_header(batch_id);
CREATE INDEX IF NOT EXISTS idx_visa_hdr_merchant ON m_visa_master_store_header(merchant_id);

-- VISA/Master 取引明細（住信SBI 区分2）
CREATE TABLE IF NOT EXISTS m_visa_master_transaction (
    transaction_id              SERIAL          NOT NULL,
    trade_code            VARCHAR(50)     NOT NULL,
    batch_id                    INTEGER         NOT NULL,
    header_id                   INTEGER,
    parent_merchant_id          VARCHAR(20),
    merchant_id                 VARCHAR(20),
    transaction_no              VARCHAR(20),
    sales_date                  DATE,
    card_number_masked          VARCHAR(25),
    brand_type                  CHAR(1),
    payment_type_code           VARCHAR(5),
    payment_method              VARCHAR(20),
    fee_rate                    NUMERIC(5,2)    NOT NULL DEFAULT 0,
    sales_amount                INTEGER         NOT NULL DEFAULT 0,
    fee_amount_1                INTEGER         NOT NULL DEFAULT 0,
    deferred_amount             INTEGER         NOT NULL DEFAULT 0,
    deferred_fee                INTEGER         NOT NULL DEFAULT 0,
    transfer_deferred_amount    INTEGER         NOT NULL DEFAULT 0,
    transfer_deferred_fee       INTEGER         NOT NULL DEFAULT 0,
    payable_sales_amount        INTEGER         NOT NULL DEFAULT 0,
    payable_fee_amount          INTEGER         NOT NULL DEFAULT 0,
    payment_amount_1            INTEGER         NOT NULL DEFAULT 0,
    deferred_balance            INTEGER         NOT NULL DEFAULT 0,
    update_employee             VARCHAR(50),
    create_date                 DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date                DATE,
    CONSTRAINT pk_m_visa_master_transaction PRIMARY KEY (transaction_id)
);
DO $$
BEGIN
    ALTER TABLE m_visa_master_transaction RENAME COLUMN member_no TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_visa_master_transaction RENAME COLUMN transaction_code TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_visa_master_transaction RENAME COLUMN updated_by TO update_employee;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_visa_master_transaction RENAME COLUMN registered_date TO create_date;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
CREATE INDEX IF NOT EXISTS idx_visa_trn_batch    ON m_visa_master_transaction(batch_id);
CREATE INDEX IF NOT EXISTS idx_visa_trn_merchant ON m_visa_master_transaction(merchant_id);
CREATE INDEX IF NOT EXISTS idx_visa_trn_date     ON m_visa_master_transaction(sales_date);

-- ネットスターズ 店舗別集計
CREATE TABLE IF NOT EXISTS m_netstar_sales_summary (
    netstar_summary_id       SERIAL          NOT NULL,
    trade_code         VARCHAR(50)     NOT NULL,
    batch_id                 INTEGER         NOT NULL,
    store_code               VARCHAR(30),
    store_name               VARCHAR(100),
    total_count              INTEGER         NOT NULL DEFAULT 0,
    total_amount             INTEGER         NOT NULL DEFAULT 0,
    sales_count              INTEGER         NOT NULL DEFAULT 0,
    sales_amount             INTEGER         NOT NULL DEFAULT 0,
    refund_count             INTEGER         NOT NULL DEFAULT 0,
    refund_amount            INTEGER         NOT NULL DEFAULT 0,
    net_amount               INTEGER         NOT NULL DEFAULT 0,
    alipay_sales_count       INTEGER         NOT NULL DEFAULT 0,
    alipay_sales_amount      INTEGER         NOT NULL DEFAULT 0,
    alipay_refund_count      INTEGER         NOT NULL DEFAULT 0,
    alipay_refund_amount     INTEGER         NOT NULL DEFAULT 0,
    alipay_net_amount        INTEGER         NOT NULL DEFAULT 0,
    dpay_sales_count         INTEGER         NOT NULL DEFAULT 0,
    dpay_sales_amount        INTEGER         NOT NULL DEFAULT 0,
    dpay_refund_count        INTEGER         NOT NULL DEFAULT 0,
    dpay_refund_amount       INTEGER         NOT NULL DEFAULT 0,
    dpay_net_amount          INTEGER         NOT NULL DEFAULT 0,
    paypay_sales_count       INTEGER         NOT NULL DEFAULT 0,
    paypay_sales_amount      INTEGER         NOT NULL DEFAULT 0,
    paypay_refund_count      INTEGER         NOT NULL DEFAULT 0,
    paypay_refund_amount     INTEGER         NOT NULL DEFAULT 0,
    paypay_net_amount        INTEGER         NOT NULL DEFAULT 0,
    rakuten_sales_count      INTEGER         NOT NULL DEFAULT 0,
    rakuten_sales_amount     INTEGER         NOT NULL DEFAULT 0,
    rakuten_refund_count     INTEGER         NOT NULL DEFAULT 0,
    rakuten_refund_amount    INTEGER         NOT NULL DEFAULT 0,
    rakuten_net_amount       INTEGER         NOT NULL DEFAULT 0,
    smartcode_sales_count    INTEGER         NOT NULL DEFAULT 0,
    smartcode_sales_amount   INTEGER         NOT NULL DEFAULT 0,
    smartcode_refund_count   INTEGER         NOT NULL DEFAULT 0,
    smartcode_refund_amount  INTEGER         NOT NULL DEFAULT 0,
    smartcode_net_amount     INTEGER         NOT NULL DEFAULT 0,
    wechat_sales_count       INTEGER         NOT NULL DEFAULT 0,
    wechat_sales_amount      INTEGER         NOT NULL DEFAULT 0,
    wechat_refund_count      INTEGER         NOT NULL DEFAULT 0,
    wechat_refund_amount     INTEGER         NOT NULL DEFAULT 0,
    wechat_net_amount        INTEGER         NOT NULL DEFAULT 0,
    update_employee          VARCHAR(50),
    create_date              DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date             DATE,
    CONSTRAINT pk_m_netstar_sales_summary PRIMARY KEY (netstar_summary_id)
);
DO $$
BEGIN
    ALTER TABLE m_netstar_sales_summary RENAME COLUMN member_no TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_netstar_sales_summary RENAME COLUMN transaction_code TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_netstar_sales_summary RENAME COLUMN updated_by TO update_employee;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_netstar_sales_summary RENAME COLUMN registered_date TO create_date;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
CREATE INDEX IF NOT EXISTS idx_netstar_batch ON m_netstar_sales_summary(batch_id);
CREATE INDEX IF NOT EXISTS idx_netstar_store ON m_netstar_sales_summary(store_code);

-- 楽天ペイ取引明細
CREATE TABLE IF NOT EXISTS m_rakuten_pay_transaction (
    rakuten_transaction_id   SERIAL          NOT NULL,
    trade_code         VARCHAR(50)     NOT NULL,
    batch_id                 INTEGER         NOT NULL,
    order_key                VARCHAR(60),
    payment_status           VARCHAR(30),
    store_no                 VARCHAR(50),
    store_name               VARCHAR(100),
    shop_code                BIGINT,
    merchant_code            VARCHAR(30),
    total_amount             INTEGER         NOT NULL DEFAULT 0,
    created_at               TIMESTAMP,
    canceled_at              TIMESTAMP,
    update_employee          VARCHAR(50),
    create_date              DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date             DATE,
    CONSTRAINT pk_m_rakuten_pay_transaction PRIMARY KEY (rakuten_transaction_id)
);
DO $$
BEGIN
    ALTER TABLE m_rakuten_pay_transaction RENAME COLUMN member_no TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_rakuten_pay_transaction RENAME COLUMN transaction_code TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_rakuten_pay_transaction RENAME COLUMN updated_by TO update_employee;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_rakuten_pay_transaction RENAME COLUMN registered_date TO create_date;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
CREATE INDEX IF NOT EXISTS idx_rakuten_batch ON m_rakuten_pay_transaction(batch_id);
CREATE INDEX IF NOT EXISTS idx_rakuten_store ON m_rakuten_pay_transaction(store_no);

-- 端末月額利用料（スマレジ）
CREATE TABLE IF NOT EXISTS m_terminal_monthly_fee (
    terminal_fee_id          SERIAL          NOT NULL,
    trade_code         VARCHAR(50)     NOT NULL,
    batch_id                 INTEGER         NOT NULL,
    company_name             VARCHAR(50),
    billing_month            VARCHAR(10),
    billing_no               VARCHAR(20),
    issue_date               DATE,
    store_name               VARCHAR(100),
    terminal_id              VARCHAR(30),
    unit_price               INTEGER         NOT NULL DEFAULT 0,
    qty_credit               INTEGER         NOT NULL DEFAULT 0,
    qty_qr                   INTEGER         NOT NULL DEFAULT 0,
    qty_ic_transportation    INTEGER         NOT NULL DEFAULT 0,
    qty_ic_id                INTEGER         NOT NULL DEFAULT 0,
    qty_ic_waon              INTEGER         NOT NULL DEFAULT 0,
    qty_ic_nanaco            INTEGER         NOT NULL DEFAULT 0,
    qty_ic_edyrakuten        INTEGER         NOT NULL DEFAULT 0,
    qty_ic_quicpay           INTEGER         NOT NULL DEFAULT 0,
    qty_sim                  INTEGER         NOT NULL DEFAULT 0,
    tx_count_credit          INTEGER         NOT NULL DEFAULT 0,
    tx_count_qr              INTEGER         NOT NULL DEFAULT 0,
    tx_count_ic              INTEGER         NOT NULL DEFAULT 0,
    tx_count_total           INTEGER         NOT NULL DEFAULT 0,
    amount_credit            INTEGER         NOT NULL DEFAULT 0,
    amount_qr                INTEGER         NOT NULL DEFAULT 0,
    amount_ic                INTEGER         NOT NULL DEFAULT 0,
    amount_total             INTEGER         NOT NULL DEFAULT 0,
    update_employee          VARCHAR(50),
    create_date              DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date             DATE,
    CONSTRAINT pk_m_terminal_monthly_fee PRIMARY KEY (terminal_fee_id)
);
DO $$
BEGIN
    ALTER TABLE m_terminal_monthly_fee RENAME COLUMN member_no TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_terminal_monthly_fee RENAME COLUMN transaction_code TO trade_code;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_terminal_monthly_fee RENAME COLUMN updated_by TO update_employee;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
DO $$
BEGIN
    ALTER TABLE m_terminal_monthly_fee RENAME COLUMN registered_date TO create_date;
EXCEPTION WHEN undefined_column THEN NULL;
END $$;
CREATE INDEX IF NOT EXISTS idx_terminal_batch ON m_terminal_monthly_fee(batch_id);
CREATE INDEX IF NOT EXISTS idx_terminal_store ON m_terminal_monthly_fee(store_name, billing_month);

-- PAYGATE 店舗コードマッピング（会員コード紐付データ）
CREATE TABLE IF NOT EXISTS m_paygate_store_mapping (
    paygate_mapping_id       SERIAL          NOT NULL,
    trade_code               VARCHAR(10)     NOT NULL,
    store_name               VARCHAR(100),
    member_type              VARCHAR(10),
    terminal_id              VARCHAR(13),
    reader_serial_no         VARCHAR(20),
    sbi_merchant_id          VARCHAR(20),
    netstar_store_code       VARCHAR(20),
    jcb_merchant_no          VARCHAR(14),
    dnp_mgmt_no              VARCHAR(20),
    rpay_store_code          VARCHAR(20),
    terminal_status          VARCHAR(10),
    usage_intention          VARCHAR(10),
    usage_intention_updated  DATE,
    create_date              DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date             DATE,
    update_employee          VARCHAR(50),
    CONSTRAINT pk_m_paygate_store_mapping PRIMARY KEY (paygate_mapping_id)
);
CREATE INDEX IF NOT EXISTS idx_paygate_trade    ON m_paygate_store_mapping(trade_code);
CREATE INDEX IF NOT EXISTS idx_paygate_terminal ON m_paygate_store_mapping(terminal_id);
CREATE INDEX IF NOT EXISTS idx_paygate_sbi      ON m_paygate_store_mapping(sbi_merchant_id);

-- 統合振込バッチ（JFTD統合振込CSV作成の確定単位のヘッダー）
CREATE TABLE IF NOT EXISTS m_jftd_transfer_batch (
    transfer_batch_id  SERIAL          NOT NULL,
    created_at         TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_employee    VARCHAR(50),
    create_date        DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date       DATE,
    CONSTRAINT pk_m_jftd_transfer_batch PRIMARY KEY (transfer_batch_id)
);

-- 統合振込明細（確定時点の計算結果のスナップショット。CSV再ダウンロード・
-- 帳票集計はすべてここを参照し、確定後に元データが変わっても数値が変わらないようにする）
CREATE TABLE IF NOT EXISTS m_jftd_transfer_detail (
    transfer_detail_id  SERIAL          NOT NULL,
    transfer_batch_id   INTEGER         NOT NULL,
    trade_code          VARCHAR(50)     NOT NULL,
    item_code           VARCHAR(10)     NOT NULL,
    quantity            INTEGER         NOT NULL DEFAULT 1,
    amount              INTEGER         NOT NULL,
    update_employee     VARCHAR(50),
    create_date         DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date        DATE,
    CONSTRAINT pk_m_jftd_transfer_detail PRIMARY KEY (transfer_detail_id)
);
CREATE INDEX IF NOT EXISTS idx_transfer_detail_batch ON m_jftd_transfer_detail(transfer_batch_id);
CREATE INDEX IF NOT EXISTS idx_transfer_detail_trade ON m_jftd_transfer_detail(trade_code);

-- 項目コードマスタ（決済会社×カードブランド×金額種別→会計項目コード）
CREATE TABLE IF NOT EXISTS m_settlement_item_code (
    item_code_id     SERIAL          NOT NULL,
    payment_company  VARCHAR(30)     NOT NULL,
    card_brand       VARCHAR(30)     NOT NULL,
    amount_type      VARCHAR(10)     NOT NULL,
    item_code        VARCHAR(10)     NOT NULL,
    update_employee  VARCHAR(50),
    create_date      DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date     DATE,
    CONSTRAINT pk_m_settlement_item_code PRIMARY KEY (item_code_id),
    CONSTRAINT uq_settlement_item_code UNIQUE (payment_company, card_brand, amount_type)
);
CREATE INDEX IF NOT EXISTS idx_stlitem_code ON m_settlement_item_code(item_code);

-- card_brandは各決済会社の明細テーブルに実際に格納される値（例:
-- m_jcb_sales_detail.card_name）と一致させること。amount_typeは
-- PAYMENT(支払金額)/FEE_BASE(手数料本体)/FEE_TAX(消費税)の3種。
-- スマレジ(端末月額)は率計算ではなく単価×数量の固定額のためPAYMENTのみ。
-- JCB分のcard_brandは11_JCB.xlsxの生データ（＜JCB＞シートG列）の実値
-- （全角括弧付き表記、例:【ＪＣＢカード】）で突合済み。ただし【ディスカバー】は
-- 2025年11月分の実績が0件のため実値を確認できておらず、他ブランドの命名規則
-- から類推した値（要確認）。
INSERT INTO m_settlement_item_code
    (payment_company, card_brand, amount_type, item_code) VALUES
    ('住信SBI', 'Visa/Master', 'PAYMENT', '3300001'),
    ('住信SBI', 'Visa/Master', 'FEE_BASE', '3300003'),
    ('住信SBI', 'Visa/Master', 'FEE_TAX', '3300201'),
    ('ネットスターズ', 'Alipay', 'PAYMENT', '3300007'),
    ('ネットスターズ', 'Alipay', 'FEE_BASE', '3300009'),
    ('ネットスターズ', 'Alipay', 'FEE_TAX', '3300203'),
    ('ネットスターズ', 'PayPay', 'PAYMENT', '3300010'),
    ('ネットスターズ', 'PayPay', 'FEE_BASE', '3300204'),
    ('ネットスターズ', 'PayPay', 'FEE_TAX', '3300232'),
    ('ネットスターズ', 'd払い', 'PAYMENT', '3300013'),
    ('ネットスターズ', 'd払い', 'FEE_BASE', '3300205'),
    ('ネットスターズ', 'd払い', 'FEE_TAX', '3300233'),
    ('ネットスターズ', 'WeChatPay', 'PAYMENT', '3300004'),
    ('ネットスターズ', 'WeChatPay', 'FEE_BASE', '3300006'),
    ('ネットスターズ', 'WeChatPay', 'FEE_TAX', '3300202'),
    ('JCB', '【ＪＣＢカード】', 'PAYMENT', '3300024'),
    ('JCB', '【ＪＣＢカード】', 'FEE_BASE', '3300026'),
    ('JCB', '【ＪＣＢカード】', 'FEE_TAX', '3300207'),
    ('JCB', '【ＡＭＥＸカード】', 'PAYMENT', '3300027'),
    ('JCB', '【ＡＭＥＸカード】', 'FEE_BASE', '3300029'),
    ('JCB', '【ＡＭＥＸカード】', 'FEE_TAX', '3300208'),
    ('JCB', '【ダイナースクラブ】', 'PAYMENT', '3300030'),
    ('JCB', '【ダイナースクラブ】', 'FEE_BASE', '3300032'),
    ('JCB', '【ダイナースクラブ】', 'FEE_TAX', '3300209'),
    ('JCB', '【ディスカバー】', 'PAYMENT', '3300033'),
    ('JCB', '【ディスカバー】', 'FEE_BASE', '3300035'),
    ('JCB', '【ディスカバー】', 'FEE_TAX', '3300210'),
    ('JCB', '【銀聯カード】', 'PAYMENT', '3300036'),
    ('JCB', '【銀聯カード】', 'FEE_BASE', '3300038'),
    ('JCB', '【銀聯カード】', 'FEE_TAX', '3300211'),
    ('JCB', '【スマートコード】', 'PAYMENT', '3300039'),
    ('JCB', '【スマートコード】', 'FEE_BASE', '3300041'),
    ('JCB', '【スマートコード】', 'FEE_TAX', '3300212'),
    ('JCB', '【ＱＵＩＣＰａｙ】', 'PAYMENT', '3300046'),
    ('JCB', '【ＱＵＩＣＰａｙ】', 'FEE_BASE', '3300048'),
    ('JCB', '【ＱＵＩＣＰａｙ】', 'FEE_TAX', '3300214'),
    ('JCB', '【交通系電子マネー】', 'PAYMENT', '3300043'),
    ('JCB', '【交通系電子マネー】', 'FEE_BASE', '3300213'),
    ('JCB', '【交通系電子マネー】', 'FEE_TAX', '3300234'),
    ('JCB', '【ｎａｎａｃｏ】', 'PAYMENT', '3300049'),
    ('JCB', '【ｎａｎａｃｏ】', 'FEE_BASE', '3300215'),
    ('JCB', '【ｎａｎａｃｏ】', 'FEE_TAX', '3300235'),
    ('JCB', '【ＷＡＯＮ】', 'PAYMENT', '3300052'),
    ('JCB', '【ＷＡＯＮ】', 'FEE_BASE', '3300216'),
    ('JCB', '【ＷＡＯＮ】', 'FEE_TAX', '3300236'),
    ('楽天ペイ', '楽天ペイ', 'PAYMENT', '3300062'),
    ('楽天ペイ', '楽天ペイ', 'FEE_BASE', '3300223'),
    ('楽天ペイ', '楽天ペイ', 'FEE_TAX', '3300237'),
    ('スマレジ(端末月額)', '本体', 'PAYMENT', '3300217'),
    ('スマレジ(端末月額)', '調整', 'PAYMENT', '3300219')
ON CONFLICT (payment_company, card_brand, amount_type) DO NOTHING;

-- 手数料率マスタ（決済会社×カードブランドごとの計算モデル・手数料率）
CREATE TABLE IF NOT EXISTS m_settlement_fee_rate (
    fee_rate_id        SERIAL          NOT NULL,
    payment_company    VARCHAR(30)     NOT NULL,
    card_brand         VARCHAR(30)     NOT NULL,
    calc_model         VARCHAR(20)     NOT NULL,
    acquirer_fee_rate  NUMERIC(6,5),
    our_fee_rate_base  NUMERIC(6,5)    NOT NULL,
    our_fee_rate_tax   NUMERIC(6,5),
    update_employee    VARCHAR(50),
    create_date        DATE            NOT NULL DEFAULT CURRENT_DATE,
    updated_date       DATE,
    CONSTRAINT pk_m_settlement_fee_rate PRIMARY KEY (fee_rate_id),
    CONSTRAINT uq_settlement_fee_rate UNIQUE (payment_company, card_brand)
);

-- calc_model: STRAIGHT(直線式)/PURCHASE_COLLECT(仕入・収代二段階式)/
-- SBI_RESIDUAL(住信SBI残差式、acquirer_fee_rateは明細行のfee_rateを使うためNULL)。
-- スマレジ(端末月額)は率計算を行わない（単価×数量のみ）ためこのマスタに行を持たない。
-- PURCHASE_COLLECTのacquirer_fee_rateは「仕入手数料の本体分の率」を保持する
-- （消費税分は含まない。消費税は本体金額×10%を計算時に別途算出するため）。
-- ネットスターズ(PayPay・d払い)・楽天ペイの生データで、本体を四捨五入・消費税を
-- 本体×10%の切り捨てで計算すると実データと一致することを検証済み。
INSERT INTO m_settlement_fee_rate
    (payment_company, card_brand, calc_model, acquirer_fee_rate, our_fee_rate_base, our_fee_rate_tax) VALUES
    ('JCB', '【ＪＣＢカード】', 'STRAIGHT', 0.0275, 0.0018, 0.0002),
    ('JCB', '【ＡＭＥＸカード】', 'STRAIGHT', 0.0275, 0.0018, 0.0002),
    ('JCB', '【ダイナースクラブ】', 'STRAIGHT', 0.0275, 0.0018, 0.0002),
    ('JCB', '【ディスカバー】', 'STRAIGHT', 0.0275, 0.0018, 0.0002),
    ('JCB', '【銀聯カード】', 'STRAIGHT', 0.0275, 0.0018, 0.0002),
    ('JCB', '【スマートコード】', 'STRAIGHT', 0.025, 0.0041, 0.0004),
    ('JCB', '【ＱＵＩＣＰａｙ】', 'STRAIGHT', 0.0275, 0.0018, 0.0002),
    ('JCB', '【交通系電子マネー】', 'PURCHASE_COLLECT', 0.0227, 0.0041, 0.0004),
    ('JCB', '【ｎａｎａｃｏ】', 'PURCHASE_COLLECT', 0.0227, 0.0041, 0.0004),
    ('JCB', '【ＷＡＯＮ】', 'PURCHASE_COLLECT', 0.0227, 0.0041, 0.0004),
    ('ネットスターズ', 'Alipay', 'STRAIGHT', 0.017, 0.0025, 0.0003),
    ('ネットスターズ', 'PayPay', 'PURCHASE_COLLECT', 0.0265, 0.00032, 0.00003),
    ('ネットスターズ', 'd払い', 'PURCHASE_COLLECT', 0.026, 0.0008, 0.0001),
    ('ネットスターズ', 'WeChatPay', 'STRAIGHT', 0.017, 0.0025, 0.0003),
    ('楽天ペイ', '楽天ペイ', 'PURCHASE_COLLECT', 0.028, 0.0015, 0.0001),
    ('住信SBI', 'Visa/Master', 'SBI_RESIDUAL', NULL, 0.0032, NULL)
ON CONFLICT (payment_company, card_brand) DO NOTHING;

-- Stera 店舗情報
CREATE TABLE IF NOT EXISTS m_stera_store (
    record_no              BIGSERIAL          NOT NULL,
    trade_code             VARCHAR(10)        NOT NULL,
    transit_company        VARCHAR(10)        NOT NULL,
    edy_id                 VARCHAR(8)         NOT NULL,
    d_point_merchant_code  VARCHAR(6),
    d_point_store_code     VARCHAR(13),
    d_point_branch_code    VARCHAR(6),
    branch_code            VARCHAR(9)         NOT NULL,
    member_type            VARCHAR(10),
    store_name             VARCHAR(50)        NOT NULL,
    store_name_kana        VARCHAR(80)        NOT NULL,
    store_name_en          VARCHAR(80)        NOT NULL,
    store_zip              VARCHAR(7)         NOT NULL,
    store_address          VARCHAR(100)       NOT NULL,
    store_address_kana     VARCHAR(150)       NOT NULL,
    store_tel              VARCHAR(20)        NOT NULL,
    email                  VARCHAR(100)       NOT NULL,
    latitude               NUMERIC(10,7),
    longitude              NUMERIC(10,7),
    bank_name              VARCHAR(30)        NOT NULL,
    bank_code              VARCHAR(4)         NOT NULL,
    branch_name            VARCHAR(20)        NOT NULL,
    bank_branch_code       VARCHAR(3)         NOT NULL,
    account_type           VARCHAR(4)         NOT NULL,
    account_no             VARCHAR(7)         NOT NULL,
    account_holder_kana    VARCHAR(80)        NOT NULL,
    jcb_status             VARCHAR(1)         NOT NULL,
    jcb_start_date         DATE,
    d_point_status         VARCHAR(1)         NOT NULL,
    d_point_start_date     DATE,
    remarks                TEXT,
    created_at             TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_user_id        VARCHAR(50)        NOT NULL,
    CONSTRAINT pk_m_stera_store PRIMARY KEY (record_no)
);
CREATE INDEX IF NOT EXISTS idx_stera_store_trade ON m_stera_store(trade_code);

-- Stera 端末情報
CREATE TABLE IF NOT EXISTS m_stera_terminal (
    record_no              BIGSERIAL          NOT NULL,
    trade_code             VARCHAR(10)        NOT NULL,
    terminal_id            VARCHAR(13)        NOT NULL,
    jcb_merchant_no        VARCHAR(20),
    hana_cupid_mgmt_no_2   VARCHAR(6)         NOT NULL,
    branch_code            VARCHAR(9)         NOT NULL,
    terminal_status        VARCHAR(10)        NOT NULL,
    terminal_start_date    DATE               NOT NULL,
    terminal_end_date      DATE,
    created_at             TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_user_id        VARCHAR(50)        NOT NULL,
    CONSTRAINT pk_m_stera_terminal PRIMARY KEY (record_no)
);
CREATE INDEX IF NOT EXISTS idx_stera_terminal_trade ON m_stera_terminal(trade_code);
CREATE INDEX IF NOT EXISTS idx_stera_terminal_id    ON m_stera_terminal(terminal_id);

-- SMCC 加盟店番号
CREATE TABLE IF NOT EXISTS m_smcc_merchant_no (
    record_no       BIGSERIAL          NOT NULL,
    trade_code      VARCHAR(10)        NOT NULL,
    merchant_no     VARCHAR(10)        NOT NULL,
    type            VARCHAR(20)        NOT NULL,
    branch_code     VARCHAR(9)         NOT NULL,
    created_at      TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_user_id VARCHAR(50)        NOT NULL,
    CONSTRAINT pk_m_smcc_merchant_no PRIMARY KEY (record_no)
);
CREATE INDEX IF NOT EXISTS idx_smcc_trade ON m_smcc_merchant_no(trade_code);

-- テーブル所有者をアプリケーションユーザーに設定（postgresで実行した場合も正しく動作させる）
ALTER TABLE m_employee                  OWNER TO hanacupit;
ALTER TABLE m_member_info               OWNER TO hanacupit;
ALTER TABLE m_import_batch              OWNER TO hanacupit;
ALTER TABLE m_jcb_sales_detail          OWNER TO hanacupit;
ALTER TABLE m_visa_master_store_header  OWNER TO hanacupit;
ALTER TABLE m_visa_master_transaction   OWNER TO hanacupit;
ALTER TABLE m_netstar_sales_summary     OWNER TO hanacupit;
ALTER TABLE m_rakuten_pay_transaction   OWNER TO hanacupit;
ALTER TABLE m_terminal_monthly_fee      OWNER TO hanacupit;
ALTER TABLE m_paygate_store_mapping     OWNER TO hanacupit;
ALTER TABLE m_jftd_transfer_batch       OWNER TO hanacupit;
ALTER TABLE m_jftd_transfer_detail      OWNER TO hanacupit;
ALTER TABLE m_settlement_item_code      OWNER TO hanacupit;
ALTER TABLE m_settlement_fee_rate       OWNER TO hanacupit;
ALTER TABLE m_stera_store               OWNER TO hanacupit;
ALTER TABLE m_stera_terminal            OWNER TO hanacupit;
ALTER TABLE m_smcc_merchant_no          OWNER TO hanacupit;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO hanacupit;
