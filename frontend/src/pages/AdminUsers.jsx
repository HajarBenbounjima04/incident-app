import { useEffect, useState } from 'react'
import { disableUser, enableUser, formatDate, getUsers } from '../services/incidentService'
import { errorMessage } from '../services/api'

export default function AdminUsers() {
  const [users, setUsers] = useState(null)
  const [msg, setMsg] = useState(null)

  const load = () => getUsers().then(setUsers).catch((e) => setMsg({ type: 'error', text: errorMessage(e) }))
  useEffect(() => { load() }, [])

  const toggle = async (u) => {
    setMsg(null)
    try {
      await (u.enabled ? disableUser(u.id) : enableUser(u.id))
      load()
    } catch (err) {
      setMsg({ type: 'error', text: errorMessage(err) })
    }
  }

  return (
    <div className="container">
      <h1>Utilisateurs</h1>
      {msg && <div className={`alert ${msg.type}`}>{msg.text}</div>}
      {!users ? <p>Chargement...</p> : (
        <table>
          <thead><tr><th>#</th><th>Nom</th><th>Email</th><th>Role</th><th>Statut</th><th>Cree le</th><th>Action</th></tr></thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.fullName}</td>
                <td>{u.email}</td>
                <td>{u.role}</td>
                <td>{u.enabled ? 'Actif' : 'Desactive'}</td>
                <td>{formatDate(u.createdAt)}</td>
                <td>
                  <button className={`btn small ${u.enabled ? 'danger' : 'ok'}`} onClick={() => toggle(u)}>
                    {u.enabled ? 'Desactiver' : 'Activer'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
