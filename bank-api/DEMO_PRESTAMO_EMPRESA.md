# Demo: Flujo de Préstamo para Empresa

Este documento explica paso a paso cómo ejecutar el flujo completo de solicitud de préstamo empresarial usando la API del banco.

## Contexto del escenario

| Rol | Usuario HTTP | Contraseña |
|-----|-------------|------------|
| Empresa (solicitante) | `client_company` | `123456` |
| Analista de banco | `analyst` | `123456` |

**Datos pre-cargados al iniciar la app** (creados automáticamente por `DemoDataInitializer`):

| Dato | Valor |
|------|-------|
| ID representante legal | `representante_demo_id` |
| Cédula representante | `12345678` |
| Nombre representante | Carlos Gomez |
| ID del cliente empresa | `empresa_demo_id` |
| NIT empresa | `900123456` |
| Nombre empresa | Tech Solutions Corp |
| Número de cuenta destino | `10000001` |

> **Por qué existe el representante legal:** `CreateClientUseCase` valida que el `legalRepresentativeId` de un `BUSINESS_CLIENT` apunte a un `NATURAL_PERSON_CLIENT` real. El representante se crea primero, y su ID se usa al registrar la empresa. En el préstamo el firmante es la empresa (NIT), pero el representante legal es quien tiene la responsabilidad civil.

---

## Herramienta recomendada

Abre Swagger UI en tu navegador: `http://localhost:8080/swagger-ui.html`

En cada endpoint, usa el botón **Authorize** (candado) e ingresa las credenciales del rol indicado.

---

## Paso 0 — Iniciar la aplicación

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Mac / Linux
./mvnw spring-boot:run
```

Al arrancar, `DemoDataInitializer` registra automáticamente el cliente empresa, su usuario de sistema activo y su cuenta bancaria. No necesitas crear nada manualmente.

---

## Primera interacción: la empresa solicita → el analista rechaza

### Paso 1 — La empresa solicita el préstamo

**Autenticarse como:** `client_company` / `123456`

```http
POST /loans
Content-Type: application/json
Authorization: Basic Y2xpZW50X2NvbXBhbnk6MTIzNDU2
```

```json
{
  "typeLoan": "BUSINESS",
  "applicantClientId": "empresa_demo_id",
  "requestedAmount": 50000000,
  "interestRate": 0.12,
  "termMonths": 36
}
```

**Respuesta esperada (201 Created):**

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

> Guarda el valor del campo `"id"` — lo necesitarás en el siguiente paso.

---

### Paso 2 — El analista rechaza el préstamo

**Autenticarse como:** `analyst` / `123456`

```http
POST /loans/{loan-id-1}/reject
Authorization: Basic YW5hbHlzdDoxMjM0NTY=
```

*(Sin cuerpo en la petición)*

**Respuesta esperada (200 OK):**

```json
{
  "id": "<loan-id-1>",
  "status": "REJECTED",
  ...
}
```

> El préstamo queda en estado `REJECTED`. Un préstamo rechazado **no puede reactivarse**, por eso la empresa debe hacer una nueva solicitud.

---

## Segunda interacción: la empresa vuelve a solicitar → el analista aprueba → se desembolsa

### Paso 3 — La empresa solicita un nuevo préstamo

**Autenticarse como:** `client_company` / `123456`

```http
POST /loans
Content-Type: application/json
Authorization: Basic Y2xpZW50X2NvbXBhbnk6MTIzNDU2
```

```json
{
  "typeLoan": "BUSINESS",
  "applicantClientId": "empresa_demo_id",
  "requestedAmount": 30000000,
  "interestRate": 0.10,
  "termMonths": 24
}
```

**Respuesta esperada (201 Created):**

```json
{
  "id": "<loan-id-2>",
  "status": "UNDER_REVIEW",
  ...
}
```

> Guarda el nuevo `"id"` para los siguientes pasos.

---

### Paso 4 — El analista aprueba el préstamo

**Autenticarse como:** `analyst` / `123456`

```http
POST /loans/{loan-id-2}/approve
Content-Type: application/json
Authorization: Basic YW5hbHlzdDoxMjM0NTY=
```

```json
{
  "approvedAmount": 30000000
}
```

> El analista puede aprobar un monto diferente al solicitado. En este caso aprobamos el monto completo.

**Respuesta esperada (200 OK):**

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

**Autenticarse como:** `analyst` / `123456`

```http
POST /loans/{loan-id-2}/disburse
Content-Type: application/json
Authorization: Basic YW5hbHlzdDoxMjM0NTY=
```

```json
{
  "destinationAccountNumber": "10000001"
}
```

> La cuenta `10000001` pertenece a `empresa_demo_id`. El sistema valida que la cuenta destino sea del mismo cliente del préstamo.

**Respuesta esperada (200 OK):**

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

## Resumen del flujo

```
[Empresa]               [Analista]
    |                       |
    |-- POST /loans -------> |  (UNDER_REVIEW)
    |                       |
    |          <-- reject --|  (REJECTED)
    |                       |
    |-- POST /loans -------> |  (UNDER_REVIEW)
    |                       |
    |          <-- approve--|  (APPROVED)
    |                       |
    |          <-- disburse-|  (DISBURSED)
    |                       |
    [Saldo +$30M en cuenta 10000001]
```

---

## Verificar el estado del préstamo en cualquier momento

```http
GET /loans/{loan-id}
```

## Verificar el saldo de la cuenta después del desembolso

```http
GET /accounts/empresa_account_001/balance
```

## Ver la bitácora de auditoría

```http
GET /auditLog
```

---

## Errores comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `403 Forbidden` | Rol incorrecto para el endpoint | Revisa que usas el usuario correcto (`client_company` para solicitar, `analyst` para aprobar/rechazar/desembolsar) |
| `Applicant client not found` | El `applicantClientId` no existe | Usa exactamente `"empresa_demo_id"` |
| `Not authorized to request loans for another client` | El usuario logueado no coincide con el cliente del préstamo | El usuario `client_company` solo puede pedir préstamos para `empresa_demo_id` |
| `Destination account does not belong to the loan client` | La cuenta de destino es de otro cliente | Usa la cuenta `10000001` que pertenece a `empresa_demo_id` |
| `Loan cannot be rejected` | El préstamo no está en `UNDER_REVIEW` | Solo se puede rechazar cuando está en revisión |
