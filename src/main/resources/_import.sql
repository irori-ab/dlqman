create table Message
(
  id               uuid not null
    primary key,
  destinationtopic varchar(255),
  fingerprint      varchar(255),
  matchedrule      varchar(255),
  processat        timestamp,
  sourceid         varchar(255),
  sourceoffset     bigint,
  sourcepartition  integer,
  sourcetopic      varchar(255),
  status           varchar(255),
  waittime         bigint
);

alter table Message
  owner to quarkus;

create table Metadata
(
  id         uuid not null
    primary key,
  key        varchar(255),
  type       integer,
  value      varchar(255),
  message_id uuid
    constraint fk7w7lci8oe9alulmskxmxupsgy
      references message
);

alter table Metadata
  owner to quarkus;

