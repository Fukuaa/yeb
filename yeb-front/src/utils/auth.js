// Older login pages stored Bearer directly against the JWT without a space.
// Normalize those sessions before HTTP requests or STOMP authentication.
export function normalizeAuthorizationHeader(value) {
  if (!value) return ''
  return value.trim().replace(/^Bearer\s*/i, 'Bearer ')
}

export function getAuthorizationHeader() {
  const stored = sessionStorage.getItem('tokenStr')
  const normalized = normalizeAuthorizationHeader(stored)
  if (stored && normalized !== stored) sessionStorage.setItem('tokenStr', normalized)
  return normalized
}
