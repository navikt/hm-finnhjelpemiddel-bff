# hm-finnhjelpemiddel-bff

## Running hm-finnhjelpemiddel-bff locally:

```
cd hm-finnhjelpemiddel-bff
docker-compose up -d

export DB_DRIVER=org.postgresql.Driver
export DB_JDBC_URL=jdbc:postgresql://localhost:5435/finnhjelpemiddelbff
export SERVER_PORT=1338

./gradlew build run
```