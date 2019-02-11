create database email;
-- pause and set active

create schema email;

drop table if exists email.account;
create table email.account (
	id serial not null,
    hostname varchar(1000) not null,
	port int not null,
	authentication varchar(1000) not null,
	inboxName varchar(100) not null,
	username varchar(1000) not null,
	password varchar(1000) not null,
	dateCreated timestamp DEFAULT now()
);

drop table if exists email.message;
create table email.message (
	id serial,
	uid int not null,
	accountid int not null,
	subject varchar(5000) null,
	dateReceived timestamp not null,
	readInd boolean not null,
	recipientid int null,
	fromid int null,
	dateCreated timestamp default now(),
	primary key (accountid, subject, dateReceived)
);

drop table if exists email.bodyPart;
create table email.bodyPart (
	messageid int not null,
	seqnum int not null,
	contenttype varchar(1000) not null,
	content bytea
);

drop table if exists email.executionlog;
create table email.executionlog (
	id serial not null,
	datecreated timestamp default now(),
	execstatusenum int not null
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