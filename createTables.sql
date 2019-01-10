create database email;
-- pause and set active

create schema email;

drop table if exists email.account;
create table email.account (
	id serial not null,
	domainid int not null,
	inboxName varchar(100) not null,
	username varchar(1000) not null,
	password varchar(1000) not null,
	dateCreated timestamp DEFAULT now()
);

drop table if exists email.domain;
create table email.domain (
	id serial not null,
	hostname varchar(1000) not null,
	port int not null,
	authentication varchar(1000) not null,
	dateCreated timestamp DEFAULT now()
);

drop table if exists email.message;
create table email.message (
	id serial not null,
	accountid int not null,
	subject varchar(5000) null,
	datereceived timestamp not null,
	recipientid int null,
	fromid int null,
	bodyid int null,
	datecreated timestamp not null
);

drop table if exists email.body;
create table email.body (
	id serial not null,
	body text not null
);

drop table if exists email.address;
create table email.address (
	id serial not null,
	groupind boolean null,
	grouplist varchar(1000) null,
	groupname varchar(1000) null,
	address varchar(1000) null,
	encodedPersonal varchar(1000) null
);

