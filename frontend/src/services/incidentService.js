import api from './api'

export const CATEGORIES = ['NETWORK', 'MALWARE', 'PHISHING', 'ACCESS', 'SOFTWARE', 'HARDWARE', 'DATA_LEAK', 'OTHER']
export const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
export const STATUSES = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']

export const createIncident = (payload) => api.post('/api/incidents', payload).then((r) => r.data)
export const getMyIncidents = () => api.get('/api/incidents/my').then((r) => r.data)
export const getAssignedIncidents = () => api.get('/api/incidents/assigned').then((r) => r.data)
export const getAllIncidents = () => api.get('/api/incidents').then((r) => r.data)
export const getIncident = (id) => api.get(`/api/incidents/${id}`).then((r) => r.data)
export const updateStatus = (id, status) => api.put(`/api/incidents/${id}/status`, { status }).then((r) => r.data)
export const assignIncident = (id, technicianId) =>
  api.put(`/api/incidents/${id}/assign/${technicianId}`).then((r) => r.data)
export const addComment = (id, content) => api.post(`/api/incidents/${id}/comments`, { content }).then((r) => r.data)

export const getDashboard = () => api.get('/api/dashboard').then((r) => r.data)
export const getAdminStatistics = () => api.get('/api/admin/statistics').then((r) => r.data)

export const getUsers = () => api.get('/api/admin/users').then((r) => r.data)
export const enableUser = (id) => api.put(`/api/admin/users/${id}/enable`).then((r) => r.data)
export const disableUser = (id) => api.put(`/api/admin/users/${id}/disable`).then((r) => r.data)

export const formatDate = (d) => (d ? new Date(d).toLocaleString('fr-FR') : '-')
