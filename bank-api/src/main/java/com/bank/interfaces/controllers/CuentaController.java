package com.bank.interfaces.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.AprobarTransferenciaUseCase;
import com.bank.application.usecases.ConsultarSaldoUseCase;
import com.bank.application.usecases.CrearCuentaUseCase;
import com.bank.application.usecases.DepositarDineroUseCase;
import com.bank.application.usecases.RetirarDineroUseCase;
import com.bank.application.usecases.TransferirDineroUseCase;
import com.bank.interfaces.dtos.CrearCuentaRequest;
import com.bank.interfaces.dtos.CrearCuentaResponse;
import com.bank.interfaces.dtos.MovimientoRequest;
import com.bank.interfaces.dtos.SaldoResponse;
import com.bank.interfaces.dtos.TransaccionResponse;
import com.bank.interfaces.dtos.TransferenciaRequest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/cuentas")
@Tag(name = "Cuentas", description = "Gestión de cuentas bancarias y operaciones transaccionales")
@SecurityRequirement(name = "basicAuth")
public class CuentaController {

    private final CrearCuentaUseCase crearCuentaUseCase;
    private final ConsultarSaldoUseCase consultarSaldoUseCase;
    private final DepositarDineroUseCase depositarDineroUseCase;
    private final RetirarDineroUseCase retirarDineroUseCase;
    private final TransferirDineroUseCase transferirDineroUseCase;
    private final AprobarTransferenciaUseCase aprobarTransferenciaUseCase;

    public CuentaController(CrearCuentaUseCase crearCuentaUseCase,
                           ConsultarSaldoUseCase consultarSaldoUseCase,
                           DepositarDineroUseCase depositarDineroUseCase,
                           RetirarDineroUseCase retirarDineroUseCase,
                           TransferirDineroUseCase transferirDineroUseCase,
                           AprobarTransferenciaUseCase aprobarTransferenciaUseCase) {
        this.crearCuentaUseCase = crearCuentaUseCase;
        this.consultarSaldoUseCase = consultarSaldoUseCase;
        this.depositarDineroUseCase = depositarDineroUseCase;
        this.retirarDineroUseCase = retirarDineroUseCase;
        this.transferirDineroUseCase = transferirDineroUseCase;
        this.aprobarTransferenciaUseCase = aprobarTransferenciaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ANALISTA','VENTANILLA','COMERCIAL')")
    public CrearCuentaResponse crear(@Valid @RequestBody CrearCuentaRequest request) {
        var cuenta = crearCuentaUseCase.execute(
                request.numeroCuenta(),
                request.saldoInicial(),
                request.tipoCuenta(),
                request.clienteId()
        );

        return new CrearCuentaResponse(
                cuenta.getId(),
                cuenta.getNumeroCuenta().value(),
                cuenta.getSaldo().value(),
                cuenta.getTipoCuenta(),
                cuenta.getClienteId(),
                cuenta.getEstado()
        );
    }

    @GetMapping("/{id}/saldo")
    @PreAuthorize("hasAnyRole('ANALISTA','VENTANILLA','COMERCIAL','SUPERVISOR_EMPRESA','EMPLEADO_EMPRESA','CLIENTE_NATURAL','CLIENTE_EMPRESA')")
    public SaldoResponse consultarSaldo(@PathVariable String id) {
        var saldo = consultarSaldoUseCase.execute(id);
        return new SaldoResponse(id, saldo.value());
    }

    @PostMapping("/depositar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('VENTANILLA')")
    public void depositar(@Valid @RequestBody MovimientoRequest request) {
        depositarDineroUseCase.execute(request.cuentaId(), request.monto());
    }

    @PostMapping("/retirar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('VENTANILLA')")
    public void retirar(@Valid @RequestBody MovimientoRequest request) {
        retirarDineroUseCase.execute(request.cuentaId(), request.monto());
    }

    @PostMapping("/transferir")
    @PreAuthorize("hasAnyRole('EMPLEADO_EMPRESA','CLIENTE_NATURAL','CLIENTE_EMPRESA')")
    public TransaccionResponse transferir(@Valid @RequestBody TransferenciaRequest request) {
        var transaccion = transferirDineroUseCase.execute(
                request.cuentaOrigenId(),
                request.cuentaDestinoId(),
                request.monto(),
                request.operacionEmpresarial()
        );

        return new TransaccionResponse(
                transaccion.getId(),
                transaccion.getTipoTransaccion(),
                transaccion.getMonto().value(),
                transaccion.getFecha(),
                transaccion.getCuentaOrigen(),
                transaccion.getCuentaDestino(),
                transaccion.getEstado()
        );
    }

    @PostMapping("/transferencias/{id}/aprobar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SUPERVISOR_EMPRESA')")
    public void aprobarTransferencia(@PathVariable String id) {
        aprobarTransferenciaUseCase.aprobar(id);
    }

    @PostMapping("/transferencias/{id}/rechazar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SUPERVISOR_EMPRESA')")
    public void rechazarTransferencia(@PathVariable String id) {
        aprobarTransferenciaUseCase.rechazar(id);
    }
}
