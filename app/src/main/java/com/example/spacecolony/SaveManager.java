package com.example.spacecolony;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SaveManager {
    private static final String SAVE_FILE_NAME = "game_save.json";
    private static final String PREFS_NAME = "SpaceColonyPrefs";

    public static boolean saveGame(Context context) {
        try {
            JSONObject root = new JSONObject();

            // Save GameManager state
            GameManager gm = GameManager.getInstance();
            JSONObject gmJson = new JSONObject();
            gmJson.put("credits", gm.getCredits());
            gmJson.put("turn", gm.getTurn());
            gmJson.put("turnsUntilThreat", gm.getTurnsUntilThreat());
            gmJson.put("missionsCompleted", gm.getMissionsCompleted());
            gmJson.put("missionsWon", gm.getMissionsWon());
            gmJson.put("missionsLost", gm.getMissionsLost());
            gmJson.put("totalCreditsEarned", gm.getTotalCreditsEarned());
            gmJson.put("highestThreatDefeated", gm.getHighestThreatDefeated());
            
            JSONArray earningsHistory = new JSONArray(gm.getEarningsHistory());
            gmJson.put("earningsHistory", earningsHistory);

            root.put("gameManager", gmJson);

            // Save Storage state (items and spaceships)
            Storage storage = Storage.getInstance();
            JSONObject storageJson = new JSONObject();
            
            JSONArray shipsArray = new JSONArray();
            for (Spaceship ship : storage.getSpaceships()) {
                shipsArray.put(ship.getName());
            }
            storageJson.put("spaceships", shipsArray);

            // Items
            JSONObject itemsJson = new JSONObject();
            Map<String, Integer> items = storage.getItems();
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                itemsJson.put(entry.getKey(), entry.getValue());
            }
            storageJson.put("items", itemsJson);
            
            root.put("storage", storageJson);

            // Save Crew
            JSONArray crewArray = new JSONArray();
            int maxId = -1;
            for (CrewMember cm : gm.getAllCrew()) {
                JSONObject cmJson = new JSONObject();
                cmJson.put("type", cm.getClass().getSimpleName());
                cmJson.put("name", cm.name);
                cmJson.put("attack", cm.attack);
                cmJson.put("defense", cm.defense);
                cmJson.put("energy", cm.energy);
                cmJson.put("maxEnergy", cm.maxEnergy);
                cmJson.put("hp", cm.healthPoints);
                cmJson.put("maxHP", cm.maxHP);
                cmJson.put("xp", cm.experience);
                cmJson.put("alive", cm.alive);
                cmJson.put("id", cm.id);
                
                if (cm.id > maxId) maxId = cm.id;
                
                if (cm instanceof Miner) {
                    cmJson.put("isMining", ((Miner) cm).isMining());
                    cmJson.put("miningAsteroid", ((Miner) cm).isMiningAsteroid());
                } else if (cm instanceof Medic) {
                    cmJson.put("actingAsStaff", ((Medic) cm).isActingAsStaff());
                }
                
                crewArray.put(cmJson);
            }
            root.put("crew", crewArray);
            root.put("maxCrewId", maxId);
            
            // Queues
            JSONArray hospitalQueue = new JSONArray();
            for (CrewMember cm : gm.getHospitalQueue()) hospitalQueue.put(cm.getId());
            root.put("hospitalQueue", hospitalQueue);

            JSONArray simulatorQueue = new JSONArray();
            for (CrewMember cm : gm.getSimulatorQueue()) simulatorQueue.put(cm.getId());
            root.put("simulatorQueue", simulatorQueue);

            // Buildings
            root.put("hospitalCapacity", Hospital.getInstance().getCapacity());
            root.put("hospitalGain", Hospital.getInstance().gain);
            root.put("simulatorCapacity", Simulator.getInstance().getCapacity());
            root.put("simulatorGain", Simulator.getInstance().gain);

            // Write to file
            FileOutputStream fos = context.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(root.toString().getBytes());
            fos.close();

            // Update last save time
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putLong("last_save_time", System.currentTimeMillis()).apply();

            return true;
        } catch (Exception e) {
            Log.e("SaveManager", "Error saving game", e);
            return false;
        }
    }

    public static boolean loadGame(Context context) {
        try {
            File file = new File(context.getFilesDir(), SAVE_FILE_NAME);
            if (!file.exists()) return false;

            FileInputStream fis = context.openFileInput(SAVE_FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            fis.close();

            JSONObject root = new JSONObject(sb.toString());

            // Reset singletons
            GameManager.getInstance().reset();
            Storage.reset();
            Hospital.reset();
            Simulator.reset();
            Quarters.reset();

            GameManager gm = GameManager.getInstance();
            Storage storage = Storage.getInstance();

            // Load Crew first
            JSONArray crewArray = root.getJSONArray("crew");
            ArrayList<CrewMember> allCrew = gm.getAllCrew();
            for (int i = 0; i < crewArray.length(); i++) {
                JSONObject cmJson = crewArray.getJSONObject(i);
                String type = cmJson.getString("type");
                String name = cmJson.getString("name");
                
                CrewMember cm;
                switch (type) {
                    case "Miner":
                        cm = new Miner(name);
                        ((Miner) cm).startMining(cmJson.getBoolean("miningAsteroid"));
                        if (!cmJson.getBoolean("isMining")) ((Miner) cm).stopMining();
                        break;
                    case "Medic":
                        cm = new Medic(name);
                        ((Medic) cm).setActingAsStaff(cmJson.getBoolean("actingAsStaff"));
                        break;
                    case "Soldier":
                        cm = new Soldier(name);
                        break;
                    case "Builder":
                        cm = new Builder(name);
                        break;
                    case "SpecialSoldier":
                        cm = new SpecialSoldier(name);
                        break;
                    default:
                        continue;
                }
                
                cm.attack = cmJson.getInt("attack");
                cm.defense = cmJson.getInt("defense");
                cm.energy = cmJson.getInt("energy");
                cm.maxEnergy = cmJson.getInt("maxEnergy");
                cm.healthPoints = cmJson.getInt("hp");
                cm.maxHP = cmJson.getInt("maxHP");
                cm.experience = cmJson.getInt("xp");
                cm.alive = cmJson.getBoolean("alive");
                cm.id = cmJson.getInt("id");
                
                allCrew.add(cm);
                storage.addLoadedCrewMember(cm);
            }
            
            // Restore ID counter
            CrewMember.setIdCounter(root.optInt("maxCrewId", CrewMember.getNumberOfCreated()) + 1);

            // Load GM
            gm.setLoadedState(root.getJSONObject("gameManager"));

            // Load Storage
            JSONObject storageJson = root.getJSONObject("storage");
            JSONArray shipsArray = storageJson.getJSONArray("spaceships");
            for (int i = 0; i < shipsArray.length(); i++) {
                storage.getSpaceships().add(new Spaceship(shipsArray.getString(i), 300));
            }
            
            JSONObject itemsJson = storageJson.getJSONObject("items");
            Iterator<String> keys = itemsJson.keys();
            while (keys.hasNext()) {
                String itemName = keys.next();
                int count = itemsJson.getInt(itemName);
                for (int i = 0; i < count; i++) storage.addItem(itemName);
            }

            // Queues
            JSONArray hQueue = root.getJSONArray("hospitalQueue");
            for (int i = 0; i < hQueue.length(); i++) {
                int id = hQueue.getInt(i);
                for (CrewMember cm : allCrew) if (cm.getId() == id) gm.getHospitalQueue().add(cm);
            }

            JSONArray sQueue = root.getJSONArray("simulatorQueue");
            for (int i = 0; i < sQueue.length(); i++) {
                int id = sQueue.getInt(i);
                for (CrewMember cm : allCrew) if (cm.getId() == id) gm.getSimulatorQueue().add(cm);
            }
            
            // Buildings
            Hospital hospital = Hospital.getInstance();
            int hTargetCap = root.getInt("hospitalCapacity");
            while (hospital.getCapacity() < hTargetCap) hospital.increaseCapacity();
            int hTargetGain = root.getInt("hospitalGain");
            while (hospital.gain < hTargetGain) hospital.increaseGain();

            Simulator simulator = Simulator.getInstance();
            int sTargetCap = root.getInt("simulatorCapacity");
            while (simulator.getCapacity() < sTargetCap) simulator.increaseCapacity();
            int sTargetGain = root.getInt("simulatorGain");
            while (simulator.gain < sTargetGain) simulator.increaseGain();

            return true;
        } catch (Exception e) {
            Log.e("SaveManager", "Error loading game", e);
            return false;
        }
    }
}
