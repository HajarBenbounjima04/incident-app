API=${API:-http://localhost:8080}
login() { curl -s -X POST $API/auth/login -H 'Content-Type: application/json' -d "{\"email\":\"$1\",\"password\":\"$2\"}" | python3 -c 'import sys,json;print(json.load(sys.stdin).get("token",""))'; }
# req METHOD URL TOKEN [JSON]  -> affiche "HTTP <code> <body>"
req() { local m=$1 u=$2 t=$3 d=$4; local args=(-s -w '\nHTTP %{http_code}\n' -X "$m" "$API$u" -H 'Content-Type: application/json'); [ -n "$t" ] && args+=(-H "Authorization: Bearer $t"); [ -n "$d" ] && args+=(-d "$d"); curl "${args[@]}"; }
id_of() { python3 -c 'import sys,json;print(json.load(sys.stdin)["id"])'; }
