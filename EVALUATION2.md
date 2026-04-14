# EVALUATION 2 - ConstruccionDeSoftware22026-MiguelLopez

## General information
- Student(s): Team members not declared in README.md
- Evaluated branch: main
- Evaluated commit: da2214b3afbb05dc7e26ef7ed7ec935311fd21ff
- Date: 2026-04-11
- Note: Spring Boot project with complete hexagonal architecture. Domain layer is in `bank-api/src/main/java/`. This is a standout reference implementation from the group. Working subdirectory: `bank-api/`.

---

## Grading table

| # | Criterion | Weight | Score (1-5) | Contribution |
|---|----------|--------|-------------|--------------|
| 1 | Domain modeling | 20% | 5 | 1.00 |
| 2 | Port modeling | 20% | 5 | 1.00 |
| 3 | Domain service modeling | 20% | 5 | 1.00 |
| 4 | Enums and states | 10% | 5 | 0.50 |
| 5 | Critical business rules | 10% | 5 | 0.50 |
| 6 | Audit log and traceability | 5% | 5 | 0.25 |
| 7 | Internal domain structure | 10% | 5 | 0.50 |
| 8 | Baseline technical quality in domain | 5% | 4 | 0.20 |
| | **Base total** | **100%** | | **4.95** |

**Formula:** Score = sum(score_i × weight_i) / 100

---

## Penalties

| Penalty | Percentage | Justification |
|---|---|---|
| Spanish code | -20% | The entire domain layer used Spanish naming: `ServicioCuenta`, `ServicioTransferencia`, `EstadoCuenta`, `Dinero`, `NumeroCuenta`, `aprobarYEjecutar()`, `validarCuentaOperativa()`, etc. |

**Base score:** 4.95  
**Penalty:** -20%  
**Adjusted score:** 4.95 × 0.80 = 3.96

---

## Bonus

| Bonus | Value | Justification |
|---|---|---|
| Strong reusable port design | +0.2 | 7 semantic repository ports: `ClienteRepositoryPort`, `CuentaRepositoryPort`, `PrestamoRepositoryPort`, `TransaccionRepositoryPort`, `UsuarioSistemaRepositoryPort`, `ProductoBancarioRepositoryPort`, `BitacoraRepositoryPort`. |
| Strong service design with high cohesion | +0.2 | `VencerTransferenciasPendientesUseCase`, `AprobarPrestamoUseCase`, `DesembolsarPrestamoUseCase`, etc. Atomic use cases, each with focused domain service and port usage. |
| Excellent audit traceability | +0.1 | `BitacoraRepositoryPort` supports dual backend (memory / MongoDB) with configuration-based switching. `BitacoraDocument` for NoSQL support. |
| **Total bonus** | **+0.5** | |

**Score with bonus:** 3.96 + 0.50 = **4.46**

---

## Final score
**4.46 / 5.0**

---

## Findings

### Strengths
- **Business value objects:** `Email` with regex validation, `Dinero` (monetary value in `BigDecimal` with `sumar()`, `restar()`, `esMenorQue()`), `NumeroCuenta`. High domain encapsulation.
- **Comprehensive enums:** `EstadoCuenta`, `EstadoPrestamo`, `EstadoTransaccion`, `EstadoUsuario`, `RolSistema`, `TipoCliente`, `TipoCuenta`, `TipoTransaccion`, `TipoPrestamo`, `CategoriaProducto` (10+ enums covering all required catalogs).
- **Entities with state machine behavior:** `Prestamo.aprobar()`, `rechazar()`, `desembolsar()` with transition validation. `Transaccion.aprobarYEjecutar()`, `rechazar()`, `vencer()`. `Cuenta.validarCuentaOperativa()` enforced before each operation.
- **`VencerTransferenciasPendientesUseCase`** explicitly implements the 60-minute expiration rule for high-amount transfers.
- **Dual audit-log repository:** configurable memory and MongoDB support without domain changes.
- **7 repository ports** with semantic signatures; no technology-first CRUD naming.
- **All required entities present:** `Cliente`, `Cuenta`, `Prestamo`, `Transaccion`, `UsuarioSistema`, `ProductoBancario`.

### Weaknesses
- **Spanish naming penalty** significantly impacts final grade (-20%). With equivalent quality and English naming, this would have reached 5.0.
- Penalty applied strictly according to rubric; technical design quality is excellent.

### Naming observation
The README in `bank-api/README.md` includes JSON examples with the name "Miguel Lopez" but does not formally declare the student(s). Traceability note kept as "Team members not declared in README.md".

---

## Recommendations
1. For future submissions: use English naming for classes, methods, and variables (per active rubric).
2. The design is reference-level: maintain the use of value objects, aggregate-focused ports, and atomic use cases.