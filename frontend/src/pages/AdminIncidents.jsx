import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { assignIncident, formatDate, getAllIncidents, getUsers } from '../services/incidentService'
import { errorMessage } from '../services/api'

export default function AdminIncidents() {
  const [incidents, setIncidents] = useState(null)
  const [technicians, setTechnicians] = useState([])
  const [selected, setSelected] = useState({})
  const [msg, setMsg] = useState(null)

  const load = () =>
    getAllIncidents().then(setIncidents).catch((e) => setMsg({ type: 'error', text: errorMessage(e) }))

  useEffect(() => {
    load()
    getUsers()
      .then((users) => setTechnicians(users.filter((u) => u.role === 'TECHNICIAN' && u.enabled)))
      .catch((e) => setMsg({ type: 'error', text: errorMessage(e) }))
  }, [])

  const assign = async (incidentId) => {
    const techId = selected[incidentId]
    if (!techId) return
    try {
      await assignIncident(incidentId, techId)
      setMsg({ type: 'success', text: `Incident #${incidentId} affecte` })
      load()
    } catch (err) {
      setMsg({ type: 'error', text: errorMessage(err) })
    }
  }

  return (
    <div className="container">
      <h1>Tous les incidents</h1>
      {msg && <div className={`alert ${msg.type}`}>{msg.text}</div>}
      {!incidents ? <p>Chargement...</p> : (
        <table>
          <thead>
            <tr><th>#</th><th>Titre</th><th>Priorite</th><th>Statut</th><th>Declare par</th><th>Technicien</th><th>Date</th><th>Affecter</th></tr>
          </thead>
          <tbody>
            {incidents.map((i) => (
              <tr key={i.id}>
                <td>{i.id}</td>
                <td><Link to={`/incidents/${i.id}`}>{i.title}</Link></td>
                <td><span className={`badge ${i.priority}`}>{i.priority}</span></td>
                <td><span className={`badge ${i.status}`}>{i.status}</span></td>
                <td>{i.createdBy?.fullName}</td>
                <td>{i.assignedTo?.fullName || '-'}</td>
                <td>{formatDate(i.createdAt)}</td>
                <td>
                  {i.status !== 'CLOSED' && (
                    <div className="row">
                      <select value={selected[i.id] || ''} onChange={(e) => setSelected({ ...selected, [i.id]: e.target.value })}>
                        <option value="">-- choisir --</option>
                        {technicians.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
                      </select>
                      <button className="btn small" disabled={!selected[i.id]} onClick={() => assign(i.id)}>OK</button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
