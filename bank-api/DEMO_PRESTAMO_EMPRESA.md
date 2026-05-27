# Demo: Flujo de Préstamo para Empresa

Este documento explica paso a paso cómo ejecutar el flujo completo de solicitud de préstamo empresarial usando la API del banco.

## Herramienta: Swagger UI

Abre `http://localhost:8080/swagger-ui.html` en tu navegador.

En la parte superior de Swagger verás la descripción completa de la API con los usuarios demo, los datos pre-cargados y el resumen del flujo. Cada sección de endpoints tiene su propia descripción.

---

## Cómo autenticarse en Swagger

1. Haz clic en el botón **Authorize** (candado) en la esquina superior derecha
2. Ingresa el usuario y contraseña del rol que necesitas para ese paso
3. Haz clic en **Authorize** y luego **Close**

| Paso | Usuario | Contraseña |
|------|---------|------------|
| Solicitar préstamo | `client_company` | `123456` |
| Rechazar préstamo | `analyst` | `123456` |
| Aprobar préstamo | `analyst` | `123456` |
| Desembolsar préstamo | `analyst` | `123456` |

> Para cambiar de usuario en Swagger: clic en **Authorize** → **Logout** → ingresa las nuevas credenciales.

---

## Datos pre-cargados al iniciar la app

| Dato | Valor |
|------|-------|
| ID representante legal | `representante_demo_id` |
| Cédula representante | `12345678` |
| Nombre representante | Carlos Gomez |
| ID del cliente empresa | `empresa_demo_id` |
| NIT empresa | `900123456` |
| Nombre empresa | Tech Solutions Corp |
| Número de cuenta destino | `10000001` |

> El representante legal debe existir antes que la empresa. `CreateClientUseCase` valida que el `legalRepresentativeId` apunte a un `NATURAL_PERSON_CLIENT` real.

---

## Paso 0 — Iniciar la aplicación

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Mac / Linux
./mvnw spring-boot:run
```

---

## Primera interacción: la empresa solicita → el analista rechaza

### Paso 1 — La empresa solicita el préstamo

**Swagger:** sección **Loans** → `POST /loans` → botón **Try it out**

**Autenticarse como:** `client_company` / `123456`

**Body:**
```json
{
  "typeLoan": "BUSINESS",
  "applicantClientId": "empresa_demo_id",
  "requestedAmount": 50000000,
  "interestRate": 0.12,
  "termMonths": 36
}
```

**Respuesta esperada — 201 Created:**
```json
{
  "id": "<loan-id-1>",
  "typeLoan": "BUSINESS",
  "applicantClientId": "empresa_demo_id",
  "requestedAmount": 50000000,
  "approvedAmount": null,
  "interestRate": 0.12,
  "termMonths": 36,
  "status": "UNDER_REVIEW",
  "approvalDate": null,
  "disbursementDate": null,
  "disbursementDestinationAccount": null
}
```

> Copia el valor del campo `"id"` — lo necesitas en el siguiente paso.

---

### Paso 2 — El analista rechaza el préstamo

**Swagger:** sección **Loans** → `POST /loans/{id}/reject` → botón **Try it out**

**Autenticarse como:** `analyst` / `123456`

**Campo `id`:** pega el `<loan-id-1>` del paso anterior

*(Sin body — este endpoint no requiere cuerpo)*

**Respuesta esperada — 200 OK:**
```json
{
  "id": "<loan-id-1>",
  "status": "REJECTED",
  ...
}
```

> Un préstamo rechazado **no puede reactivarse**. La empresa debe hacer una nueva solicitud.

---

## Segunda interacción: la empresa vuelve a solicitar → el analista aprueba → se desembolsa

### Paso 3 — La empresa solicita un nuevo préstamo

**Swagger:** sección **Loans** → `POST /loans` → botón **Try it out**

**Autenticarse como:** `client_company` / `123456`

**Body:**
```json
{
  "typeLoan": "BUSINESS",
  "applicantClientId": "empresa_demo_id",
  "requestedAmount": 30000000,
  "interestRate": 0.10,
  "termMonths": 24
}
```

**Respuesta esperada — 201 Created:**
```json
{
  "id": "<loan-id-2>",
  "status": "UNDER_REVIEW",
  ...
}
```

> Copia el nuevo `"id"` — lo necesitas en los pasos 4 y 5.

---

### Paso 4 — El analista aprueba el préstamo

**Swagger:** sección **Loans** → `POST /loans/{id}/approve` → botón **Try it out**

**Autenticarse como:** `analyst` / `123456`

**Campo `id`:** pega el `<loan-id-2>`

**Body:**
```json
{
  "approvedAmount": 30000000
}
```

> El analista puede aprobar un monto diferente al solicitado.

**Respuesta esperada — 200 OK:**
```json
{
  "id": "<loan-id-2>",
  "status": "APPROVED",
  "approvedAmount": 30000000,
  "approvalDate": "2026-05-27T...",
  ...
}
```

---

### Paso 5 — El analista desembolsa el préstamo

**Swagger:** sección **Loans** → `POST /loans/{id}/disburse` → botón **Try it out**

**Autenticarse como:** `analyst` / `123456`

**Campo `id`:** pega el `<loan-id-2>`

**Body:**
```json
{
  "destinationAccountNumber": "10000001"
}
```

> El sistema valida que la cuenta `10000001` pertenezca a `empresa_demo_id`.

**Respuesta esperada — 200 OK:**
```json
{
  "id": "<loan-id-2>",
  "status": "DISBURSED",
  "approvedAmount": 30000000,
  "disbursementDate": "2026-05-27T...",
  "disbursementDestinationAccount": "10000001"
}
```

El saldo de la cuenta `10000001` ahora tiene **$30,000,000 COP**.

---

## Verificar el desembolso — 3 formas

### Opción A — Ver el préstamo con estado DISBURSED

**Swagger:** sección **Loans** → `GET /loans/{id}` → **Try it out**

**Campo `id`:** pega el `<loan-id-2>`

Cualquier usuario autenticado sirve.

**Respuesta esperada:**
```json
{
  "id": "<loan-id-2>",
  "status": "DISBURSED",
  "approvedAmount": 30000000,
  "disbursementDate": "2026-05-27T...",
  "disbursementDestinationAccount": "10000001"
}
```

---

### Opción B — Ver que el dinero llegó a la cuenta

**Swagger:** sección **Accounts** → `GET /accounts/{id}/balance` → **Try it out**

**Autenticarse como:** `analyst` / `123456`

**Campo `id`:** `empresa_account_001`

**Respuesta esperada:**
```json
{
  "accountId": "empresa_account_001",
  "balance": 30000000,
  "status": "ACTIVE"
}
```

> Aquí se demuestra que el desembolso no es solo un cambio de estado en el préstamo — el dinero realmente se acreditó en la cuenta bancaria de la empresa.

---

### Opción C — Ver la bitácora completa del proceso

**Swagger:** sección **Audit Log** → `GET /auditLog` → **Try it out**

Cualquier usuario autenticado sirve.

Muestra las 5 operaciones registradas en orden cronológico:

| Operación | Quién | Estado resultante |
|-----------|-------|-------------------|
| `LOAN_REQUEST` | client_company | UNDER_REVIEW |
| `Rejection_Loan` | analyst | REJECTED |
| `LOAN_REQUEST` | client_company | UNDER_REVIEW |
| `Approval_Loan` | analyst | APPROVED |
| `Disbursement_Loan` | analyst | DISBURSED |

---

## Resumen del flujo

```
[Empresa: client_company]        [Analista: analyst]
         |                               |
         |-- POST /loans (50M) --------> |  status: UNDER_REVIEW
         |                               |
         |     POST /loans/{id}/reject --|  status: REJECTED
         |                               |
         |-- POST /loans (30M) --------> |  status: UNDER_REVIEW
         |                               |
         |    POST /loans/{id}/approve --|  status: APPROVED
         |                               |
         |   POST /loans/{id}/disburse --|  status: DISBURSED
         |                               |
         [cuenta 10000001: +$30,000,000 COP]
```

---

## Errores comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `403 Forbidden` | Rol incorrecto para el endpoint | Revisa el usuario activo en Swagger (Authorize) |
| `Applicant client not found` | El `applicantClientId` no existe en BD | Usa exactamente `"empresa_demo_id"` |
| `Not authorized to request loans for another client` | El usuario no coincide con el cliente | `client_company` solo puede pedir préstamos para `empresa_demo_id` |
| `Destination account does not belong to the loan client` | La cuenta destino es de otro cliente | Usa `"10000001"` que pertenece a `empresa_demo_id` |
| `Loan cannot be rejected` | El préstamo no está en `UNDER_REVIEW` | Solo se puede rechazar cuando está en revisión |
