create table transactions (
    id varchar(37) not null primary key unique,
    date timestamp,
    account_number varchar(16),
    amount bigint
);

create table accounts (
  account_number varchar(16),
  balance bigint
)

