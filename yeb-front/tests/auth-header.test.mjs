import assert from 'node:assert/strict'
import {readFile} from 'node:fs/promises'
const source = await readFile(new URL('../src/utils/auth.js', import.meta.url), 'utf8')
const {normalizeAuthorizationHeader, getAuthorizationHeader} = await import('data:text/javascript;base64,' + Buffer.from(source).toString('base64'))

const token = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuYXFpYW8ifQ.signature'
assert.equal(normalizeAuthorizationHeader('Bearer' + token), 'Bearer ' + token)
assert.equal(normalizeAuthorizationHeader('Bearer ' + token), 'Bearer ' + token)
assert.equal(normalizeAuthorizationHeader('  Bearer   ' + token + '  '), 'Bearer ' + token)
assert.equal(normalizeAuthorizationHeader('bearer ' + token), 'Bearer ' + token)
assert.equal(normalizeAuthorizationHeader(null), '')

// Refreshing an already logged-in legacy session fixes both its connection header and stored value.
const values = new Map([['tokenStr', 'Bearer' + token]])
globalThis.sessionStorage = {getItem: key => values.get(key) ?? null, setItem: (key, value) => values.set(key, value)}
assert.equal(getAuthorizationHeader(), 'Bearer ' + token)
assert.equal(values.get('tokenStr'), 'Bearer ' + token)
values.delete('tokenStr')
assert.equal(getAuthorizationHeader(), '')
console.log('Authorization header regression checks passed.')
