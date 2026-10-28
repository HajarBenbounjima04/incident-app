import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <Link to="/" className="brand">Incidents IT</Link>
      {user && (
        <div className="links">
          <Link to="/">Dashboard</Link>
          {user.role === 'EMPLOYEE' && (
            <>
              <Link to="/my-incidents">Mes incidents</Link>
              <Link to="/new-incident">Nouvel incident</Link>
            </>
          )}
          {user.role === 'TECHNICIAN' && <Link to="/assigned">Incidents affectes</Link>}
          {user.role === 'ADMIN' && (
            <>
              <Link to="/admin/incidents">Incidents</Link>
              <Link to="/admin/users">Utilisateurs</Link>
            </>
          )}
          <Link to="/profile">Profil</Link>
          <span className="who">{user.fullName} ({user.role})</span>
          <button className="btn small" onClick={handleLogout}>Deconnexion</button>
        </div>
      )}
    </nav>
  )
}
