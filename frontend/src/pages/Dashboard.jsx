import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getAdminStatistics, getDashboard, formatDate } from '../services/incidentService'
import { errorMessage } from '../services/api'

function Stat({ label, value }) {
  return (
    <div className="stat">
      <div className="n">{value}</div>
      <div className="l">{label}</div>
    </div>
  )
}

function AdminDashboard() {
  const [s, setS] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getAdminStatistics().then(setS).catch((e) => setError(errorMessage(e)))
  }, [])

  if (error) return <div className="alert error">{error}</div>
  if (!s) return <p>Chargement...</p>
  return (
    <>
      <h2>Utilisateurs</h2>
      <div className="stats">
        <Stat label="Total" value={s.totalUsers} />
        <Stat label="Employes" value={s.employees} />
        <Stat label="Techniciens" value={s.technicians} />
        <Stat label="Admins" value={s.admins} />
      </div>
      <h2>Incidents</h2>
      <div className="stats">
        <Stat label="Total" value={s.totalIncidents} />
        <Stat label="Ouverts" value={s.open} />
        <Stat label="Affectes" value={s.assigned} />
        <Stat label="En cours" value={s.inProgress} />
        <Stat label="Resolus" value={s.resolved} />
        <Stat label="Fermes" value={s.closed} />
      </div>
    </>
  )
}

function MyDashboard({ technician }) {
  const [s, setS] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getDashboard().then(setS).catch((e) => setError(errorMessage(e)))
  }, [])

  if (error) return <div className="alert error">{error}</div>
  if (!s) return <p>Chargement...</p>
  return (
    <>
      <div className="stats">
        <Stat label={technician ? 'Incidents affectes' : 'Mes incidents'} value={s.total} />
        <Stat label="Ouverts / affectes" value={s.open} />
        <Stat label="En cours" value={s.inProgress} />
        <Stat label="Resolus / fermes" value={s.resolvedOrClosed} />
        <Stat label="Critiques" value={s.critical} />
      </div>
      <h2>Derniers incidents</h2>
      {s.latest.length === 0 ? (
        <p className="muted">Aucun incident.</p>
      ) : (
        <table>
          <thead><tr><th>Titre</th><th>Priorite</th><th>Statut</th><th>Date</th></tr></thead>
          <tbody>
            {s.latest.map((i) => (
              <tr key={i.id}>
                <td><Link to={`/incidents/${i.id}`}>{i.title}</Link></td>
                <td><span className={`badge ${i.priority}`}>{i.priority}</span></td>
                <td><span className={`badge ${i.status}`}>{i.status}</span></td>
                <td>{formatDate(i.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  )
}

export default function Dashboard() {
  const { user } = useAuth()
  return (
    <div className="container">
      <h1>Bonjour {user.fullName}</h1>
      {user.role === 'ADMIN' && <AdminDashboard />}
      {user.role === 'TECHNICIAN' && <MyDashboard technician />}
      {user.role === 'EMPLOYEE' && <MyDashboard />}
    </div>
  )
}
