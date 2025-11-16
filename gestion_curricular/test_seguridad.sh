#!/bin/bash
# Script de Pruebas de Seguridad - Linux/Mac
# Uso: chmod +x test_seguridad.sh && ./test_seguridad.sh

BASE_URL="http://localhost:5000"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

PASSED=0
TOTAL=0

test_result() {
    local test_name=$1
    local passed=$2
    local message=$3
    
    TOTAL=$((TOTAL + 1))
    
    if [ "$passed" = true ]; then
        echo -e "${GREEN}✅ $test_name${NC}"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}❌ $test_name${NC}"
        if [ ! -z "$message" ]; then
            echo -e "   ${YELLOW}$message${NC}"
        fi
    fi
}

echo -e "${CYAN}🧪 Iniciando Pruebas de Seguridad${NC}"
echo -e "${GRAY}Servidor: $BASE_URL${NC}"
echo ""

# Test 1: Verificar que el servidor está corriendo
echo -e "${YELLOW}1️⃣ Verificando conexión con el servidor...${NC}"
if curl -s -f -o /dev/null -w "%{http_code}" --max-time 5 "$BASE_URL/api/usuarios/login" > /dev/null 2>&1; then
    test_result "Servidor accesible" true
else
    test_result "Servidor accesible" false "El servidor no está corriendo o no es accesible en $BASE_URL"
    echo ""
    echo -e "${YELLOW}⚠️  Por favor inicia el servidor con: mvn spring-boot:run${NC}"
    exit 1
fi
echo ""

# Test 2: Verificar headers de seguridad
echo -e "${YELLOW}2️⃣ Verificando headers de seguridad HTTP...${NC}"
HEADERS_PRESENT=0
HEADERS_TOTAL=0

check_header() {
    local header_name=$1
    HEADERS_TOTAL=$((HEADERS_TOTAL + 1))
    if curl -s -I "$BASE_URL/api/usuarios/login" | grep -qi "^$header_name:"; then
        echo -e "   ${GREEN}✅ $header_name presente${NC}"
        HEADERS_PRESENT=$((HEADERS_PRESENT + 1))
        return 0
    else
        echo -e "   ${RED}❌ $header_name falta${NC}"
        return 1
    fi
}

check_header "X-Frame-Options"
check_header "X-Content-Type-Options"
check_header "Strict-Transport-Security"
check_header "Content-Security-Policy"

if [ $HEADERS_PRESENT -eq $HEADERS_TOTAL ]; then
    test_result "Headers de seguridad" true "$HEADERS_PRESENT/$HEADERS_TOTAL headers presentes"
else
    test_result "Headers de seguridad" false "Solo $HEADERS_PRESENT/$HEADERS_TOTAL headers presentes"
fi
echo ""

# Test 3: Probar acceso sin token
echo -e "${YELLOW}3️⃣ Probando protección de endpoints (sin token)...${NC}"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/usuarios/listarUsuarios")
if [ "$STATUS" == "401" ]; then
    test_result "Protección de endpoints" true "401 Unauthorized (correcto)"
else
    test_result "Protección de endpoints" false "El endpoint debería devolver 401 pero devolvió: $STATUS"
fi
echo ""

# Test 4: Probar login
echo -e "${YELLOW}4️⃣ Probando autenticación (login)...${NC}"
LOGIN_EMAIL="test@test.com"
LOGIN_PASSWORD="test"

echo -e "   ${GRAY}Intentando login con: $LOGIN_EMAIL${NC}"
echo -e "   ${YELLOW}⚠️  Nota: Asegúrate de tener un usuario válido en la base de datos${NC}"

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/usuarios/login" \
  -H "Content-Type: application/json" \
  -d "{\"correo\":\"$LOGIN_EMAIL\",\"password\":\"$LOGIN_PASSWORD\"}")

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    if [ ! -z "$TOKEN" ]; then
        TOKEN_PREVIEW="${TOKEN:0:30}..."
        test_result "Login exitoso" true "Token obtenido: $TOKEN_PREVIEW"
    else
        test_result "Login exitoso" false "No se pudo extraer el token de la respuesta"
        TOKEN=""
    fi
else
    if echo "$LOGIN_RESPONSE" | grep -qi "credenciales\|unauthorized\|401"; then
        test_result "Login exitoso" false "Credenciales incorrectas o usuario no existe. Verifica tus credenciales en la base de datos."
    else
        test_result "Login exitoso" false "Error en la respuesta: $LOGIN_RESPONSE"
    fi
    TOKEN=""
fi
echo ""

# Test 5: Probar acceso con token válido
if [ ! -z "$TOKEN" ]; then
    echo -e "${YELLOW}5️⃣ Probando acceso con token válido...${NC}"
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
      -H "Authorization: Bearer $TOKEN" \
      "$BASE_URL/api/usuarios/listarUsuarios")
    
    if [ "$STATUS" == "200" ]; then
        test_result "Acceso con token válido" true "200 OK"
    elif [ "$STATUS" == "403" ]; then
        test_result "Acceso con token válido" true "403 Forbidden (token válido pero sin permisos - correcto)"
    elif [ "$STATUS" == "401" ]; then
        test_result "Acceso con token válido" false "401 Unauthorized (token no válido o expirado)"
    else
        test_result "Acceso con token válido" false "Código inesperado: $STATUS"
    fi
else
    echo -e "${GRAY}5️⃣ ⏭️  Omitido (no se obtuvo token)${NC}"
fi
echo ""

# Test 6: Probar token inválido
echo -e "${YELLOW}6️⃣ Probando token inválido...${NC}"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer token_invalido_aqui" \
  "$BASE_URL/api/usuarios/listarUsuarios")

if [ "$STATUS" == "401" ]; then
    test_result "Rechazo de token inválido" true "401 Unauthorized (correcto)"
else
    test_result "Rechazo de token inválido" false "El endpoint debería rechazar tokens inválidos (código: $STATUS)"
fi
echo ""

# Resumen
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}📊 Resumen de Pruebas${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

echo -e "Resultado: ${GREEN}$PASSED${NC}/${CYAN}$TOTAL${NC} pruebas pasadas"

if [ $PASSED -eq $TOTAL ]; then
    echo ""
    echo -e "${GREEN}🎉 ¡Todas las pruebas pasaron!${NC}"
else
    echo ""
    echo -e "${YELLOW}⚠️  Algunas pruebas fallaron. Revisa los detalles arriba.${NC}"
fi

echo ""
echo -e "${GRAY}📝 Nota: Para ver los logs de auditoría, revisa la consola del servidor o el archivo de logs.${NC}"

