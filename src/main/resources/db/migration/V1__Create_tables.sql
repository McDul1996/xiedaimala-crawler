create table news (
id bigint primary key auto_increment,
title text,
content text,
url varchar(10000),
created_at timestamp,
modified_at timestamp default now()
) Default CHARSET=utf8mb4;

create table LINKS_TO_BE_PROCESSED (link varchar(10000));

create table LINKS_ALREADY_PROCESSED (link varchar(10000));
