## Objetivo de la demo

Mostrar un flujo completo y estable del proyecto:

1. levantar la API
2. crear un cliente
3. registrar el usuario del sistema asociado a ese cliente
4. abrir una cuenta
5. hacer un deposito
6. hacer un retiro
7. consultar saldo
8. listar transacciones

Este es el flujo mas seguro para mostrar porque respeta las dependencias reales del sistema.

---

## 1. Levantar el proyecto

En PowerShell:

```powershell
cd "c:\Users\migue\OneDrive\Documents\GitHub\ConstruccionDeSoftware22026-MiguelLopez\bank-api"
.\mvnw.cmd spring-boot:run
```

Cuando veas algo como:

```text
Tomcat started on port 8080
Started BankApiApplication
```

abre:

```text
http://localhost:8080/swagger-ui.html
```

No cierres esa terminal porque si la cierras la API se apaga.

---

## 2. Usuarios para la demo

Usaremos estos dos:

- `analyst / 123456`
- `teller / 123456`

En Swagger:

1. pulsa `Authorize`
2. escribe `analyst` y `123456`
3. prueba los endpoints del bloque de analista
4. cuando toque caja, vuelve a `Authorize` y cambia a `teller / 123456`

---

## 3. Orden correcto del flujo

Importante: este proyecto tiene dependencias entre entidades.

El orden correcto es:

1. crear cliente
2. guardar el `clientId`
3. registrar usuario del sistema para ese cliente
4. abrir cuenta para ese cliente
5. guardar el `accountId`
6. depositar
7. retirar
8. consultar saldo
9. listar transacciones

Si intentas crear la cuenta antes del usuario del sistema, el sistema falla.

---

## 4. Paso 1: crear cliente

Endpoint:

```text
POST /clients
```

Usuario:

```text
analyst
```

Body:

```json
{
  "identificationId": "1234567890",
  "name": "Juan Perez",
  "email": "juan.perez.demo@bank.com",
  "phone": "3001234567",
  "birthDate": "1990-05-15",
  "address": "Calle 123 Bogota",
  "typeClient": "NATURAL_PERSON_CLIENT",
  "legalRepresentativeId": null
}
```

### Que debes guardar

De la respuesta guarda:

- `id`
- `identificationId`

Ejemplo:

```text
clientId = 1f23ab...
identificationId = 1234567890
```

---

## 5. Paso 2: registrar usuario del sistema del cliente

Endpoint:

```text
POST /users
```

Usuario:

```text
analyst
```

Este paso es obligatorio porque la apertura de cuenta valida que el cliente tenga un usuario del sistema activo.

Body:

```json
{
  "userId": 1001,
  "idRelated": "CLIENT_ID_AQUI",
  "fullName": "Juan Perez",
  "identificationId": "1234567890",
  "email": "juan.perez.demo@bank.com",
  "phone": "3001234567",
  "birthDate": "1990-05-15",
  "address": "Calle 123 Bogota",
  "systemRole": "NATURAL_PERSON_CLIENT",
  "userStatus": "ACTIVE"
}
```

### Que debes reemplazar

- `CLIENT_ID_AQUI` por el `id` del cliente creado en el paso anterior

### Que debes guardar

De la respuesta guarda:

- `userId`

---

## 6. Paso 3: abrir cuenta

Endpoint:

```text
POST /accounts
```

Usuario:

```text
analyst
```

Body:

```json
{
  "accountNumber": "10000001",
  "initialBalance": 500000,
  "accountType": "SAVINGS",
  "clientId": "CLIENT_ID_AQUI"
}
```

### Que debes reemplazar

- `CLIENT_ID_AQUI` por el `id` del cliente

### Que debes guardar

De la respuesta guarda:

- `id`
- `accountNumber`

Ejemplo:

```text
accountId = 8ab3cd...
accountNumber = 10000001
```

---

## 7. Paso 4: depositar dinero

Ahora cambia la autenticacion a:

```text
teller / 123456
```

Endpoint:

```text
POST /accounts/deposit
```

Body:

```json
{
  "accountId": "ACCOUNT_ID_AQUI",
  "identificationIdClient": "1234567890",
  "amount": 200000
}
```

### Que debes reemplazar

- `ACCOUNT_ID_AQUI` por el `id` de la cuenta

### Resultado esperado

Respuesta `204 No Content`.

Eso significa que el deposito fue exitoso.

---

## 8. Paso 5: retirar dinero

Endpoint:

```text
POST /accounts/withdraw
```

Usuario:

```text
teller
```

Body:

```json
{
  "accountId": "ACCOUNT_ID_AQUI",
  "identificationIdClient": "1234567890",
  "amount": 50000
}
```

### Resultado esperado

Respuesta `204 No Content`.

---

## 9. Paso 6: consultar saldo

Endpoint:

```text
GET /accounts/{id}/balance
```

Usuario:

```text
teller
```

Usa el `accountId` en la URL y agrega este query param:

```text
identificationIdClient=1234567890
```

Ejemplo:

```text
GET /accounts/ACCOUNT_ID_AQUI/balance?identificationIdClient=1234567890
```

### Resultado esperado

Si empezaste con `500000`, luego depositaste `200000` y retiraste `50000`, el saldo final esperado es:

```text
650000
```

---

## 10. Paso 7: ver la cuenta

Endpoint:

```text
GET /accounts/{id}
```

Usuario:

```text
teller
```

Esto te sirve para mostrar que la cuenta existe y que el estado sigue activo.

---

## 11. Paso 8: listar transacciones

Endpoint:

```text
GET /transactions
```

Usuario:

```text
teller
```

### Resultado esperado

Deberias ver al menos:

- una transaccion tipo `DEPOSIT`
- una transaccion tipo `WITHDRAWAL`

Esto sirve para mostrar trazabilidad de operaciones.

---

## 12. Flujo resumido para decirlo en voz alta

Puedes explicarlo asi:

1. primero creo el cliente
2. luego registro el usuario del sistema asociado a ese cliente
3. despues abro la cuenta
4. cambio al rol de caja para operar sobre esa cuenta
5. realizo un deposito
6. realizo un retiro
7. consulto el saldo final
8. finalmente reviso el historial de transacciones

---

## 13. Que esta demostrando este flujo

Con este recorrido muestras:

- autenticacion por roles
- creacion de cliente
- asociacion del cliente con un usuario del sistema
- apertura de cuenta
- deposito
- retiro
- consulta de saldo
- persistencia de transacciones

---

## 14. Cosas que no conviene mostrar primero

No recomiendo arrancar la demo con:

- `GET /accounts` usando `teller`
- transferencias con `client_natural`
- prestamos con `client_natural`
- flujos de empresa

Porque varios de esos dependen de mapeos adicionales de usuario-cliente y pueden distraer la demostracion principal.

Si el profesor pregunta por esos flujos, puedes decir:

```text
El sistema los soporta, pero para la demo principal elegi el flujo interno mas estable:
cliente -> usuario del sistema -> cuenta -> deposito -> retiro -> saldo -> transacciones.
```

---

## 15. Endpoints exactos de la demo

En orden:

1. `POST /clients`
2. `POST /users`
3. `POST /accounts`
4. `POST /accounts/deposit`
5. `POST /accounts/withdraw`
6. `GET /accounts/{id}/balance`
7. `GET /accounts/{id}`
8. `GET /transactions`

---

## 16. Si algo falla

### Si sale `403 Forbidden`

Normalmente es por rol incorrecto.

- usa `analyst` para `/clients`, `/users`, `/accounts`
- usa `teller` para `/accounts/deposit`, `/accounts/withdraw`, `/transactions`

### Si sale `Account not found`

Revisa que estas usando el `accountId` y no el `accountNumber`.

### Si sale error sobre usuario del sistema

Significa que faltó el paso `POST /users` o el usuario no quedó en estado `ACTIVE`.

### Si sale error por puerto

Puede que ya haya otra instancia corriendo en `8080`.

---

## 17. Cierre sugerido para la sustentacion

Puedes cerrar diciendo:

```text
En esta demostracion mostre un flujo completo del dominio bancario con control de acceso por roles,
dependencias reales entre cliente, usuario y cuenta, operaciones de caja y trazabilidad mediante transacciones.
```
