# Guía completa del proyecto — Banco API

## ¿Qué es este proyecto?

Es una **aplicación web de un banco** que maneja clientes, cuentas, préstamos y transferencias. Funciona como un servidor (API REST): no tiene pantallas ni botones, sino que recibe peticiones y responde con datos en formato JSON. Se prueba con Postman o Swagger.

---

## Cómo arrancar el proyecto

1. Abre el proyecto en IntelliJ IDEA o VSCode.
2. Ejecuta la clase `BankApiApplication.java` (es el punto de entrada, tiene el método `main`).
3. El servidor arranca en `http://localhost:8080`.
4. Para ver todas las APIs disponibles visualmente, abre en el navegador:
   ```
   http://localhost:8080/swagger-ui.html
   ```
   Desde Swagger puedes probar todo sin Postman.

---

## Dónde está cada cosa

El proyecto usa **arquitectura hexagonal** (también llamada "puertos y adaptadores"). En palabras simples:

| Carpeta | Qué hay ahí | Analogía simple |
|---------|------------|-----------------|
| `domain/entities/` | Las "cosas" del banco: Cuenta, Préstamo, Transacción, Cliente, Usuario | Los objetos del mundo real |
| `domain/valueobjects/` | Tipos especiales: Dinero (`Money`), Número de cuenta, Email | Como tipos de dato más inteligentes |
| `domain/entities/` (enums) | Los estados posibles: `AccountStatus`, `LoanStatus`, etc. | Las opciones de un menú desplegable |
| `application/usecases/` | Lo que el sistema puede **hacer**: crear cuenta, aprobar préstamo, etc. | Los casos de uso / funcionalidades |
| `application/ports/` | Contratos (interfaces) que dicen qué operaciones se pueden hacer con la BD | El menú de lo que la BD ofrece |
| `infrastructure/` | La implementación real de la BD (H2 en memoria, MongoDB para bitácora) | La cocina que nadie ve |
| `interfaces/controllers/` | Las APIs que recibe Postman | Las ventanillas del banco |
| `interfaces/dtos/` | Los formatos de los datos que entran y salen | Los formularios |

---

## Los usuarios del sistema (cómo hacer login)

El sistema usa **autenticación básica** (usuario + contraseña en cada petición).

En Postman: pestaña **Authorization** → tipo **Basic Auth** → poner usuario y contraseña.

| Usuario | Contraseña | Rol | Qué puede hacer |
|---------|-----------|-----|-----------------|
| `analyst` | `123456` | Analista Interno | Aprobar/rechazar préstamos, ver todo, registrar usuarios |
| `teller` | `123456` | Cajero (Ventanilla) | Depósitos, retiros, abrir cuentas, consultar saldo |
| `sales` | `123456` | Empleado Comercial | Solicitar productos para clientes, ver clientes asignados |
| `supervisor` | `123456` | Supervisor de Empresa | Aprobar/rechazar transferencias de su empresa |
| `company_employee` | `123456` | Empleado de Empresa | Crear transferencias y pagos masivos |
| `client_natural` | `123456` | Cliente Persona Natural | Ver sus propias cuentas, préstamos, transferencias |
| `client_company` | `123456` | Cliente Empresa | Ver productos de su empresa |

> **Nota importante:** Los clientes `client_natural` y `client_company` están mapeados a IDs de prueba en la configuración. En un sistema real esto vendría de la base de datos.

---

## Todas las APIs — Cómo probarlas en Postman

### Configuración base en Postman
- URL base: `http://localhost:8080`
- Siempre agregar `Authorization: Basic Auth` con usuario y contraseña
- En el header: `Content-Type: application/json`

---

### CLIENTES (`/clients`)

#### Crear cliente
```
POST http://localhost:8080/clients
Usuario: teller (o analyst o sales)

Body:
{
  "identificationId": "1234567890",
  "name": "Juan Pérez",
  "email": "juan@email.com",
  "phone": "3001234567",
  "birthDate": "1990-05-15",
  "address": "Calle 123 Bogotá",
  "typeClient": "NATURAL_PERSON_CLIENT",
  "legalRepresentativeId": null
}
```

#### Crear cliente empresa
```
POST http://localhost:8080/clients
Usuario: analyst

Body:
{
  "identificationId": "NIT900123456",
  "name": "Empresa XYZ SAS",
  "email": "empresa@xyz.com",
  "phone": "6012345678",
  "birthDate": null,
  "address": "Av Empresarial 456",
  "typeClient": "BUSINESS_CLIENT",
  "legalRepresentativeId": "ID_DEL_REPRESENTANTE_LEGAL"
}
```

#### Ver un cliente
```
GET http://localhost:8080/clients/{id}
Usuario: analyst o sales
```

#### Listar todos los clientes
```
GET http://localhost:8080/clients
Usuario: analyst
```

---

### CUENTAS (`/accounts`)

#### Crear cuenta
```
POST http://localhost:8080/accounts
Usuario: teller

Body:
{
  "accountNumber": "10000001",
  "initialBalance": 500000,
  "accountType": "SAVINGS",
  "clientId": "ID_DEL_CLIENTE"
}
```
Tipos de cuenta: `SAVINGS`, `CHECKING`, `PERSONAL`, `BUSINESS`

#### Listar cuentas
```
GET http://localhost:8080/accounts
Usuario: cualquiera autenticado (cada rol ve lo suyo)
```

#### Ver cuenta por ID
```
GET http://localhost:8080/accounts/{id}
Usuario: cualquiera autenticado
```

#### Ver saldo
```
GET http://localhost:8080/accounts/{id}/balance
Usuario: cualquiera autenticado
(Si es cajero, agregar ?identificationIdClient=CEDULA_CLIENTE)
```

#### Depositar dinero
```
POST http://localhost:8080/accounts/deposit
Usuario: teller

Body:
{
  "accountId": "ID_DE_LA_CUENTA",
  "identificationIdClient": "1234567890",
  "amount": 200000
}
```

#### Retirar dinero
```
POST http://localhost:8080/accounts/withdraw
Usuario: teller

Body:
{
  "accountId": "ID_DE_LA_CUENTA",
  "identificationIdClient": "1234567890",
  "amount": 50000
}
```

#### Transferir dinero
```
POST http://localhost:8080/accounts/transfer
Usuario: client_natural, client_company o company_employee

Body:
{
  "sourceAccountId": "ID_CUENTA_ORIGEN",
  "destinationAccountId": "ID_CUENTA_DESTINO",
  "amount": 15000000,
  "businessOperation": true
}
```
> Si el monto supera $10.000 y es operación empresarial (`businessOperation: true`), queda en estado `AWAITING_APPROVAL` (espera aprobación del supervisor). Si no, se ejecuta inmediatamente.

#### Pago masivo (nómina)
```
POST http://localhost:8080/accounts/bulk-payments
Usuario: company_employee

Body:
{
  "sourceAccountId": "ID_CUENTA_EMPRESA",
  "payments": [
    { "destinationAccountId": "ID_CUENTA_EMPLEADO_1", "amount": 3000000 },
    { "destinationAccountId": "ID_CUENTA_EMPLEADO_2", "amount": 2500000 }
  ]
}
```

#### Aprobar transferencia (supervisor)
```
POST http://localhost:8080/accounts/transfers/{id}/approve
Usuario: supervisor
```

#### Rechazar transferencia (supervisor)
```
POST http://localhost:8080/accounts/transfers/{id}/reject
Usuario: supervisor
```

---

### PRÉSTAMOS (`/loans`)

#### Solicitar préstamo
```
POST http://localhost:8080/loans
Usuario: client_natural o sales

Body:
{
  "typeLoan": "CONSUMER",
  "applicantClientId": "ID_DEL_CLIENTE",
  "requestedAmount": 10000000,
  "interestRate": 12.5,
  "termMonths": 24
}
```
Tipos de préstamo: `CONSUMER`, `VEHICLE`, `MORTGAGE`, `BUSINESS`

El préstamo queda en estado `UNDER_REVIEW` (en estudio).

#### Listar préstamos
```
GET http://localhost:8080/loans
Usuario: cualquiera autenticado
```

#### Ver préstamo por ID
```
GET http://localhost:8080/loans/{id}
Usuario: cualquiera autenticado
```

#### Aprobar préstamo
```
POST http://localhost:8080/loans/{id}/approve
Usuario: analyst

Body:
{
  "approvedAmount": 9000000
}
```

#### Rechazar préstamo
```
POST http://localhost:8080/loans/{id}/reject
Usuario: analyst
```

#### Desembolsar préstamo
```
POST http://localhost:8080/loans/{id}/disburse
Usuario: analyst

Body:
{
  "destinationAccountNumber": "10000001"
}
```
Esto deposita el dinero en la cuenta del cliente y el saldo aumenta automáticamente.

---

### TRANSACCIONES (`/transactions`)

#### Listar transacciones
```
GET http://localhost:8080/transactions
Usuario: cualquiera autenticado
```

#### Ver transacciones pendientes de aprobación
```
GET http://localhost:8080/transactions?status=AWAITING_APPROVAL
Usuario: supervisor o analyst
```

#### Ver transacción por ID
```
GET http://localhost:8080/transactions/{id}
Usuario: cualquiera autenticado
```

Estados posibles: `PENDING`, `AWAITING_APPROVAL`, `EXECUTED`, `REJECTED`, `EXPIRED`

---

### BITÁCORA (`/auditLog`)

#### Ver bitácora completa
```
GET http://localhost:8080/auditLog
Usuario: analyst
```

#### Filtrar por usuario
```
GET http://localhost:8080/auditLog?userId=1
Usuario: analyst
```

#### Filtrar por producto
```
GET http://localhost:8080/auditLog?affectedProductId=ID_DE_CUENTA_O_PRESTAMO
Usuario: cualquiera autenticado (clientes solo ven sus propios productos)
```

---

### USUARIOS DEL SISTEMA (`/users`)

#### Registrar usuario del sistema
```
POST http://localhost:8080/users
Usuario: analyst

Body:
{
  "userId": 1,
  "idRelated": null,
  "fullName": "Ana González",
  "identificationId": "87654321",
  "email": "ana@banco.com",
  "phone": "3109876543",
  "birthDate": "1985-03-20",
  "address": "Carrera 45 #12-30",
  "systemRole": "TELLER_EMPLOYEE",
  "userStatus": "ACTIVE"
}
```
Roles: `NATURAL_PERSON_CLIENT`, `BUSINESS_CLIENT`, `TELLER_EMPLOYEE`, `COMMERCIAL_EMPLOYEE`, `COMPANY_EMPLOYEE`, `COMPANY_SUPERVISOR`, `INTERNAL_ANALYST`

#### Ver usuario
```
GET http://localhost:8080/users/{userId}
Usuario: analyst
```

#### Activar / Desactivar / Bloquear usuario
```
POST http://localhost:8080/users/{userId}/activate
POST http://localhost:8080/users/{userId}/deactivate
POST http://localhost:8080/users/{userId}/block
Usuario: analyst
```

---

### USUARIOS OPERATIVOS DE EMPRESA (`/companies/operational-users`)

#### Listar usuarios de la empresa
```
GET http://localhost:8080/companies/operational-users
Usuario: supervisor
```

#### Registrar usuario operativo
```
POST http://localhost:8080/companies/operational-users
Usuario: supervisor

Body:
{
  "username": "emp_juan",
  "fullName": "Juan Operativo",
  "email": "juan.op@empresa.com"
}
```

#### Activar / Desactivar
```
POST http://localhost:8080/companies/operational-users/{username}/activate
POST http://localhost:8080/companies/operational-users/{username}/deactivate
Usuario: supervisor
```

---

## Flujos completos para demostrar

### Flujo 1: Préstamo completo (el más importante)
1. `POST /clients` → crear cliente (con `teller`)
2. `POST /accounts` → crear cuenta para ese cliente (con `teller`)
3. `POST /loans` → solicitar préstamo (con `client_natural` o `sales`)
4. `POST /loans/{id}/approve` → aprobar (con `analyst`) → estado cambia a `APPROVED`
5. `POST /loans/{id}/disburse` → desembolsar (con `analyst`) → el saldo de la cuenta sube
6. `GET /accounts/{id}/balance` → verificar que el saldo aumentó
7. `GET /auditLog` → ver que quedó registrado todo

### Flujo 2: Transferencia de alto valor (requiere aprobación)
1. Crear dos cuentas
2. `POST /accounts/transfer` con `amount` mayor a 10.000 y `businessOperation: true` (con `company_employee`)
3. La transferencia queda en `AWAITING_APPROVAL`
4. `POST /accounts/transfers/{id}/approve` (con `supervisor`) → se ejecuta y los saldos cambian
5. Esperar 60 minutos sin aprobar → cambia automáticamente a `EXPIRED`

### Flujo 3: Transferencia normal (se ejecuta sola)
1. `POST /accounts/transfer` con monto menor a 10.000
2. Estado va directo a `EXECUTED`
3. Los saldos se actualizan inmediatamente

---

## Preguntas frecuentes que te pueden hacer

**¿Qué es la arquitectura hexagonal?**
Es una forma de organizar el código donde el negocio (dominio) no sabe nada de la base de datos ni del internet. El dominio es el centro, y todo lo demás (BD, APIs, etc.) se conecta desde afuera. Así si cambias la BD de H2 a PostgreSQL, el negocio no cambia.

**¿Dónde están las reglas de negocio?**
En `domain/entities/`. Por ejemplo:
- `Loan.java` tiene `approve()`, `reject()`, `disburse()` y valida que solo se puede aprobar si está en revisión.
- `Account.java` tiene `validateOperationalAccount()` que impide operaciones en cuentas bloqueadas.
- `Transaction.java` tiene `expire()` para vencimiento automático.

**¿Qué son los value objects?**
Son tipos de dato que tienen validación integrada. `Money` valida que el dinero no sea null y lo redondea a 2 decimales. `Email` valida el formato. `AccountNumber` valida el número. No son simples `String`.

**¿Qué son los puertos?**
Son interfaces (contratos) en `application/ports/`. Dicen "necesito poder guardar una cuenta, buscarla por ID, etc." pero no dicen cómo. La implementación real está en `infrastructure/persistence/adapters/`.

**¿Por qué hay dos bases de datos?**
- **H2 (SQL):** Para cuentas, clientes, préstamos, transacciones. Datos estructurados que necesitan consistencia.
- **MongoDB (NoSQL):** Solo para la bitácora. Los registros de bitácora tienen estructura variable (una transferencia registra saldos antes/después, un préstamo registra tasas y montos) por eso NoSQL es ideal.

**¿Cómo funciona la seguridad?**
Cada endpoint tiene `@PreAuthorize("hasRole('ANALYST')")` o similar. Spring Security verifica el rol del usuario autenticado antes de ejecutar el código. Si no tiene permiso, devuelve error 403.

**¿Qué pasa si una transferencia no se aprueba en 60 minutos?**
Hay una tarea programada (`ExpirePendingTransfersUseCase`) que corre cada minuto, busca transferencias en `AWAITING_APPROVAL` con más de 60 minutos, y las cambia a `EXPIRED`. Registra el evento en la bitácora con el motivo.

**¿Por qué el código está en inglés?**
Por buena práctica de programación y porque la evaluación anterior penalizó el código en español con -20% en la nota.

**¿Qué es un enum?**
Es una lista fija de valores posibles. `AccountStatus` solo puede ser `ACTIVE`, `BLOCKED` o `CANCELLED`. Evita errores de escribir "activo" vs "Activo" vs "ACTIVO".

**¿Dónde se valida que el cliente sea mayor de edad?**
En `CreateClientUseCase.java`. Calcula la diferencia entre la fecha de nacimiento y hoy, y si es menor de 18 años lanza un error.

**¿Cómo se prueba el vencimiento sin esperar 60 minutos?**
En `application.yml` está la configuración `approval-expiration-minutes: 60`. Se puede cambiar a 1 minuto para probar. También existe el endpoint `GET /transactions?status=AWAITING_APPROVAL` para ver las pendientes.

---

## Dónde están los tests

En `src/test/java/com/bank/`. Hay tests para:
- Cada use case importante (crear cliente, solicitar préstamo, aprobar, etc.)
- Seguridad (que un cliente no pueda ver datos de otro)
- Entidades del dominio (que las transiciones de estado funcionen)

Se corren con: clic derecho en la carpeta `test` → "Run All Tests" en IntelliJ.

---

## Base de datos

- **H2 (en memoria):** Se resetea cada vez que reinicias el servidor. Para verla: `http://localhost:8080/h2-console` con JDBC URL `jdbc:h2:mem:bankdb`, usuario `sa`, contraseña vacía.
- **MongoDB:** La bitácora. Si no tienes MongoDB instalado localmente, el sistema usa almacenamiento en memoria (configurado en `application.yml` con `bank.bitacora.storage: memory`).

---

## Resumen rápido de endpoints

| Método | URL | Quién puede | Qué hace |
|--------|-----|-------------|---------|
| POST | /clients | analyst, teller, sales | Crear cliente |
| GET | /clients | analyst | Listar todos los clientes |
| GET | /clients/{id} | analyst, sales | Ver cliente |
| POST | /accounts | analyst, teller, sales | Crear cuenta |
| GET | /accounts | todos | Listar cuentas (filtrado por rol) |
| GET | /accounts/{id} | todos | Ver cuenta |
| GET | /accounts/{id}/balance | todos | Ver saldo |
| POST | /accounts/deposit | teller | Depositar |
| POST | /accounts/withdraw | teller | Retirar |
| POST | /accounts/transfer | client, company_employee | Transferir |
| POST | /accounts/bulk-payments | company_employee | Pago masivo |
| POST | /accounts/transfers/{id}/approve | supervisor | Aprobar transferencia |
| POST | /accounts/transfers/{id}/reject | supervisor | Rechazar transferencia |
| POST | /loans | client, sales | Solicitar préstamo |
| GET | /loans | todos | Listar préstamos |
| GET | /loans/{id} | todos | Ver préstamo |
| POST | /loans/{id}/approve | analyst | Aprobar préstamo |
| POST | /loans/{id}/reject | analyst | Rechazar préstamo |
| POST | /loans/{id}/disburse | analyst | Desembolsar préstamo |
| GET | /transactions | todos | Listar transacciones |
| GET | /transactions/{id} | todos | Ver transacción |
| GET | /transactions?status= | todos | Filtrar por estado |
| GET | /auditLog | todos (filtrado) | Ver bitácora |
| POST | /users | analyst | Registrar usuario sistema |
| GET | /users/{id} | analyst | Ver usuario |
| POST | /users/{id}/activate | analyst | Activar usuario |
| POST | /users/{id}/deactivate | analyst | Desactivar usuario |
| POST | /users/{id}/block | analyst | Bloquear usuario |
| GET | /companies/operational-users | supervisor | Listar users empresa |
| POST | /companies/operational-users | supervisor | Registrar user empresa |
