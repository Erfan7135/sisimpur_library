#!/bin/bash

# Start PostgreSQL service under the postgres user
service postgresql start

# Wait for PostgreSQL to be ready by attempting a full connection
echo "Waiting for PostgreSQL to be ready..."
until PGPASSWORD=machvaja psql -h localhost -U halum -d sisimpur -c '\q'; do
  >&2 echo "Postgres is unavailable - sleeping"
  sleep 1
done

>&2 echo "Postgres is up and ready for connections!"

# Start Spring Boot application
echo "Starting Spring Boot application..."
java -jar -Dspring.profiles.active=single /app/sisimpur-library.jar
