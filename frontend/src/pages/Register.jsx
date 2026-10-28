import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../services/authService'
import { errorMessage } from '../services/api'

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(form.fullName, form.email, form.password)
      navigate('/login')
    } catch (err) {
      setError(errorMessage(err, "Inscription impossible"))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <div className="card">
        <h1>Inscription</h1>
        <p className="muted">Les nouveaux comptes ont le role EMPLOYEE.</p>
        {error && <div className="alert error">{error}</div>}
        <form className="form" onSubmit={submit}>
          <label>Nom complet
            <input name="fullName" value={form.fullName} onChange={change} required />
          </label>
          <label>Email
            <input name="email" type="email" value={form.email} onChange={change} required />
          </label>
          <label>Mot de passe (6 caracteres min.)
            <input name="password" type="password" minLength={6} value={form.password} onChange={change} required />
          </label>
          <button className="btn" disabled={loading}>{loading ? 'Creation...' : "S'inscrire"}</button>
        </form>
        <p className="muted">Deja inscrit ? <Link to="/login">Se connecter</Link></p>
      </div>
    </div>
  )
}
