import { Link } from 'react-router-dom'
import { formatDate } from '../services/incidentService'

export default function IncidentTable({ incidents, showCreator = false, showAssignee = false }) {
  if (!incidents) return <p>Chargement...</p>
  if (incidents.length === 0) return <p className="muted">Aucun incident.</p>
  return (
    <table>
      <thead>
        <tr>
          <th>#</th><th>Titre</th><th>Categorie</th><th>Priorite</th><th>Statut</th>
          {showCreator && <th>Declare par</th>}
          {showAssignee && <th>Technicien</th>}
          <th>Date</th>
        </tr>
      </thead>
      <tbody>
        {incidents.map((i) => (
          <tr key={i.id}>
            <td>{i.id}</td>
            <td><Link to={`/incidents/${i.id}`}>{i.title}</Link></td>
            <td>{i.category}</td>
            <td><span className={`badge ${i.priority}`}>{i.priority}</span></td>
            <td><span className={`badge ${i.status}`}>{i.status}</span></td>
            {showCreator && <td>{i.createdBy?.fullName}</td>}
            {showAssignee && <td>{i.assignedTo?.fullName || '-'}</td>}
            <td>{formatDate(i.createdAt)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
