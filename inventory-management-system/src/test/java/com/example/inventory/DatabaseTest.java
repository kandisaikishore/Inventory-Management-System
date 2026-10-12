package com.example.inventory;

import com.example.inventory.dao.Database;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseTest {
    @Test
    void databaseConnectionCanBeOpened() throws SQLException {
        try (Connection connection = Database.connect()) {
            assertNotNull(connection, "Database connection should not be null");
            assertNotNull(connection.getMetaData(), "Database metadata should be available");
        }
    }

    @Test
    void databaseInitializationIsIdempotent() throws SQLException {
        Database.initializeDatabase();
        Database.initializeDatabase();
        try (Connection connection = Database.connect()) {
            assertNotNull(connection, "Connection should work after initialization");
        }
    }
}
