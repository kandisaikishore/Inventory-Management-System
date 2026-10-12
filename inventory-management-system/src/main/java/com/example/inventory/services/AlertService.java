package com.example.inventory.services;

import com.example.inventory.dao.ItemDao;
import com.example.inventory.models.Item;

import java.util.List;
import java.sql.SQLException;

public class AlertService {
    private final ItemDao itemDao;

    public AlertService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public List<Item> checkLowStock(int threshold) throws SQLException {
        return itemDao.getItemsBelowThreshold(threshold);
    }

    public void generateLowStockAlerts(int threshold) throws SQLException {
        List<Item> lowStockItems = checkLowStock(threshold);
        for (Item item : lowStockItems) {
            System.out.println("Low stock alert for item: " + item.getName() + " (ID: " + item.getId() + ")");
        }
    }
}