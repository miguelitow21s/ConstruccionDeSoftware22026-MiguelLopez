package com.bank.domain.services;

import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.entities.TipoTransaccion;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.valueobjects.Dinero;

public class ServicioTransferencia {

    public Transaccion transferir(Cuenta origen, Cuenta destino, Dinero monto, boolean requiereAprobacion, Long idUsuarioCreador) {
        if (origen.getNumeroCuenta().equals(destino.getNumeroCuenta())) {
            throw new IllegalArgumentException("La cuenta origen y destino no pueden ser iguales");
        }

        EstadoTransaccion estadoInicial = requiereAprobacion
                ? EstadoTransaccion.EN_ESPERA_APROBACION
                : EstadoTransaccion.EJECUTADA;

        if (!requiereAprobacion) {
            origen.retirar(monto);
            destino.depositar(monto);
        }

        return new Transaccion(
                TipoTransaccion.TRANSFERENCIA,
                monto,
                origen.getNumeroCuenta().value(),
                destino.getNumeroCuenta().value(),
                estadoInicial,
                idUsuarioCreador
        );
    }

    public void ejecutarTransferenciaPendiente(Transaccion transaccion, Cuenta origen, Cuenta destino, Long idUsuarioAprobador) {
        if (transaccion.getEstado() != EstadoTransaccion.EN_ESPERA_APROBACION) {
            throw new IllegalStateException("Solo se pueden ejecutar transferencias en espera de aprobacion");
        }
        origen.retirar(transaccion.getMonto());
        destino.depositar(transaccion.getMonto());
        transaccion.aprobarYEjecutar(idUsuarioAprobador);
    }
}
