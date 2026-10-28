import { createContext, useContext, useState } from 'react'
import * as authService from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(authService.getCurrentUser())

  const login = async (email, password) => {
    await authService.login(email, password)
    setUser(authService.getCurrentUser())
  }

  const logout = async () => {
    await authService.logout()
    setUser(null)
  }

  const refreshUser = () => setUser(authService.getCurrentUser())

  return <AuthContext.Provider value={{ user, login, logout, refreshUser }}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
