package com.bank.interfaces.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.ListAuditLogUseCase;
import com.bank.interfaces.dtos.AuditLogResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auditLog")
@Tag(name = "Audit Log", description = "Audit records and traceability")
@SecurityRequirement(name = "basicAuth")
public class AuditLogController {

    private final ListAuditLogUseCase listAuditLogUseCase;

    public AuditLogController(ListAuditLogUseCase listAuditLogUseCase) {
        this.listAuditLogUseCase = listAuditLogUseCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List audit log entries",
               description = "Lists audit records. Analysts can filter by user; " +
                           "other roles can only view their own operations.")
    public List<AuditLogResponse> list(@RequestParam(required = false) String userId,
                                         @RequestParam(required = false) String affectedProductId) {
        return listAuditLogUseCase.execute(userId, affectedProductId).stream()
                .map(entry -> new AuditLogResponse(
                        entry.idAuditLog(),
                entry.typeOperacion(),
                        entry.operationDateTime(),
                        entry.userId(),
                        entry.userRole(),
                entry.idProductoAfectado(),
                entry.datosDetalle()
                ))
                .toList();
    }
}
