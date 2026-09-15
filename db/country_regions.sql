create table country_regions (
    merchant_country varchar(2) primary key,
    region varchar(10) not null,

    constraint chk_country_regions_region check (region in ('EU', 'US', 'UK'))
);