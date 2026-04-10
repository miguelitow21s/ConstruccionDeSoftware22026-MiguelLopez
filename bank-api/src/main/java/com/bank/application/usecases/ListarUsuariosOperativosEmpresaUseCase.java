package com.bank.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.services.AuthContextService;
import com.bank.application.services.GestionUsuariosOperativosEmpresaService;
import com.bank.application.services.GestionUsuariosOperativosEmpresaService.UsuarioOperativoEmpresa;

@Service
public class ListarUsuariosOperativosEmpresaUseCase {

    private final GestionUsuariosOperativosEmpresaService gestionUsuariosService;
    private final AuthContextService authContextService;

    public ListarUsuariosOperativosEmpresaUseCase(GestionUsuariosOperativosEmpresaService gestionUsuariosService,
                                                  AuthContextService authContextService) {
        this.gestionUsuariosService = gestionUsuariosService;
        this.authContextService = authContextService;
    }

    public List<UsuarioOperativoEmpresa> execute() {
        validarSupervisor();
        return gestionUsuariosService.listar(authContextService.currentRelatedClientIdOrThrow());
    }

    private void validarSupervisor() {
        if (!authContextService.hasRole("SUPERVISOR_EMPRESA")) {
            throw new SecurityException("Solo Supervisor de Empresa puede gestionar usuarios operativos");
        }
    }
}
