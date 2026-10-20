#!/usr/bin/env node
// Derives data.isos22 for BFF categories from the verified ISO 2016 -> 2022 map in hm-grunndata-register.
// Read-only: writes a review CSV and a reviewable SQL file. Nothing is applied to any database.
//
//   node scripts/isos22-from-isomap.mjs --isomap <file|url> --categories <file|url> [--out out] [--include-review]
//
// --isomap      JSON array from GET <register>/api/v22/isomap/
// --categories  JSON array from GET <bff>/admin/category
// --include-review  also write SQL for categories with split/merged/deleted codes (default: only clean mappings)
import { readFile, mkdir, writeFile } from 'node:fs/promises'
import { parseArgs } from 'node:util'

const { values: args } = parseArgs({
  options: {
    isomap: { type: 'string' },
    categories: { type: 'string' },
    out: { type: 'string', default: 'isos22-out' },
    'include-review': { type: 'boolean', default: false },
  },
})

if (!args.isomap || !args.categories) {
  console.error('Usage: isos22-from-isomap.mjs --isomap <file|url> --categories <file|url> [--out dir] [--include-review]')
  process.exit(2)
}

const load = async (source) =>
  JSON.parse(/^https?:\/\//.test(source) ? await (await fetch(source)).text() : await readFile(source, 'utf8'))

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

const sqlString = (value) => `'${String(value).replaceAll("'", "''")}'`
const csvCell = (value) => `"${String(value).replaceAll('"', '""')}"`

const isomap = await load(args.isomap)
const categories = await load(args.categories)
const rows = isomap.filter((row) => row.code16)

const classify = (category) => {
  const isos = category.data?.isos ?? []
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
  const invalid = isos22.filter((code) => !ISO_CODE.test(code))
  const unchanged = isos22.length === isos.length && isos22.every((code, i) => code === [...isos].sort()[i])

  let status
  if (unmapped.length === isos.length) status = 'not mapped'
  else if (unmapped.length > 0) status = 'partly unmapped'
  else if (unverified) status = 'unverified'
  else if (invalid.length > 0) status = 'invalid code'
  else if (needsReview) status = 'review'
  else status = unchanged ? 'same' : 'renumbered'

  return { isos, isos22, unmapped, status }
}

const results = categories
  .filter((category) => (category.data?.isos ?? []).length > 0)
  .map((category) => ({ category, ...classify(category) }))
  .sort((a, b) => a.category.title.localeCompare(b.category.title, 'nb'))

const writable = new Set(['same', 'renumbered', ...(args['include-review'] ? ['review'] : [])])
const sql = results
  .filter((r) => writable.has(r.status))
  .map(({ category, isos22 }) => {
    const current = category.data.isos22
    const guard = current == null ? 'IS NULL' : `= ${sqlString(JSON.stringify(current))}::jsonb`
    return (
      `UPDATE category SET data = jsonb_set(data, '{isos22}', ${sqlString(JSON.stringify(isos22))}::jsonb)\n` +
      `  WHERE id = ${sqlString(category.id)} AND data->'isos22' ${guard}; -- ${category.title.replaceAll(/[\r\n]/g, ' ')}`
    )
  })

const csv = [
  ['category', 'id', 'status', 'v16_isos', 'v22_isos', 'current_isos22', 'unmapped_v16_prefixes'].join(','),
  ...results.map((r) =>
    [
      r.category.title,
      r.category.id,
      r.status,
      r.isos.join(' '),
      r.isos22.join(' '),
      (r.category.data.isos22 ?? []).join(' '),
      r.unmapped.join(' '),
    ]
      .map(csvCell)
      .join(',')
  ),
].join('\n')

await mkdir(args.out, { recursive: true })
await writeFile(`${args.out}/isos22-review.csv`, csv + '\n')
await writeFile(`${args.out}/isos22-update.sql`, `BEGIN;\n${sql.join('\n')}\nCOMMIT;\n`)

const counts = Object.entries(Object.groupBy(results, (r) => r.status)).map(([s, rs]) => `${s}: ${rs.length}`)
console.log(`${results.length} categories with isos. ${counts.join(', ')}`)
console.log(`${sql.length} UPDATE statements written to ${args.out}/isos22-update.sql`)
