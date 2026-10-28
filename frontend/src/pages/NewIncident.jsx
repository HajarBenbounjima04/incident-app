import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { CATEGORIES, PRIORITIES, createIncident } from '../services/incidentService'
import { errorMessage } from '../services/api'

export default function NewIncident() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ title: '', description: '', category: 'OTHER', priority: 'MEDIUM' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await createIncident(form)
      navigate('/my-incidents')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <div className="card">
        <h1>Declarer un incident</h1>
        {error && <div className="alert error">{error}</div>}
        <form className="form" onSubmit={submit}>
          <label>Titre
            <input name="title" value={form.title} onChange={change} maxLength={200} required />
          </label>
          <label>Description
            <textarea name="description" value={form.description} onChange={change} required />
          </label>
          <label>Categorie
            <select name="category" value={form.category} onChange={change}>
              {CATEGORIES.map((c) => <option key={c}>{c}</option>)}
            </select>
          </label>
          <label>Priorite
            <select name="priority" value={form.priority} onChange={change}>
              {PRIORITIES.map((p) => <option key={p}>{p}</option>)}
            </select>
          </label>
          <button className="btn" disabled={loading}>{loading ? 'Envoi...' : 'Creer'}</button>
        </form>
      </div>
    </div>
  )
}
