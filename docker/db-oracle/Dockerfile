FROM oracle/database:19.3.0-ee

ENV NLS_LANG=JAPANESE_JAPAN.AL32UTF8

COPY 01_create_user.sql /opt/oracle/scripts/setup/01_create_user.sql
COPY 02_create_tables.sql /opt/oracle/scripts/setup/02_create_tables.sql
COPY 03_insert_master_data.sql /opt/oracle/scripts/setup/03_insert_master_data.sql

