import { useEffect, useState } from 'react'
import IncidentTable from '../components/IncidentTable'
import { getAssignedIncidents } from '../services/incidentService'
import { errorMessage } from '../services/api'

export default function AssignedIncidents() {
  const [incidents, setIncidents] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getAssignedIncidents().then(setIncidents).catch((e) => setError(errorMessage(e)))
  }, [])

  return (
    <div className="container">
      <h1>Incidents qui me sont affectes</h1>
      {error && <div className="alert error">{error}</div>}
      <IncidentTable incidents={incidents} showCreator />
    </div>
  )
}
