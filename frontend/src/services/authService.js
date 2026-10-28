import api from './api'

function store(data) {
  localStorage.setItem('token', data.token)
  if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken)
  localStorage.setItem('role', data.role)
  localStorage.setItem('fullName', data.fullName)
}

export async function login(email, password) {
  const { data } = await api.post('/auth/login', { email, password })
  store(data)
  return data
}

export async function register(fullName, email, password) {
  const { data } = await api.post('/auth/register', { fullName, email, password })
  return data
}

export async function logout() {
  const refreshToken = localStorage.getItem('refreshToken')
  try {
    if (refreshToken) await api.post('/auth/logout', { refreshToken })
  } catch (e) {
    // on se deconnecte localement quoi qu'il arrive
  }
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('role')
  localStorage.removeItem('fullName')
}

export function getCurrentUser() {
  const token = localStorage.getItem('token')
  if (!token) return null
  return { token, role: localStorage.getItem('role'), fullName: localStorage.getItem('fullName') }
}

// Profil
export async function getProfile() {
  const { data } = await api.get('/api/profile')
  return data
}

export async function updateProfile(fullName, email) {
  const { data } = await api.put('/api/profile', { fullName, email })
  // nouveau token (l'email est dans le JWT)
  localStorage.setItem('token', data.token)
  localStorage.setItem('fullName', data.fullName)
  return data
}

export async function changePassword(oldPassword, newPassword, confirmPassword) {
  await api.put('/api/profile/password', { oldPassword, newPassword, confirmPassword })
}
