def radar=RADAR
spool &radar
connect &radar/&radar@&1
set echo on
@@master_scripts/create_TRF_radar_schema.sql
@@master_scripts/insert_TRF_radar_metadata.sql
commit;
spool off