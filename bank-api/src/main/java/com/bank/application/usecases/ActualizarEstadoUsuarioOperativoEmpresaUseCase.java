package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.services.AuthContextService;
import com.bank.application.services.GestionUsuariosOperativosEmpresaService;
import com.bank.application.services.GestionUsuariosOperativosEmpresaService.UsuarioOperativoEmpresa;

@Service
public class ActualizarEstadoUsuarioOperativoEmpresaUseCase {

    private final GestionUsuariosOperativosEmpresaService gestionUsuariosService;
    private final AuthContextService authContextService;

    public ActualizarEstadoUsuarioOperativoEmpresaUseCase(GestionUsuariosOperativosEmpresaService gestionUsuariosService,
                                                          AuthContextService authContextService) {
        this.gestionUsuariosService = gestionUsuariosService;
        this.authContextService = authContextService;
    }

    public UsuarioOperativoEmpresa activar(String username) {
        validarSupervisor();
        return gestionUsuariosService.cambiarEstado(authContextService.currentRelatedClientIdOrThrow(), username, true);
    }

    public UsuarioOperativoEmpresa inactivar(String username) {
        validarSupervisor();
        return gestionUsuariosService.cambiarEstado(authContextService.currentRelatedClientIdOrThrow(), username, false);
    }

    private void validarSupervisor() {
        if (!authContextService.hasRole("SUPERVISOR_EMPRESA")) {
            throw new SecurityException("Solo Supervisor de Empresa puede gestionar usuarios operativos");
        }
    }
}
