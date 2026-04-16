package com.example.spacecolony;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class BattleActivity extends AppCompatActivity {

    private TextView tvAlienName, tvAlienHP, tvAlienThreatLevel, tvAlienCrit;
    private ProgressBar progressAlienHp;
    private LinearLayout layoutSquadRow;
    private Button btnAttack, btnRest, btnPotion, btnFirstAid;
    private View viewCritFlash;

    private Threat currentThreat;
    private List<CrewMember> squad = new ArrayList<>();
    private int currentTurnIndex = 0;
    private int threatLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        initViews();
        setupBattle();
        updateUI();
    }

    private void initViews() {
        tvAlienName = findViewById(R.id.tv_alien_name);
        tvAlienHP = findViewById(R.id.tv_alien_hp);
        tvAlienThreatLevel = findViewById(R.id.tv_alien_threat_level);
        tvAlienCrit = findViewById(R.id.tv_alien_crit);
        progressAlienHp = findViewById(R.id.progress_alien_hp);
        layoutSquadRow = findViewById(R.id.layout_squad_row);
        btnAttack = findViewById(R.id.btn_action_attack);
        btnRest = findViewById(R.id.btn_action_rest);
        btnPotion = findViewById(R.id.btn_action_potion);
        btnFirstAid = findViewById(R.id.btn_action_first_aid);
        viewCritFlash = findViewById(R.id.view_crit_flash);

        btnAttack.setOnClickListener(v -> performAttack());
        btnRest.setOnClickListener(v -> performRest());
        btnPotion.setOnClickListener(v -> performUsePotion());
        btnFirstAid.setOnClickListener(v -> performUseFirstAid());
    }

    private void setupBattle() {
        ArrayList<Integer> squadIds = getIntent().getIntegerArrayListExtra("squad_ids");
        if (squadIds != null) {
            for (int id : squadIds) {
                for (CrewMember cm : GameManager.getInstance().getAllCrew()) {
                    if (cm.getId() == id) {
                        squad.add(cm);
                        break;
                    }
                }
            }
        }

        int turn = GameManager.getInstance().getTurn();
        threatLevel = (turn / 6) + 1;
        // Threat(int attack, int defense, double critChance, int critAttackBoost, int maxHP)
        currentThreat = new Threat(threatLevel * 10, threatLevel * 2, 0.1, 15, threatLevel * 50);

        tvAlienThreatLevel.setText("Threat " + threatLevel);
        tvAlienCrit.setText("Crit chance: " + (int)(0.1 * 100) + "%");
        progressAlienHp.setMax(threatLevel * 50);
        
        // Ensure we start with a living member
        if (!squad.isEmpty() && !squad.get(0).isAlive()) {
            advanceToNextTurn();
        }
    }

    private void updateUI() {
        tvAlienHP.setText(currentThreat.getHealthPoints() + " / " + progressAlienHp.getMax());
        progressAlienHp.setProgress(currentThreat.getHealthPoints());

        layoutSquadRow.removeAllViews();
        for (int i = 0; i < squad.size(); i++) {
            View card = getLayoutInflater().inflate(R.layout.item_crew_battle_card, layoutSquadRow, false);
            updateCrewCard(card, squad.get(i), i == currentTurnIndex);
            layoutSquadRow.addView(card);
        }

        if (currentTurnIndex >= 0 && currentTurnIndex < squad.size()) {
            CrewMember activeMember = squad.get(currentTurnIndex);
            btnAttack.setEnabled(activeMember.isAlive() && activeMember.energy > 0);
            btnRest.setEnabled(activeMember.isAlive());
            
            int potionCount = Storage.getInstance().getItemCount("EnergyRestore");
            btnPotion.setEnabled(activeMember.isAlive() && potionCount > 0);
            btnPotion.setText("Use Potion (" + potionCount + ")");

            int firstAidCount = Storage.getInstance().getItemCount("FirstAidKit");
            btnFirstAid.setEnabled(activeMember.isAlive() && firstAidCount > 0);
            btnFirstAid.setText("Use First Aid (" + firstAidCount + ")");
        } else {
            btnAttack.setEnabled(false);
            btnRest.setEnabled(false);
            btnPotion.setEnabled(false);
            btnFirstAid.setEnabled(false);
        }
    }

    private void updateCrewCard(View card, CrewMember cm, boolean isActive) {
        TextView tvName = card.findViewById(R.id.tv_battle_crew_name);
        ProgressBar progressHp = card.findViewById(R.id.progress_battle_hp);
        ProgressBar progressEnergy = card.findViewById(R.id.progress_battle_energy);
        TextView tvStatus = card.findViewById(R.id.tv_battle_status_badge);

        tvName.setText(cm.name);
        progressHp.setMax(cm.maxHP);
        progressHp.setProgress(cm.healthPoints);
        progressEnergy.setMax(cm.maxEnergy);
        progressEnergy.setProgress(cm.energy);

        if (!cm.isAlive()) {
            tvStatus.setText("KO");
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            card.setAlpha(0.5f);
            card.setBackgroundResource(R.drawable.bg_card);
        } else if (isActive) {
            tvStatus.setText("ACTIVE");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.teal_300));
            card.setBackgroundResource(R.drawable.bg_card_selected);
            card.setAlpha(1.0f);
        } else {
            tvStatus.setText("WAITING");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.gray_400));
            card.setBackgroundResource(R.drawable.bg_card);
            card.setAlpha(1.0f);
        }
    }

    private void performAttack() {
        if (currentTurnIndex < 0 || currentTurnIndex >= squad.size()) return;
        CrewMember activeMember = squad.get(currentTurnIndex);
        activeMember.attack(currentThreat);
        checkBattleStatus();
    }

    private void performRest() {
        if (currentTurnIndex < 0 || currentTurnIndex >= squad.size()) return;
        CrewMember activeMember = squad.get(currentTurnIndex);
        activeMember.changeEnergy(2);
        checkBattleStatus();
    }

    private void performUsePotion() {
        if (currentTurnIndex < 0 || currentTurnIndex >= squad.size()) return;
        CrewMember activeMember = squad.get(currentTurnIndex);
        if (Storage.getInstance().getItemCount("EnergyRestore") > 0) {
            EnergyRestore potion = new EnergyRestore();
            potion.used(activeMember);
            Storage.getInstance().removeItem(potion);
            updateUI();
        }
    }

    private void performUseFirstAid() {
        if (currentTurnIndex < 0 || currentTurnIndex >= squad.size()) return;
        CrewMember activeMember = squad.get(currentTurnIndex);
        if (Storage.getInstance().getItemCount("FirstAidKit") > 0) {
            FirstAidKit kit = new FirstAidKit();
            kit.used(activeMember);
            Storage.getInstance().removeItem(kit);
            updateUI();
        }
    }

    private void checkBattleStatus() {
        if (!currentThreat.isAlive()) {
            GameManager.getInstance().setThreatDefeated(true);
            GameManager.getInstance().setThreatBattled(true);
            GameManager.getInstance().resetThreatCounter();
            GameManager.getInstance().recordMissionResult(true, threatLevel, squad);
            Toast.makeText(this, "Victory!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        advanceToNextTurn();
    }

    private void advanceToNextTurn() {
        currentTurnIndex++;
        
        // Skip dead members
        while (currentTurnIndex < squad.size() && !squad.get(currentTurnIndex).isAlive()) {
            currentTurnIndex++;
        }

        if (currentTurnIndex >= squad.size()) {
            enemyTurn();
        } else {
            updateUI();
        }
    }

    private void enemyTurn() {
        // Enemy attacks all members it finds
        for (CrewMember cm : squad) {
            if (cm.isAlive()) {
                currentThreat.attack(cm);
            }
        }

        boolean anyoneAlive = false;
        for (CrewMember cm : squad) {
            if (cm.isAlive()) {
                anyoneAlive = true;
                break;
            }
        }

        if (!anyoneAlive) {
            GameManager.getInstance().setThreatBattled(true);
            GameManager.getInstance().resetThreatCounter();
            GameManager.getInstance().recordMissionResult(false, threatLevel, squad);
            Toast.makeText(this, "Defeat...", Toast.LENGTH_LONG).show();
            finish();
        } else {
            // Reset to first alive member for the new player round
            currentTurnIndex = 0;
            while (currentTurnIndex < squad.size() && !squad.get(currentTurnIndex).isAlive()) {
                currentTurnIndex++;
            }
            updateUI();
        }
    }
}
