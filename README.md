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

## Running the full stack locally with Docker Compose

`docker-compose-local.yml` starts the BFF (`localhost:8083`) and PostgreSQL (`localhost:5433`, data in a Docker volume).
Search is expected on `localhost:8080` (port-forward to dev) and can be overridden with `SEARCH_URL` and `GRUNNDATA_DB_URL`.

```
export DB_PASSWORD=<choose a local password>
./gradlew shadowJar
docker compose -f docker-compose-local.yml up --build
```

## ISO versions (16 and 22)

`GET /category/{title}` and `POST /category/ids` accept `?isoVersion=16|22` (default 16, other values return 400).
For `22` the response has the same shape as for 16: `data.isos` contains the values from `data.isos22` (an empty list
when `isos22` is not set) and `isoVersion` is `22`. Filters and subcategories are shared between the versions.
Clients therefore switch version by passing the parameter, without reading any `…22` field.

## Deriving `isos22` from the register ISO map

`scripts/isos22-from-isomap.mjs` prepares `data.isos22` from the verified ISO 2016 to 2022 map in hm-grunndata-register
(`GET /api/v22/isomap/`). It is read-only: it writes a review CSV and an SQL file, and applies nothing.

```
curl localhost:8083/admin/category > cats.json
node scripts/isos22-from-isomap.mjs --isomap <isomap.json|url> --categories cats.json --out isos22-out
```

- `isos22-review.csv`: every category with ISOs, with derived v22 codes, current `isos22`, unmapped prefixes and a status
  (`same`, `renumbered`, `review`, `unverified`, `partly unmapped`, `not mapped`).
- `isos22-update.sql`: one guarded `UPDATE … jsonb_set` per `same` or `renumbered` category, in one transaction. The guard
  skips a category that was edited after `cats.json` was fetched. `--include-review` also writes `review` categories
  (split, merged or deleted codes).

Review the CSV, then run the SQL yourself. Run the script again when the register team has verified more mappings.
Products only match v22 codes once the search index contains them.
