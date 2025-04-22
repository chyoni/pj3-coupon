create table coupon
(
    coupon_id            bigint       not null auto_increment primary key,
    title                varchar(255) not null comment '쿠폰명',
    coupon_type          varchar(255) not null comment '쿠폰 타입 (선착순 쿠폰, ...)',
    total_quantity       int comment '쿠폰 발급 최대 수량',
    issued_quantity      int          not null comment '발급된 쿠폰 수량',
    discount_amount      int          not null comment '할인 금액',
    min_available_amount int          not null comment '최소 사용 금액',
    date_issue_start     datetime     not null comment '발급 시작 일시',
    date_issue_end       datetime     not null comment '발급 종료 일시',
    created_at           datetime     not null comment '생성 일시',
    updated_at           datetime     not null comment '수정 일시'
);

create table coupon_issued
(
    issue_id    bigint   not null auto_increment primary key,
    coupon_id   bigint   not null comment '쿠폰 ID',
    user_id     bigint   not null comment '유저 ID',
    date_issued datetime not null comment '발급 일시',
    date_used   datetime comment '사용 일시',
    created_at  datetime not null comment '생성 일시',
    updated_at  datetime not null comment '수정 일시',
    constraint fk_coupon_issued_coupon_id foreign key (coupon_id) references coupon (coupon_id) on delete cascade
);