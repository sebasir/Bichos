package net.hpclab.services;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.hpclab.entities.AuditEnum;
import net.hpclab.entities.AuditLog;
import net.hpclab.entities.Modules;
import net.hpclab.entities.Users;

public class AuditService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AuditService.class.getSimpleName());
    private static AuditService auditService;
    private DataBaseService<AuditLog> auditDBService;
    private AuditLog auditLog;

    static {
        try {
            auditService = new AuditService();
            auditService.auditDBService = new DataBaseService<>(AuditLog.class);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "El servicio de log no ha podido iniciar correctamente: {0}.", e.getMessage());
        }
    }

    public void log(Users idUser, Modules idModule, String aulogIpAddress, AuditEnum aulogAction, String aulogTarget) {
        try {
            auditLog = new AuditLog();
            auditLog.setIdUser(idUser);
            auditLog.setIdModule(idModule);
            auditLog.setAulogTime(new Date());
            auditLog.setAulogIpAddress(aulogIpAddress);
            auditLog.setAulogAction(aulogAction.get());
            auditLog.setAulogTarget(aulogTarget);
            auditDBService.persist(auditLog);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error realizando inserción en AuditLog: {0}", e.getMessage());
        }
    }

    public static AuditService getInstance() {
        return auditService;
    }
}