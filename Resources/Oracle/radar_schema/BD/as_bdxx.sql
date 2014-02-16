
def bd=BD01
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD02
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD03
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD04
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD05
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD06
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD07
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD08
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD09
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD10
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD11
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

def bd=BD12
spool &bd
connect &bd/&bd@&1
set echo on
@@master_scripts/create_BD_radar_schema.sql
@@master_scripts/insert_BD_radar_metadata.sql
commit;
spool off

