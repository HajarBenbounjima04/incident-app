#!/usr/bin/env bash
# Scénario §24 — nécessite le backend démarré (comptes créés par DataInitializer)
source "$(dirname "$0")/lib.sh"
E1=$(login emp1@test.com 'Employee1!'); E2=$(login emp2@test.com 'Employee1!')
AD=$(login admin@test.com 'Admin123!');  TE=$(login tech@test.com 'Tech1234!')
echo "== employé 1 : 2 incidents"
I1=$(curl -s -X POST $API/api/incidents -H "Authorization: Bearer $E1" -H 'Content-Type: application/json' -d '{"title":"Malware poste 12","description":"Antivirus alerte","category":"MALWARE","priority":"HIGH"}' | id_of)
curl -s -o /dev/null -X POST $API/api/incidents -H "Authorization: Bearer $E1" -H 'Content-Type: application/json' -d '{"title":"Phishing","description":"Mail suspect","category":"PHISHING","priority":"MEDIUM"}'
echo "== employé 2 : 1 incident"
I3=$(curl -s -X POST $API/api/incidents -H "Authorization: Bearer $E2" -H 'Content-Type: application/json' -d '{"title":"Panne réseau","description":"Plus de wifi","category":"NETWORK","priority":"LOW"}' | id_of)
echo "== employé 1 lit l'incident de l'employé 2 (attendu 403)"; req GET /api/incidents/$I3 $E1
echo "== admin : liste + affectation (technicien id 2)"; req GET /api/incidents $AD | tail -1
req PUT /api/incidents/$I1/assign/2 $AD | tail -1
echo "== technicien : ses incidents, statuts, note"; req GET /api/incidents/assigned $TE | tail -1
req PUT /api/incidents/$I1/status $TE '{"status":"IN_PROGRESS"}' | tail -1
req PUT /api/incidents/$I1/status $TE '{"status":"RESOLVED"}' | tail -1
req POST /api/incidents/$I1/comments $TE '{"content":"Malware supprimé"}' | tail -1
echo "== employé 1 : détail + historique"; req GET /api/incidents/$I1 $E1
