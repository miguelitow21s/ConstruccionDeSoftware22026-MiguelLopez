package com.bank.application.usecases;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bank.application.services.AuthContextService;
import com.bank.application.services.GestionUsuariosOperativosEmpresaService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestionUsuariosOperativosEmpresaUseCasesTest {

    @Test
    void supervisorPuedeRegistrarListarEInactivarUsuarioOperativoDeSuEmpresa() {
        GestionUsuariosOperativosEmpresaService gestionService = new GestionUsuariosOperativosEmpresaService();
        AuthContextService authContextService = new AuthContextService("supervisor:empresa-1");

        RegistrarUsuarioOperativoEmpresaUseCase registrarUseCase =
                new RegistrarUsuarioOperativoEmpresaUseCase(gestionService, authContextService);
        ListarUsuariosOperativosEmpresaUseCase listarUseCase =
                new ListarUsuariosOperativosEmpresaUseCase(gestionService, authContextService);
        ActualizarEstadoUsuarioOperativoEmpresaUseCase actualizarUseCase =
                new ActualizarEstadoUsuarioOperativoEmpresaUseCase(gestionService, authContextService);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("supervisor", "123456", "ROLE_SUPERVISOR_EMPRESA")
        );

        var creado = registrarUseCase.execute("operativo_1", "Operativo Uno", "operativo1@empresa.com");
        assertTrue(creado.activo());

        List<GestionUsuariosOperativosEmpresaService.UsuarioOperativoEmpresa> usuarios = listarUseCase.execute();
        assertEquals(1, usuarios.size());
        assertEquals("operativo_1", usuarios.getFirst().username());

        var inactivado = actualizarUseCase.inactivar("operativo_1");
        assertFalse(inactivado.activo());

        var activado = actualizarUseCase.activar("operativo_1");
        assertTrue(activado.activo());
    }

    @Test
    void noSupervisorNoPuedeGestionarUsuariosOperativos() {
        GestionUsuariosOperativosEmpresaService gestionService = new GestionUsuariosOperativosEmpresaService();
        AuthContextService authContextService = new AuthContextService("empleado_empresa:empresa-1");

        RegistrarUsuarioOperativoEmpresaUseCase registrarUseCase =
                new RegistrarUsuarioOperativoEmpresaUseCase(gestionService, authContextService);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("empleado_empresa", "123456", "ROLE_EMPLEADO_EMPRESA")
        );

        SecurityException thrown = assertThrows(
                SecurityException.class,
                () -> registrarUseCase.execute("operativo_2", "Operativo Dos", "operativo2@empresa.com")
        );
        assertEquals("Solo Supervisor de Empresa puede gestionar usuarios operativos", thrown.getMessage());
    }
}

