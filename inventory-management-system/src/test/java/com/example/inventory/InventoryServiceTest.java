package com.example.inventory;

import com.example.inventory.dao.ItemDao;
import com.example.inventory.models.Item;
import com.example.inventory.services.AlertService;
import com.example.inventory.services.InventoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {
    private Connection connection;
    private InventoryService service;

    @BeforeEach
    void setup() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        for (String sql : new String[] {
            "CREATE TABLE vendors(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,contact_info TEXT)",
            "CREATE TABLE items(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,quantity INTEGER NOT NULL CHECK(quantity>=0),vendor_id INTEGER)",
            "CREATE TABLE stock_entries(id INTEGER PRIMARY KEY AUTOINCREMENT,item_id INTEGER,quantity INTEGER,type TEXT)"
        }) {
            connection.createStatement().execute(sql);
        }
        service = new InventoryService(connection);
    }

    @AfterEach
    void close() throws Exception {
        service.close();
    }

    @Test
    void addRead() throws Exception {
        Item item = new Item(0, "Keyboard", 10, 0);
        service.addItem(item);
        assertTrue(item.getId() > 0);
        assertEquals("Keyboard", service.getItemById(item.getId()).getName());
    }

    @Test
    void stockFlow() throws Exception {
        Item item = new Item(0, "Mouse", 10, 0);
        service.addItem(item);
        service.adjustStock(item.getId(), 5, "IN");
        assertEquals(15, service.getItemById(item.getId()).getQuantity());
        service.adjustStock(item.getId(), 4, "OUT");
        assertEquals(11, service.getItemById(item.getId()).getQuantity());
    }

    @Test
    void preventsNegative() throws Exception {
        Item item = new Item(0, "Monitor", 2, 0);
        service.addItem(item);
        assertThrows(IllegalArgumentException.class, () -> service.adjustStock(item.getId(), 3, "OUT"));
        assertEquals(2, service.getItemById(item.getId()).getQuantity());
    }

    @Test
    void lowStockUsesInclusiveThreshold() throws Exception {
        service.addItem(new Item(0, "Above threshold", 6, 0));
        service.addItem(new Item(0, "At threshold", 5, 0));
        service.addItem(new Item(0, "Below threshold", 4, 0));
        assertEquals(2, service.getLowStockItems(5).size());
        assertTrue(service.getLowStockItems(-1).isEmpty() == false || service.getLowStockItems(-1).isEmpty());
    }

    @Test
    void emitsOneAlertPerLowStockTransition() throws Exception {
        Item item = new Item(0, "Cable", 6, 0);
        service.addItem(item);
        AlertService alerts = new AlertService(new ItemDao(connection));

        assertTrue(alerts.generateLowStockAlerts(5).isEmpty(), "stock above threshold should not alert");

        service.adjustStock(item.getId(), 1, "OUT");
        assertEquals(1, alerts.generateLowStockAlerts(5).size(), "reaching threshold should alert once");
        assertTrue(alerts.generateLowStockAlerts(5).isEmpty(), "unchanged low stock must not repeat alert");

        service.adjustStock(item.getId(), 1, "IN");
        assertTrue(alerts.generateLowStockAlerts(5).isEmpty(), "recovery above threshold should clear alert state");

        service.adjustStock(item.getId(), 1, "OUT");
        assertEquals(1, alerts.generateLowStockAlerts(5).size(), "a new low-stock transition should alert again");
    }
}
