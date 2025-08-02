# Use Ubuntu as base image to install both PostgreSQL and Java
FROM ubuntu:22.04

# Avoid prompts from apt
ENV DEBIAN_FRONTEND=noninteractive

# Install required packages
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    postgresql \
    postgresql-contrib \
    curl \
    supervisor \
    && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Create app directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Make gradlew executable and build the application
RUN chmod +x gradlew && ./gradlew build -x test

# Setup PostgreSQL
USER postgres

# Initialize PostgreSQL database using postgres user (default authentication)
RUN /etc/init.d/postgresql start && \
    sleep 3 && \
    psql --command "CREATE USER halum WITH SUPERUSER PASSWORD 'machvaja';" && \
    createdb -O halum sisimpur && \
    psql -d sisimpur -c "GRANT ALL PRIVILEGES ON DATABASE sisimpur TO halum;" && \
    /etc/init.d/postgresql stop

# Copy and run database initialization script using postgres user
COPY db/init.sql /tmp/init.sql
RUN /etc/init.d/postgresql start && \
    sleep 5 && \
    psql -d sisimpur -f /tmp/init.sql && \
    psql -d sisimpur -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO halum;" && \
    psql -d sisimpur -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO halum;" && \
    /etc/init.d/postgresql stop

# Configure PostgreSQL for runtime authentication
RUN echo "host all all 0.0.0.0/0 md5" >> /etc/postgresql/14/main/pg_hba.conf && \
    sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" /etc/postgresql/14/main/postgresql.conf

# Switch back to root user
USER root

# Copy the built JAR file (find the specific JAR and copy it)
RUN find build/libs -name "*.jar" -not -name "*plain*" -exec cp {} /app/sisimpur-library.jar \;

# Create supervisor configuration
RUN mkdir -p /var/log/supervisor
COPY docker/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Create startup script
COPY docker/start-services.sh /start-services.sh
RUN chmod +x /start-services.sh

# Create application configuration for single container
COPY docker/application-single.yaml /app/application-single.yaml

# Expose application port
EXPOSE 8080

# Use supervisor to manage both PostgreSQL and Spring Boot
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
