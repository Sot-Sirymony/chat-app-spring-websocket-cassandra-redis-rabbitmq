/**
 * Global callback for 401 Unauthorized from API.
 * Set by AuthRedirectHandler so apiFetch/uploadFile can trigger logout + redirect.
 */
let on401: (() => void) | null = null;

export function setOn401(fn: (() => void) | null): void {
  on401 = fn;
}

export function getOn401(): (() => void) | null {
  return on401;
}
