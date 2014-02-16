To Deploy:
Run the following where <tnsname> is the DBs tnsname:

sqlplus system@<tnsname> @as_system.sql

sqlplus /nolog @as_radar.sql <tnsname>


To Update:
Run the following where <tnsname> is the DBs tnsname and <release> is the version of the current release (e.g. 2.0.0-11):

sqlplus /nolog @update_as_radar.sql <release> <tnsname>
