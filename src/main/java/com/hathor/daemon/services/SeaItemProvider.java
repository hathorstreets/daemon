
package com.hathor.daemon.services;

import java.io.File;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SeaItemProvider {
    public static class Item {
        int weight;
        File file;

        public Item(int weight, File file) {
            this.weight = weight;
            this.file = file;
        }

        public File getFile() {
            return file;
        }
    }

    public Item chooseOnWeight(List<Item> items) {
        int completeWeight = 0;
        for (Item item : items)
            completeWeight += item.weight;
        double r = Math.random() * completeWeight;
        int countWeight = 0;
        for (Item item : items) {
            countWeight += item.weight;
            if (countWeight >= r)
                return item;
        }
        throw new RuntimeException("Should never be shown.");
    }

}