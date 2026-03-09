package com.bank.interfaces.controllers;

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
import com.bank.interfaces.dtos.TransferenciaRequest;
import com.bank.interfaces.dtos.TransaccionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cuentas")
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
    public SaldoResponse consultarSaldo(@PathVariable String id) {
        var saldo = consultarSaldoUseCase.execute(id);
        return new SaldoResponse(id, saldo.value());
    }

    @PostMapping("/depositar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void depositar(@Valid @RequestBody MovimientoRequest request) {
        depositarDineroUseCase.execute(request.cuentaId(), request.monto());
    }

    @PostMapping("/retirar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(@Valid @RequestBody MovimientoRequest request) {
        retirarDineroUseCase.execute(request.cuentaId(), request.monto());
    }

    @PostMapping("/transferir")
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
    public void aprobarTransferencia(@PathVariable String id) {
        aprobarTransferenciaUseCase.aprobar(id);
    }

    @PostMapping("/transferencias/{id}/rechazar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rechazarTransferencia(@PathVariable String id) {
        aprobarTransferenciaUseCase.rechazar(id);
    }
}
