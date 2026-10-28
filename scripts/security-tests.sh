#!/usr/bin/env bash
# Tests de sécurité §25 — attendus entre parenthèses
source "$(dirname "$0")/lib.sh"
E1=$(login emp1@test.com 'Employee1!'); E2=$(login emp2@test.com 'Employee1!')
AD=$(login admin@test.com 'Admin123!');  TE=$(login tech@test.com 'Tech1234!')
J='{"title":"t","description":"d","category":"OTHER","priority":"LOW"}'
I2=$(curl -s -X POST $API/api/incidents -H "Authorization: Bearer $E2" -H 'Content-Type: application/json' -d "$J" | id_of)
echo "1. sans JWT (401)";               req GET /api/profile "" | tail -2
echo "2. JWT invalide (401)";           req GET /api/profile "abc.def.ghi" | tail -2
echo "3. JWT expiré (401) -> collez un ancien token dans EXPIRED=..."; [ -n "$EXPIRED" ] && req GET /api/profile "$EXPIRED" | tail -2
echo "4. EMPLOYEE sur route ADMIN (403)"; req GET /api/admin/users $E1 | tail -2
echo "5. EMPLOYEE sur incident d'un autre (403)"; req GET /api/incidents/$I2 $E1 | tail -2
echo "6. TECHNICIAN modifie un incident non affecté (403)"; req PUT /api/incidents/$I2/status $TE '{"status":"IN_PROGRESS"}' | tail -2
echo "7. utilisateur désactivé (403)"
UID_=$(curl -s -X POST $API/auth/register -H 'Content-Type: application/json' -d '{"fullName":"Temp","email":"temp@test.com","password":"Temp12345"}' | id_of)
req PUT /api/admin/users/$UID_/disable $AD >/dev/null
req POST /auth/login "" '{"email":"temp@test.com","password":"Temp12345"}' | tail -2
echo "8. email déjà existant (409)";    req POST /auth/register "" '{"fullName":"X","email":"emp1@test.com","password":"Employee1!"}' | tail -2
echo "9. incident invalide (400)";      req POST /api/incidents $E1 '{"title":"","description":""}' | tail -3
echo "+ inscription avec role ADMIN : le rôle est ignoré (201, role EMPLOYEE)"
req POST /auth/register "" '{"fullName":"Hack","email":"hack'$RANDOM'@test.com","password":"Hack12345","role":"ADMIN"}' | tail -2
