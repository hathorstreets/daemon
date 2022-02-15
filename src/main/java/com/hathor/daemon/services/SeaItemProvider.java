
package com.hathor.daemon.services;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SeaItemProvider {
    private static Map<String, String> nameMap = new HashMap<>();
    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("1_lighthouse.png", "Lighthouse");
        aMap.put("2_yacht.png", "Yacht");
        aMap.put("3_lighthouse_2.png", "Lighthouse 2");
        aMap.put("4_yacht_2.png", "Yacht 2");
        aMap.put("5_boat.png", "Boat");
        aMap.put("6_boat_2.png", "Boat 2");
        aMap.put("7_wooden_house.png", "Wooden House");
        aMap.put("8_luxury_yacht.png", "Luxury Yacht");
        aMap.put("9_fishing_boat.png", "Fishing Boat");
        aMap.put("10_fishing_boat_2.png", "Fishing Boat 2");
        aMap.put("11_crane_boat.png", "Crane Boat");
        aMap.put("12_tanker.png", "Tanker");
        aMap.put("13_tanker_2.png", "Tanker 2");
        aMap.put("14_cargo_ship.png", "Cargo Ship");
        aMap.put("15_cargo_ship_2.png", "Cargo Ship 2");
        aMap.put("16_cargo_ship_3.png", "Cargo Ship 3");
        aMap.put("17_cruise_ship.png", "Cruise Ship");
        aMap.put("18_drilling_marine.png", "Drilling Marine");
        aMap.put("19_whale.png", "Whale");
        aMap.put("20_kids_boat.png", "Kids Boat");
        aMap.put("21_house_on_island.png", "House on Island");
        aMap.put("22_island.png", "Island");
        aMap.put("23_assault_boat.png", "Assault Boat");
        aMap.put("24_icebreaker.png", "Ice Breaker");
        aMap.put("25_oil_rig.png", "Oil Rig");
        aMap.put("26_cruiser.png", "Cruiser");
        aMap.put("27_battleship.png", "Battleship");
        aMap.put("28_submarine.png", "Submarine");
        aMap.put("29_submarine_2.png", "Submarine 2");
        aMap.put("30_aircraft_carrier.png", "Aircraft Carrier");
        nameMap = Collections.unmodifiableMap(aMap);
    }

    public static class Item {
        int weight;
        File file;
        String name;

        public Item(int weight, File file) {
            this.weight = weight;
            this.file = file;
            this.name = nameMap.get(file.getName());
        }

        public File getFile() {
            return file;
        }

        public String getName() {
            return name;
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