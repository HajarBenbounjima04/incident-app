import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { addComment, formatDate, getIncident, updateStatus } from '../services/incidentService'
import { errorMessage } from '../services/api'

// Transitions proposees dans l'UI (le backend reste l'autorite)
const NEXT = {
  OPEN: ['IN_PROGRESS'],
  ASSIGNED: ['IN_PROGRESS'],
  IN_PROGRESS: ['RESOLVED'],
  RESOLVED: ['CLOSED', 'IN_PROGRESS'],
  CLOSED: [],
}

export default function IncidentDetails() {
  const { id } = useParams()
  const { user } = useAuth()
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [note, setNote] = useState('')

  const load = () =>
    getIncident(id).then(setData).catch((e) => setError(errorMessage(e, 'Incident inaccessible')))

  useEffect(() => { load() }, [id])

  const canModify = user.role === 'ADMIN' || user.role === 'TECHNICIAN'

  const changeStatus = async (status) => {
    setError('')
    try {
      await updateStatus(id, status)
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  const sendNote = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await addComment(id, note)
      setNote('')
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  if (error && !data) return <div className="container"><div className="alert error">{error}</div></div>
  if (!data) return <div className="container"><p>Chargement...</p></div>

  const i = data.incident
  return (
    <div className="container">
      {error && <div className="alert error">{error}</div>}
      <div className="card">
        <h1>#{i.id} - {i.title}</h1>
        <div className="row">
          <span className={`badge ${i.priority}`}>{i.priority}</span>
          <span className={`badge ${i.status}`}>{i.status}</span>
          <span className="badge">{i.category}</span>
        </div>
        <p>{i.description}</p>
        <p className="muted">
          Declare par {i.createdBy?.fullName} le {formatDate(i.createdAt)} - Technicien : {i.assignedTo?.fullName || 'non affecte'}
        </p>
        {canModify && NEXT[i.status].length > 0 && (
          <div className="row">
            {NEXT[i.status].map((s) => (
              <button key={s} className="btn small" onClick={() => changeStatus(s)}>Passer a {s}</button>
            ))}
          </div>
        )}
      </div>

      <div className="card">
        <h2>Historique</h2>
        <ul className="timeline">
          {data.history.map((h) => (
            <li key={h.id}>
              <b>{h.message}</b> <span className="muted">- {h.performedBy}, {formatDate(h.createdAt)}</span>
            </li>
          ))}
        </ul>
      </div>

      <div className="card">
        <h2>Notes de traitement</h2>
        {data.comments.length === 0 && <p className="muted">Aucune note.</p>}
        <ul className="timeline">
          {data.comments.map((c) => (
            <li key={c.id}>
              {c.content} <span className="muted">- {c.author}, {formatDate(c.createdAt)}</span>
            </li>
          ))}
        </ul>
        {canModify && i.status !== 'CLOSED' && (
          <form className="form" onSubmit={sendNote}>
            <textarea value={note} onChange={(e) => setNote(e.target.value)} placeholder="Ajouter une note..." required />
            <button className="btn">Ajouter la note</button>
          </form>
        )}
      </div>
    </div>
  )
}
