package com.example.inventory;

import com.example.inventory.dao.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseTest {
    private Database database;

    @BeforeEach
    void setUp() {
        database = new Database();
        database.connect();
    }

    @AfterEach
    void tearDown() {
        database.disconnect();
    }

    @Test
    void testDatabaseConnection() {
        Connection connection = database.getConnection();
        assertNotNull(connection, "Database connection should not be null");
    }

    @Test
    void testDatabaseInitialization() throws SQLException {
        database.initialize();
        assertNotNull(database.getConnection(), "Database should remain connected after initialization");
    }
}
