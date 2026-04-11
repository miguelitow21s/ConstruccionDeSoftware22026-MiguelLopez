# EVALUACION 2 - ConstruccionDeSoftware22026-MiguelLopez

## Informacion general
- Estudiante(s): Integrantes no informados en README.md
- Rama evaluada: main
- Commit evaluado: da2214b3afbb05dc7e26ef7ed7ec935311fd21ff
- Fecha: 2026-04-11
- Nota: Proyecto Spring Boot con arquitectura hexagonal completa. Dominio en `bank-api/src/main/java/`. Implementacion de referencia destacada del grupo. El subdirectorio de trabajo es `bank-api/`.

---

## Tabla de calificacion

| # | Criterio | Peso | Puntaje (1-5) | Contribucion |
|---|----------|------|---------------|--------------|
| 1 | Modelado de dominio | 20% | 5 | 1.00 |
| 2 | Modelado de puertos | 20% | 5 | 1.00 |
| 3 | Modelado de servicios de dominio | 20% | 5 | 1.00 |
| 4 | Enums y estados | 10% | 5 | 0.50 |
| 5 | Reglas de negocio criticas | 10% | 5 | 0.50 |
| 6 | Bitacora y trazabilidad | 5% | 5 | 0.25 |
| 7 | Estructura interna de dominio | 10% | 5 | 0.50 |
| 8 | Calidad tecnica base en domain | 5% | 4 | 0.20 |
| | **Total base** | **100%** | | **4.95** |

**Formula:** Nota = sum(puntaje_i × peso_i) / 100

---

## Penalizaciones

| Penalizacion | Porcentaje | Justificacion |
|---|---|---|
| Codigo en espanol | -20% | Toda la capa de dominio usa nomenclatura en espanol: `ServicioCuenta`, `ServicioTransferencia`, `EstadoCuenta`, `Dinero`, `NumeroCuenta`, `aprobarYEjecutar()`, `validarCuentaOperativa()`, etc. |

**Nota base:** 4.95
**Penalizacion:** -20%
**Nota ajustada:** 4.95 × 0.80 = 3.96

---

## Bonus

| Bonus | Valor | Justificacion |
|---|---|---|
| Buen diseno de puertos reutilizables | +0.2 | 7 puertos de repositorio semanticos: `ClienteRepositoryPort`, `CuentaRepositoryPort`, `PrestamoRepositoryPort`, `TransaccionRepositoryPort`, `UsuarioSistemaRepositoryPort`, `ProductoBancarioRepositoryPort`, `BitacoraRepositoryPort`. |
| Buen diseno de servicios con alta cohesion | +0.2 | `VencerTransferenciasPendientesUseCase`, `AprobarPrestamoUseCase`, `DesembolsarPrestamoUseCase`, etc. Casos de uso atomicos cada uno con su servicio de dominio y puerto correspondiente. |
| Excelente trazabilidad en bitacora | +0.1 | `BitacoraRepositoryPort` con soporte dual (memoria / MongoDB) configurable. `BitacoraDocument` para NoSQL. |
| **Total bonus** | **+0.5** | |

**Nota con bonus:** 3.96 + 0.50 = **4.46**

---

## Nota final
**4.46 / 5.0**

---

## Hallazgos

### Fortalezas
- **Value Objects de negocio:** `Email` con validacion por expresion regular, `Dinero` (valor monetario en `BigDecimal` con operaciones `sumar()`, `restar()`, `esMenorQue()`), `NumeroCuenta`. Maximo nivel de encapsulacion del dominio.
- **Enums exhaustivos:** `EstadoCuenta`, `EstadoPrestamo`, `EstadoTransaccion`, `EstadoUsuario`, `RolSistema`, `TipoCliente`, `TipoCuenta`, `TipoTransaccion`, `TipoPrestamo`, `CategoriaProducto` — 10+ enums cubriendo todos los catalogos del enunciado.
- **Entidades con maquina de estados:** `Prestamo.aprobar()`, `rechazar()`, `desembolsar()` con validacion de transiciones. `Transaccion.aprobarYEjecutar()`, `rechazar()`, `vencer()`. `Cuenta.validarCuentaOperativa()` usado antes de cada operacion.
- **`VencerTransferenciasPendientesUseCase`** implementa explicitamente la regla de los 60 minutos de vencimiento de transferencias de alto monto.
- **Repositorio dual para bitacora:** soporte `memory` y `mongodb` configurables sin tocar el dominio.
- **7 puertos de repositorio** con firmas semanticas: ninguno usa terminos de CRUD tecnologico.
- **Todas las entidades del enunciado** presentes: `Cliente`, `Cuenta`, `Prestamo`, `Transaccion`, `UsuarioSistema`, `ProductoBancario`.

### Debilidades
- **Penalizacion por codigo en espanol** impacta significativamente la nota final (-20%). El mismo nivel de calidad con nomenclatura en ingles habria obtenido 5.0.
- La penalizacion es aplicada siguiendo la rubrica; el diseño tecnico es sobresaliente.

### Observacion de nomenclatura
El README en `bank-api/README.md` contiene ejemplos JSON con el nombre "Miguel Lopez" pero no declara formalmente al estudiante. Se deja trazabilidad de "Integrantes no informados en README.md".

---

## Recomendaciones
1. Para futuras entregas: usar nomenclatura en ingles para nombres de clases, metodos y variables (segun rubrica vigente).
2. El diseño es de referencia — mantener la practica de value objects, puertos por agregado y use cases atomicos.
