const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8090';

function getToken() {
  return localStorage.getItem('docsig_token');
}

function setToken(token) {
  if (token) {
    localStorage.setItem('docsig_token', token);
  } else {
    localStorage.removeItem('docsig_token');
  }
}

async function apiFetch(path, options = {}) {
  const headers = new Headers(options.headers || {});
  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (!response.ok) {
    let payload = null;
    try {
      payload = await response.json();
    } catch {
      payload = { message: response.statusText };
    }
    throw new Error(payload.message || 'Request failed');
  }
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return response.json();
  }
  return response;
}

export const api = {
  baseUrl: API_BASE,
  getToken,
  setToken,
  register: (payload) => apiFetch('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  login: (payload) => apiFetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  me: () => apiFetch('/api/auth/me'),
  listDocuments: () => apiFetch('/api/docs'),
  getDocument: (id) => apiFetch(`/api/docs/${id}`),
  uploadDocument: (formData) => apiFetch('/api/docs/upload', {
    method: 'POST',
    body: formData
  }),
  getSignature: (documentId) => apiFetch(`/api/signatures/${documentId}`),
  createSignature: (payload) => apiFetch('/api/signatures', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  finalizeSignature: (payload) => apiFetch('/api/signatures/finalize', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  getAudit: (documentId) => apiFetch(`/api/audit/${documentId}`),
  getPublicSigning: (token) => apiFetch(`/api/public/signatures/${token}`),
  finalizePublicSigning: (token, payload) => apiFetch(`/api/public/signatures/${token}/finalize`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  downloadDocument: async (url) => {
    const response = await apiFetch(url);
    return response.blob();
  }
};
