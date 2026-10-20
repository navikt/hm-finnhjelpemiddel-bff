#!/usr/bin/env node
// Derives data.isos22 for BFF categories from the verified ISO 2016 -> 2022 map in hm-grunndata-register.
// Read-only: writes a review CSV and a reviewable SQL file. Nothing is applied to any database.
//
//   node scripts/isos22-from-isomap.mjs [--isomap <file|url>] [--categories <file|url>] [--out <dir>]
//                                       [--include-review] [--overwrite-manual]
//
// --isomap            JSON array from GET <register>/api/v22/isomap/      (default scripts/isomap.json)
// --categories        JSON array from GET <bff>/admin/category            (default scripts/cats.json)
// --out               output directory                                    (default scripts/isos22-out)
// --include-review    also write SQL for `review` categories (split, merged, deleted or shared v22 codes)
// --overwrite-manual  also write SQL for categories whose current isos22 differs from the derived value
import { mkdir, readFile, writeFile } from 'node:fs/promises'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { parseArgs } from 'node:util'

const scriptDir = dirname(fileURLToPath(import.meta.url))

const { values: args } = parseArgs({
  options: {
    isomap: { type: 'string', default: join(scriptDir, 'isomap.json') },
    categories: { type: 'string', default: join(scriptDir, 'cats.json') },
    out: { type: 'string', default: join(scriptDir, 'isos22-out') },
    'include-review': { type: 'boolean', default: false },
    'overwrite-manual': { type: 'boolean', default: false },
  },
})

const load = async (source) => {
  if (!/^https?:\/\//.test(source)) return JSON.parse(await readFile(source, 'utf8'))
  const response = await fetch(source)
  if (!response.ok) throw new Error(`GET ${source} failed with HTTP ${response.status}`)
  return response.json()
}

const ISO_CODE = /^(?:\d{2}){1,4}$/
const CHANGING = new Set([
  'NEW_CODE_NEW_HEADER_SPLIT',
  'NEW_CODE_SAME_HEADER_SPLIT',
  'NEW_CODE_NEW_HEADER_MERGED',
  'SAME_OR_CHANGED_CODE_SAME_HEADER_MERGED',
  'DELETED_CLASS_OR_SUBCLASS_OR_SECTION',
])

const minimalPrefixes = (codes) =>
  [...new Set(codes)].sort().filter((code, i, all) => !all.slice(0, i).some((other) => code.startsWith(other)))

const sameSet = (a, b) => a.length === b.length && [...a].sort().every((code, i) => code === [...b].sort()[i])
const sqlString = (value) => `'${String(value).replaceAll("'", "''")}'`
// Leading = + - @ would be read as a formula by spreadsheet tools.
const csvCell = (value) => `"${String(value).replace(/^([=+\-@])/, "'$1").replaceAll('"', '""')}"`

const isomap = await load(args.isomap)
const categories = await load(args.categories)
const rows = isomap.filter((row) => row.code16)

const classify = (category) => {
  const isos = category.data?.isos ?? []
  const current = category.data?.isos22 ?? []
  const codes22 = []
  const unmapped = []
  let unverified = false
  let needsReview = false

  for (const prefix of isos) {
    const matches = rows.filter((row) => row.code16.startsWith(prefix))
    if (matches.length === 0) {
      unmapped.push(prefix)
      continue
    }
    if (matches.some((row) => !row.verified)) unverified = true
    if (matches.some((row) => row.mapEnum.some((e) => CHANGING.has(e)))) needsReview = true
    const mapped = matches.filter((row) => row.code22).map((row) => row.code22)
    if (mapped.length === 0) needsReview = true
    codes22.push(...mapped)
  }

  const isos22 = minimalPrefixes(codes22)

  // Reverse check: a v22 prefix must not also cover v16 codes from outside this category,
  // otherwise the v22 category would show products that belonged to another category in v16.
  const foreign = [
    ...new Set(
      rows
        .filter((row) => row.code22 && isos22.some((prefix) => row.code22.startsWith(prefix)))
        .filter((row) => !isos.some((prefix) => row.code16.startsWith(prefix)))
        .map((row) => row.code16)
    ),
  ].sort()
  if (foreign.length > 0) needsReview = true

  let status
  if (unmapped.length === isos.length) status = 'not mapped'
  else if (unmapped.length > 0) status = 'partly unmapped'
  else if (unverified) status = 'unverified'
  else if (isos22.some((code) => !ISO_CODE.test(code))) status = 'invalid code'
  else if (needsReview) status = 'review'
  else status = sameSet(isos22, isos) ? 'same' : 'renumbered'

  let action
  const writableStatus = ['same', 'renumbered', ...(args['include-review'] ? ['review'] : [])]
  if (!writableStatus.includes(status)) action = 'skip: status'
  else if (sameSet(current, isos22) && category.data?.isos22 != null) action = 'skip: up to date'
  else if (current.length > 0 && !args['overwrite-manual']) action = 'skip: manual'
  else action = 'update'

  return { isos, isos22, current, unmapped, foreign, status, action }
}

const results = categories
  .filter((category) => (category.data?.isos ?? []).length > 0)
  .map((category) => ({ category, ...classify(category) }))
  .sort((a, b) => a.category.title.localeCompare(b.category.title, 'nb'))

// Each update runs in a DO block that fails, and rolls back the whole transaction, when the row
// was changed after the export or does not exist. Run with: psql -v ON_ERROR_STOP=1 -f isos22-update.sql
const statements = results
  .filter((r) => r.action === 'update')
  .map(({ category, isos22 }) => {
    const id = sqlString(category.id)
    const stored = category.data.isos22 == null ? 'IS NULL' : `= ${sqlString(JSON.stringify(category.data.isos22))}::jsonb`
    return [
      `-- ${category.title.replaceAll(/[\r\n]/g, ' ')}`,
      'DO $isos22$ BEGIN',
      `  UPDATE category SET data = jsonb_set(data, '{isos22}', ${sqlString(JSON.stringify(isos22))}::jsonb)`,
      // NULLIF treats a stored JSON null like a missing key. The isos check aborts if the v16 source changed.
      `    WHERE id = ${id} AND NULLIF(data->'isos22', 'null'::jsonb) ${stored}`,
      `      AND data->'isos' = ${sqlString(JSON.stringify(category.data.isos))}::jsonb;`,
      `  IF NOT FOUND THEN RAISE EXCEPTION 'Category % was changed after export or is missing', ${id}; END IF;`,
      'END $isos22$;',
    ].join('\n')
  })

const csv = [
  ['category', 'id', 'status', 'action', 'v16_isos', 'v22_isos', 'current_isos22', 'unmapped_v16_prefixes', 'foreign_v16_codes'].join(','),
  ...results.map((r) =>
    [
      r.category.title,
      r.category.id,
      r.status,
      r.action,
      r.isos.join(' '),
      r.isos22.join(' '),
      r.current.join(' '),
      r.unmapped.join(' '),
      r.foreign.join(' '),
    ]
      .map(csvCell)
      .join(',')
  ),
].join('\n')

await mkdir(args.out, { recursive: true })
await writeFile(join(args.out, 'isos22-review.csv'), csv + '\n')
await writeFile(join(args.out, 'isos22-update.sql'), `\\set ON_ERROR_STOP on\nBEGIN;\n${statements.join('\n')}\nCOMMIT;\n`)

const count = (key) =>
  Object.entries(Object.groupBy(results, (r) => r[key]))
    .map(([value, rs]) => `${value}: ${rs.length}`)
    .join(', ')
console.log(`${results.length} categories with isos.`)
console.log(`Status: ${count('status')}`)
console.log(`Action: ${count('action')}`)
console.log(`${statements.length} updates written to ${join(args.out, 'isos22-update.sql')}`)
