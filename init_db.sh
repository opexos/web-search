set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER websearch PASSWORD 'websearch';
    CREATE SCHEMA AUTHORIZATION websearch;
    ALTER USER websearch SET search_path = websearch;
EOSQL
