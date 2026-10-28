import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { errorMessage } from '../services/api'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // Message affiche apres une expiration de session (pose par l'interceptor Axios)
  const expired = sessionStorage.getItem('sessionExpired')
  if (expired) sessionStorage.removeItem('sessionExpired')
  const [showExpired] = useState(!!expired)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
      navigate(location.state?.from?.pathname || '/', { replace: true })
    } catch (err) {
      setError(errorMessage(err, 'Connexion impossible'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <div className="card">
        <h1>Connexion</h1>
        {showExpired && <div className="alert info">Votre session a expire, veuillez vous reconnecter.</div>}
        {error && <div className="alert error">{error}</div>}
        <form className="form" onSubmit={submit}>
          <label>Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label>Mot de passe
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </label>
          <button className="btn" disabled={loading}>{loading ? 'Connexion...' : 'Se connecter'}</button>
        </form>
        <p className="muted">Pas de compte ? <Link to="/register">Creer un compte</Link></p>
        <p className="muted">Comptes de test (mot de passe <b>password123</b>) : admin@demo.com, tech1@demo.com, employee1@demo.com</p>
      </div>
    </div>
  )
}
