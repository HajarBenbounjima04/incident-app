import { useEffect, useState } from 'react'
import IncidentTable from '../components/IncidentTable'
import { getMyIncidents } from '../services/incidentService'
import { errorMessage } from '../services/api'

export default function MyIncidents() {
  const [incidents, setIncidents] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getMyIncidents().then(setIncidents).catch((e) => setError(errorMessage(e)))
  }, [])

  return (
    <div className="container">
      <h1>Mes incidents</h1>
      {error && <div className="alert error">{error}</div>}
      <IncidentTable incidents={incidents} showAssignee />
    </div>
  )
}
