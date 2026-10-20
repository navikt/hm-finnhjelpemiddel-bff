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
Input and output stay in `scripts/` and are ignored by git, because they contain category data.

```
curl -o scripts/cats.json localhost:8083/admin/category
curl -o scripts/isomap.json <register>/api/v22/isomap/
node scripts/isos22-from-isomap.mjs
```

Options: `--isomap`, `--categories` and `--out` override the default paths (a URL works for the inputs).
`--include-review` also writes SQL for `review` categories, and `--overwrite-manual` also writes SQL for categories whose
current `isos22` differs from the derived value.

`scripts/isos22-out/isos22-review.csv` lists every category with ISOs: derived v22 codes, current `isos22`, unmapped
v16 prefixes, foreign v16 codes, a `status` and an `action`.

| Status | Meaning |
|---|---|
| `same` / `renumbered` | All prefixes are mapped and verified, without split, merge or delete |
| `review` | Split, merged or deleted codes, or a v22 prefix that also covers v16 codes from outside the category (`foreign_v16_codes`) |
| `unverified` | At least one matching isomap row is not verified |
| `partly unmapped` / `not mapped` | Some or all v16 prefixes are missing from the isomap |

`action` is `update`, `skip: status`, `skip: up to date` or `skip: manual` (current `isos22` is non-empty and differs; an empty `[]` is treated as not set).

`scripts/isos22-out/isos22-update.sql` contains one `DO` block per `update`. Each block stops with an error, and the
transaction rolls back, if `isos` or `isos22` of the category was changed after `cats.json` was exported. Run it in dev first:

```
psql -v ON_ERROR_STOP=1 -f scripts/isos22-out/isos22-update.sql
```

Run the script again when the register team has verified more mappings. Products only match v22 codes once the search
index contains them.
