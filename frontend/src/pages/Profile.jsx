import { useEffect, useState } from 'react'
import { changePassword, getProfile, updateProfile } from '../services/authService'
import { errorMessage } from '../services/api'
import { useAuth } from '../context/AuthContext'

export default function Profile() {
  const { refreshUser } = useAuth()
  const [profile, setProfile] = useState({ fullName: '', email: '', role: '' })
  const [pwd, setPwd] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' })
  const [msg, setMsg] = useState(null)
  const [pwdMsg, setPwdMsg] = useState(null)

  useEffect(() => {
    getProfile().then(setProfile).catch((e) => setMsg({ type: 'error', text: errorMessage(e) }))
  }, [])

  const saveProfile = async (e) => {
    e.preventDefault()
    setMsg(null)
    try {
      await updateProfile(profile.fullName, profile.email)
      refreshUser()
      setMsg({ type: 'success', text: 'Profil mis a jour' })
    } catch (err) {
      setMsg({ type: 'error', text: errorMessage(err) })
    }
  }

  const savePassword = async (e) => {
    e.preventDefault()
    setPwdMsg(null)
    if (pwd.newPassword !== pwd.confirmPassword) {
      setPwdMsg({ type: 'error', text: 'La confirmation ne correspond pas' })
      return
    }
    try {
      await changePassword(pwd.oldPassword, pwd.newPassword, pwd.confirmPassword)
      setPwd({ oldPassword: '', newPassword: '', confirmPassword: '' })
      setPwdMsg({ type: 'success', text: 'Mot de passe modifie' })
    } catch (err) {
      setPwdMsg({ type: 'error', text: errorMessage(err) })
    }
  }

  return (
    <div className="container">
      <h1>Mon profil</h1>
      <div className="card">
        <h2>Informations</h2>
        {msg && <div className={`alert ${msg.type}`}>{msg.text}</div>}
        <form className="form" onSubmit={saveProfile}>
          <label>Nom complet
            <input value={profile.fullName} onChange={(e) => setProfile({ ...profile, fullName: e.target.value })} required />
          </label>
          <label>Email
            <input type="email" value={profile.email} onChange={(e) => setProfile({ ...profile, email: e.target.value })} required />
          </label>
          <p className="muted">Role : <b>{profile.role}</b> (non modifiable)</p>
          <button className="btn">Enregistrer</button>
        </form>
      </div>
      <div className="card">
        <h2>Changer le mot de passe</h2>
        {pwdMsg && <div className={`alert ${pwdMsg.type}`}>{pwdMsg.text}</div>}
        <form className="form" onSubmit={savePassword}>
          <label>Ancien mot de passe
            <input type="password" value={pwd.oldPassword} onChange={(e) => setPwd({ ...pwd, oldPassword: e.target.value })} required />
          </label>
          <label>Nouveau mot de passe
            <input type="password" minLength={6} value={pwd.newPassword} onChange={(e) => setPwd({ ...pwd, newPassword: e.target.value })} required />
          </label>
          <label>Confirmation
            <input type="password" value={pwd.confirmPassword} onChange={(e) => setPwd({ ...pwd, confirmPassword: e.target.value })} required />
          </label>
          <button className="btn">Modifier</button>
        </form>
      </div>
    </div>
  )
}
