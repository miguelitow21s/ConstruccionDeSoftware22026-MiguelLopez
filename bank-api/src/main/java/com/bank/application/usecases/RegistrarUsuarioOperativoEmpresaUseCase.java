package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.services.AuthContextService;
import com.bank.application.services.GestionUsuariosOperativosEmpresaService;
import com.bank.application.services.GestionUsuariosOperativosEmpresaService.UsuarioOperativoEmpresa;

@Service
public class RegistrarUsuarioOperativoEmpresaUseCase {

    private final GestionUsuariosOperativosEmpresaService gestionUsuariosService;
    private final AuthContextService authContextService;

    public RegistrarUsuarioOperativoEmpresaUseCase(GestionUsuariosOperativosEmpresaService gestionUsuariosService,
                                                   AuthContextService authContextService) {
        this.gestionUsuariosService = gestionUsuariosService;
        this.authContextService = authContextService;
    }

    public UsuarioOperativoEmpresa execute(String username, String nombreCompleto, String email) {
        validarSupervisor();
        return gestionUsuariosService.registrar(
                authContextService.currentRelatedClientIdOrThrow(),
                username,
                nombreCompleto,
                email
        );
    }

    private void validarSupervisor() {
        if (!authContextService.hasRole("SUPERVISOR_EMPRESA")) {
            throw new SecurityException("Solo Supervisor de Empresa puede gestionar usuarios operativos");
        }
    }
}
