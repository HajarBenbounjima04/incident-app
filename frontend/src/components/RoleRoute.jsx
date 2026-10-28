import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/** N'autorise que les roles listes (ex: <RoleRoute roles={['ADMIN']} />). */
export default function RoleRoute({ roles }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (!roles.includes(user.role)) {
    return (
      <div className="container">
        <div className="alert error">Acces refuse : vous n'avez pas les droits pour cette page.</div>
      </div>
    )
  }
  return <Outlet />
}
