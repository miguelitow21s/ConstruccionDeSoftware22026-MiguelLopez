# Bank API - DDD + Hexagonal (Spring Boot)

Proyecto backend bancario en Java con enfoque empresarial usando **Domain Driven Design** y **Arquitectura Hexagonal**.

## 1) Explicacion General
La API implementa operaciones nucleares de banca:
- Creacion de clientes
- Apertura de cuentas
- Consulta de saldo
- Depositos y retiros
- Transferencias (con flujo de aprobacion para alto monto)
- Listado de transacciones

El diseno separa negocio puro (dominio) de detalles tecnicos (JPA, REST, seguridad), para que el sistema sea mantenible, testeable y escalable.

## 2) DDD Aplicado
- **Bounded Context**: Core bancario transaccional.
- **Entidades**: `Cliente`, `Cuenta`, `Transaccion`.
- **Value Objects**: `Dinero`, `NumeroCuenta`, `Email`.
- **Servicios de Dominio**: `ServicioCuenta`, `ServicioTransferencia`.
- **Puertos** (application): contratos de persistencia desacoplados (`ClienteRepositoryPort`, etc).
- **Adaptadores** (infrastructure): implementaciones JPA y almacenamiento de bitacora.

## 3) Estructura del Proyecto
```text
bank-api/
  pom.xml
  src/main/java/com/bank/
    BankApiApplication.java
    domain/
      entities/
      valueobjects/
      repositories/
      services/
      exceptions/
    application/
      ports/
      usecases/
    infrastructure/
      persistence/
        entities/
        repositories/
        mappers/
        adapters/
        nosql/
      config/
    interfaces/
      controllers/
      dtos/
  src/main/resources/
    application.yml
```

## 4) Como Interactuan las Capas
- `interfaces` recibe HTTP y transforma DTOs.
- `application` ejecuta casos de uso y coordina puertos.
- `domain` aplica reglas de negocio puras.
- `infrastructure` resuelve persistencia, seguridad y configuracion.

Flujo ejemplo (transferencia):
1. `CuentaController` recibe request.
2. `TransferirDineroUseCase` carga cuentas via puertos.
3. `ServicioTransferencia` valida y ejecuta logica.
4. Adaptadores JPA guardan estados y transaccion.

## 5) Plan por Fases Academicas
- **Fase 1 (15 marzo, 10%)**
  - Modelo de dominio: entidades, value objects, enums y reglas basicas.
- **Fase 2 (5 abril, 10%)**
  - Puertos, servicios de dominio y casos de uso.
- **Parcial (6-9 abril, 20%)**
  - Evidencia de arquitectura, casos de uso y pruebas funcionales.
- **Fase 3 (11-30 mayo, 20%)**
  - API web completa, persistencia JPA, seguridad y flujos.
- **Fase 4 (11-30 mayo, 20%)**
  - Sustentacion, endurecimiento, bitacora NoSQL real (Mongo) y pruebas.
- **Final (1-6 junio, 20%)**
  - Cierre con pruebas integrales y evidencias tecnicas.

## 6) Ejecucion del Proyecto
Requisitos:
- Java 17+
- Maven 3.9+

Comandos:
```bash
cd bank-api
mvn spring-boot:run
```

URLs:
- API: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`

Credenciales basicas (Spring Security en memoria):
- `analista / 123456`
- `ventanilla / 123456`
- `comercial / 123456`
- `supervisor / 123456`

## 7) Endpoints Principales
- `POST /clientes`
- `POST /cuentas`
- `GET /cuentas/{id}/saldo`
- `POST /cuentas/depositar`
- `POST /cuentas/retirar`
- `POST /cuentas/transferir`
- `GET /transacciones`

Adicionales para flujo empresarial:
- `POST /cuentas/transferencias/{id}/aprobar`
- `POST /cuentas/transferencias/{id}/rechazar`

## 8) Ejemplos HTTP
Crear cliente:
```http
POST /clientes
Authorization: Basic YW5hbGlzdGE6MTIzNDU2
Content-Type: application/json

{
  "nombre": "Miguel Lopez",
  "email": "miguel@bank.com",
  "telefono": "3001234567"
}
```

Crear cuenta:
```http
POST /cuentas
Authorization: Basic YW5hbGlzdGE6MTIzNDU2
Content-Type: application/json

{
  "numeroCuenta": "1234567890",
  "saldoInicial": 500000,
  "tipoCuenta": "AHORROS",
  "clienteId": "<clienteId>"
}
```

Depositar:
```http
POST /cuentas/depositar
Authorization: Basic dmVudGFuaWxsYToxMjM0NTY=
Content-Type: application/json

{
  "cuentaId": "<cuentaId>",
  "monto": 100000
}
```

Transferir:
```http
POST /cuentas/transferir
Authorization: Basic dmVudGFuaWxsYToxMjM0NTY=
Content-Type: application/json

{
  "cuentaOrigenId": "<cuentaIdOrigen>",
  "cuentaDestinoId": "<cuentaIdDestino>",
  "monto": 20000,
  "operacionEmpresarial": true
}
```

## 9) Prueba en Postman
1. Crear coleccion `Bank API`.
2. Configurar `Authorization -> Basic Auth` con usuario/clave.
3. Ejecutar flujo sugerido:
   - Crear cliente
   - Crear 2 cuentas
   - Depositar en cuenta origen
   - Transferir
   - Consultar saldo en ambas cuentas
   - Listar transacciones
4. Si una transferencia queda en `EN_ESPERA_APROBACION`, usar endpoint de aprobar/rechazar.

## 10) Nota de Evolucion
La bitacora se implemento como adapter en memoria para demostrar el puerto NoSQL.
En fase final puedes reemplazar ese adapter por MongoDB sin tocar la logica de dominio ni casos de uso.
