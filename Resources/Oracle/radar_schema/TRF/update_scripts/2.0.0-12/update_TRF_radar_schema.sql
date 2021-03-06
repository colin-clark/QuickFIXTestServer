CREATE TABLE TEMP_USER_ACTIVITY_HISTORY 
    ( 
     USER_ACTIVITY_ID NUMBER (10) , 
     UPDATE_DATETIME TIMESTAMP , 
     USER_ID NUMBER  NOT NULL , 
     ACTIVITY_SOURCE VARCHAR2 (500 BYTE)  NOT NULL , 
     ACTIVITY_DETAILS VARCHAR2 (4000)  NOT NULL , 
     ACTIVITY VARCHAR2 (500 BYTE)  NOT NULL 
    )         
/

INSERT INTO TEMP_USER_ACTIVITY_HISTORY(SELECT * FROM USER_ACTIVITY_HISTORY)
/

TRUNCATE TABLE USER_ACTIVITY_HISTORY
/

ALTER TABLE USER_ACTIVITY_HISTORY MODIFY ACTIVITY_SOURCE VARCHAR2(500 BYTE)
/

ALTER TABLE USER_ACTIVITY_HISTORY MODIFY ACTIVITY_DETAILS VARCHAR2(4000)
/

ALTER TABLE USER_ACTIVITY_HISTORY MODIFY ACTIVITY VARCHAR2(500 BYTE)
/

INSERT INTO USER_ACTIVITY_HISTORY(SELECT * FROM TEMP_USER_ACTIVITY_HISTORY)
/

DROP TABLE TEMP_USER_ACTIVITY_HISTORY
/