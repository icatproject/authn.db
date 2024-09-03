-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;
CREATE TABLE IF NOT EXISTS PASSWD (USERNAME VARCHAR(255) NOT NULL PRIMARY KEY,ENCODEDPASSWORD VARCHAR(255) NOT NULL);

INSERT INTO PASSWD (USERNAME, ENCODEDPASSWORD) VALUES ('user1', 'sunshine');
INSERT INTO PASSWD (USERNAME, ENCODEDPASSWORD) VALUES ('user2', 'trainspotting');
INSERT INTO PASSWD (USERNAME, ENCODEDPASSWORD) VALUES ('user3', 'olympic');

