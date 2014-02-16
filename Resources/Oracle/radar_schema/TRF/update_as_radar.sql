def radar=RADAR
spool &radar
connect &radar/&radar@&2
set echo on
@@&1/update_TRF_radar_schema.sql
commit;
spool off