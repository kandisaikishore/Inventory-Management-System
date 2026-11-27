package com.example.inventory.services;

import com.example.inventory.dao.ItemDao;
import com.example.inventory.models.Item;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlertService {
    private final ItemDao itemDao;
    private final Set<Integer> alertedItemIds = new HashSet<>();

    public AlertService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public List<Item> checkLowStock(int threshold) throws SQLException {
        return itemDao.getItemsBelowThreshold(threshold);
    }

    /**
     * Emits one alert when an item enters the low-stock state. An item can
     * trigger another alert only after it has recovered above the threshold
     * and subsequently becomes low-stock again.
     *
     * @return the items that triggered a new alert during this check
     */
    public synchronized List<Item> generateLowStockAlerts(int threshold) throws SQLException {
        List<Item> lowStockItems = checkLowStock(threshold);
        Set<Integer> currentlyLow = new HashSet<>();
        List<Item> newAlerts = new ArrayList<>();

        for (Item item : lowStockItems) {
            currentlyLow.add(item.getId());
            if (alertedItemIds.add(item.getId())) {
                newAlerts.add(item);
                System.out.println("Low stock alert for item: " + item.getName()
                    + " (ID: " + item.getId() + ")");
            }
        }

        alertedItemIds.retainAll(currentlyLow);
        return newAlerts;
    }
}
