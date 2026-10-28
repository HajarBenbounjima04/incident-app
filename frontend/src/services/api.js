import axios from 'axios'

export const API_URL = 'http://localhost:8080'

const api = axios.create({ baseURL: API_URL })

// --- Requete : ajoute automatiquement "Authorization: Bearer <JWT>" ---
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function forceLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('role')
  localStorage.removeItem('fullName')
  sessionStorage.setItem('sessionExpired', '1')
  window.location.href = '/login'
}

// --- Reponse : 401 -> tentative de refresh, sinon suppression du JWT + retour a /login ---
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    const status = error.response?.status
    const isAuthCall = original?.url?.startsWith('/auth/')

    if (status === 401 && !isAuthCall) {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken && !original._retry) {
        original._retry = true
        try {
          // axios "nu" pour ne pas repasser dans les interceptors
          const { data } = await axios.post(`${API_URL}/auth/refresh`, { refreshToken })
          localStorage.setItem('token', data.token)
          original.headers.Authorization = `Bearer ${data.token}`
          return api(original)
        } catch (e) {
          // refresh token invalide ou expire -> session terminee
        }
      }
      forceLogout()
    }
    return Promise.reject(error)
  }
)

export function errorMessage(error, fallback = 'Une erreur est survenue') {
  return error?.response?.data?.message || fallback
}

export default api
