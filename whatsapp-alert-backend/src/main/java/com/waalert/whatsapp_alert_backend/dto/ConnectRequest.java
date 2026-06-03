package com.waalert.whatsapp_alert_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for the Connection Screen.
 * Supports MySQL and SQL Server database types.
 */
@Data
public class ConnectRequest {

    /** Database type: MYSQL or SQLSERVER */
    private String databaseType = "MYSQL";

    @NotBlank(message = "Server address is required")
    private String server;

    /** Default depends on databaseType: 3306 for MySQL, 1433 for SQL Server */
    private String port = "3306";

    @NotBlank(message = "Database name is required")
    private String database;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}