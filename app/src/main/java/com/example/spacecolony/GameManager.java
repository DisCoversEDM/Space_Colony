package com.example.spacecolony;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    public interface OnCreditsChangedListener {
        void onCreditsChanged(int totalCreditsEarned);
    }

    private static GameManager instance;
    private ArrayList<CrewMember> allCrew;
    private ArrayList<CrewMember> hospitalQueue;
    private ArrayList<CrewMember> simulatorQueue;
    private int credits;
    private int turn;
    private int turnsUntilThreat;
    private boolean threatDefeated;
    private boolean threatBattled;
    private boolean gameOver;

    // Statistics
    private int missionsCompleted = 0;
    private int missionsWon = 0;
    private int missionsLost = 0;
    private int totalCreditsEarned = 0;
    private int highestThreatDefeated = 0;
    private Map<Integer, Integer> crewMissionCount = new HashMap<>();
    private List<Integer> earningsHistory = new ArrayList<>();
    private int currentTurnEarnings = 0;
    
    private OnCreditsChangedListener creditsListener;

    private GameManager() {
        allCrew = new ArrayList<>();
        hospitalQueue = new ArrayList<>();
        simulatorQueue = new ArrayList<>();
        credits = 400; // Starting credits
        totalCreditsEarned = 0;
        turn = 0;
        turnsUntilThreat = 5;
        threatDefeated = false;
        threatBattled = false;
        gameOver = false;
        earningsHistory.add(0); // Initial turn 0
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void reset() {
        instance = new GameManager();
    }

    public ArrayList<CrewMember> getAllCrew() { return allCrew; }
    public int getCredits() { return credits; }
    public int getTurn() { return turn; }
    public int getTurnsUntilThreat() { return turnsUntilThreat; }

    public void setOnCreditsChangedListener(OnCreditsChangedListener listener) {
        this.creditsListener = listener;
    }

    public void addCredits(int amount) { 
        credits += amount; 
        totalCreditsEarned += amount;
        currentTurnEarnings += amount;
        if (creditsListener != null) {
            creditsListener.onCreditsChanged(totalCreditsEarned);
        }
    }
    
    public boolean spendCredits(int amount) {
        if (credits >= amount) {
            credits -= amount;
            return true;
        }
        return false;
    }

    public double getInflationRate() {
        return 1.0 + (turn * 0.05);
    }

    public int getInflatedPrice(int basePrice) {
        return (int) (basePrice * getInflationRate());
    }

    public boolean isThreatActive() {
        return turnsUntilThreat <= 0 && !threatDefeated;
    }

    public void setThreatDefeated(boolean defeated) {
        this.threatDefeated = defeated;
    }

    public boolean isThreatActiveCheck() {
        return turnsUntilThreat <= 0 && !threatDefeated;
    }

    public boolean isThreatBattled() {
        return threatBattled;
    }

    public void setThreatBattled(boolean battled) {
        this.threatBattled = battled;
    }

    public void resetThreatCounter() {
        this.turnsUntilThreat = 5;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    // Stats Methods
    public void recordMissionResult(boolean victory, int threatLevel, List<CrewMember> squad) {
        missionsCompleted++;
        if (victory) {
            missionsWon++;
            if (threatLevel > highestThreatDefeated) {
                highestThreatDefeated = threatLevel;
            }
        } else {
            missionsLost++;
            if (missionsLost >= 3) {
                gameOver = true;
            }
        }

        for (CrewMember cm : squad) {
            int count = crewMissionCount.getOrDefault(cm.getId(), 0);
            crewMissionCount.put(cm.getId(), count + 1);
        }
    }

    public int getMissionsCompleted() { return missionsCompleted; }
    public int getMissionsWon() { return missionsWon; }
    public int getMissionsLost() { return missionsLost; }
    public int getTotalCreditsEarned() { return totalCreditsEarned; }
    public int getHighestThreatDefeated() { return highestThreatDefeated; }
    public List<Integer> getEarningsHistory() { return earningsHistory; }
    
    public CrewMember getMostUsedCrew() {
        int max = -1;
        CrewMember topCrew = null;
        for (CrewMember cm : allCrew) {
            int count = crewMissionCount.getOrDefault(cm.getId(), 0);
            if (count > max) {
                max = count;
                topCrew = cm;
            }
        }
        return topCrew;
    }

    public int getMissionCountForCrew(CrewMember cm) {
        return crewMissionCount.getOrDefault(cm.getId(), 0);
    }

    public boolean hasLivingCrew() {
        for (CrewMember cm : allCrew) {
            if (cm.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCrewMemberBusy(CrewMember crew) {
        if (!crew.isAlive()) {
            return true;
        }
        if (crew instanceof Miner && ((Miner) crew).isMining()) {
            return true;
        }
        if (crew instanceof Builder) {
            if (Hospital.getInstance().getAssignedBuilder() == crew || 
                Simulator.getInstance().getAssignedBuilder() == crew) {
                return true;
            }
        }
        return false;
    }

    public void handleCrewDeath(CrewMember deceased) {
        simulatorQueue.remove(deceased);
        if (hospitalQueue.contains(deceased)) {
            boolean wasStaff = (deceased instanceof Medic && ((Medic) deceased).isActingAsStaff());
            hospitalQueue.remove(deceased);
            if (wasStaff) {
                for (CrewMember cm : hospitalQueue) {
                    boolean isPatient = !(cm instanceof Medic) || !((Medic) cm).isActingAsStaff();
                    if (isPatient) {
                        hospitalQueue.remove(cm);
                        break;
                    }
                }
            } else {
                for (CrewMember cm : hospitalQueue) {
                    boolean isStaff = (cm instanceof Medic && ((Medic) cm).isActingAsStaff());
                    if (isStaff) {
                        hospitalQueue.remove(cm);
                        break;
                    }
                }
            }
        }
        if (Hospital.getInstance().getAssignedBuilder() == deceased) {
            Hospital.getInstance().setUpgradePending(false, null);
        }
        if (Simulator.getInstance().getAssignedBuilder() == deceased) {
            Simulator.getInstance().setUpgradePending(false, null);
        }
        if (deceased instanceof Miner) {
            ((Miner) deceased).stopMining();
        }
    }

    public void sendToHospital(CrewMember crew) {
        sendToHospital(crew, true);
    }

    public void sendToHospital(CrewMember crew, boolean asStaff) {
        if (hospitalQueue.contains(crew)) {
            hospitalQueue.remove(crew);
            return;
        }
        if (isCrewMemberBusy(crew)) {
            return;
        }
        int patientsCount = 0;
        int staffCount = 0;
        for (CrewMember cm : hospitalQueue) {
            if (cm instanceof Medic && ((Medic) cm).isActingAsStaff()) {
                staffCount++;
            } else {
                patientsCount++;
            }
        }
        if (crew instanceof Medic && asStaff) {
            if (staffCount < Hospital.getInstance().getCapacity() / 2) {
                ((Medic) crew).setActingAsStaff(true);
                simulatorQueue.remove(crew);
                hospitalQueue.add(crew);
            }
        } else {
            if (patientsCount < Hospital.getInstance().getCapacity() / 2 && patientsCount < staffCount) {
                if (crew instanceof Medic) {
                    ((Medic) crew).setActingAsStaff(false);
                }
                simulatorQueue.remove(crew);
                hospitalQueue.add(crew);
            }
        }
    }

    public void sendToSimulator(CrewMember crew) {
        if (simulatorQueue.contains(crew)) {
            simulatorQueue.remove(crew);
            return;
        }
        if (isCrewMemberBusy(crew)) {
            return;
        }
        if (simulatorQueue.size() < Simulator.getInstance().getCapacity()) {
            hospitalQueue.remove(crew);
            simulatorQueue.add(crew);
        }
    }
    
    public void removeFromBuildings(CrewMember crew) {
        hospitalQueue.remove(crew);
        simulatorQueue.remove(crew);
    }

    public ArrayList<CrewMember> getHospitalQueue() { return hospitalQueue; }
    public ArrayList<CrewMember> getSimulatorQueue() { return simulatorQueue; }

    public void nextTurn() {
        if (gameOver) return;

        // Handle automatic defeat if threat is active but no crew remains
        if (isThreatActive() && !threatBattled && !hasLivingCrew()) {
            int currentThreatLevel = (turn / 6) + 1;
            recordMissionResult(false, currentThreatLevel, new ArrayList<>());
            resetThreatCounter();
        }

        earningsHistory.add(currentTurnEarnings);
        currentTurnEarnings = 0;
        
        turn++;
        if (turnsUntilThreat > 0) {
            turnsUntilThreat--;
        }

        threatDefeated = false;
        threatBattled = false;
        
        Hospital.getInstance().completeUpgrade();
        Simulator.getInstance().completeUpgrade();
        
        Hospital hospital = Hospital.getInstance();
        hospital.heal(hospitalQueue);
        
        Simulator simulator = Simulator.getInstance();
        simulator.train(simulatorQueue);

        ArrayList<Spaceship> availableSpaceships = new ArrayList<>(Storage.getInstance().getSpaceships());
        for (CrewMember cm : allCrew) {
            if (cm instanceof Miner) {
                Miner miner = (Miner) cm;
                if (miner.isMining()) {
                    Spaceship assignedShip = null;
                    if (miner.isMiningAsteroid() && !availableSpaceships.isEmpty()) {
                        assignedShip = availableSpaceships.remove(0);
                    }
                    int earnings = miner.completeMining(assignedShip);
                    addCredits(earnings);
                }
            }
        }
        
        ArrayList<CrewMember> quartersQueue = new ArrayList<>(allCrew);
        quartersQueue.removeAll(hospitalQueue);
        quartersQueue.removeAll(simulatorQueue);
        
        quartersQueue.removeIf(cm -> !cm.isAlive());
        hospitalQueue.removeIf(cm -> !cm.isAlive());
        simulatorQueue.removeIf(cm -> !cm.isAlive());

        for (CrewMember cm : allCrew) {
            if (cm instanceof Miner && ((Miner) cm).isMining()) {
                quartersQueue.remove(cm);
            }
        }

        if (Hospital.getInstance().getAssignedBuilder() != null) {
            quartersQueue.remove(Hospital.getInstance().getAssignedBuilder());
        }
        if (Simulator.getInstance().getAssignedBuilder() != null) {
            quartersQueue.remove(Simulator.getInstance().getAssignedBuilder());
        }

        Quarters.getInstance().rest(quartersQueue);
    }

    public void setLoadedState(JSONObject json) {
        try {
            this.credits = json.getInt("credits");
            this.turn = json.getInt("turn");
            this.turnsUntilThreat = json.getInt("turnsUntilThreat");
            this.missionsCompleted = json.getInt("missionsCompleted");
            this.missionsWon = json.getInt("missionsWon");
            this.missionsLost = json.getInt("missionsLost");
            this.totalCreditsEarned = json.getInt("totalCreditsEarned");
            this.highestThreatDefeated = json.getInt("highestThreatDefeated");
            
            this.earningsHistory.clear();
            JSONArray history = json.getJSONArray("earningsHistory");
            for (int i = 0; i < history.length(); i++) {
                this.earningsHistory.add(history.getInt(i));
            }
            
            this.gameOver = this.missionsLost >= 3;
            if (creditsListener != null) {
                creditsListener.onCreditsChanged(totalCreditsEarned);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
