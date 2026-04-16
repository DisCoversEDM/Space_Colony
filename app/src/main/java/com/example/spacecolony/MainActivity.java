package com.example.spacecolony;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TextView tvTurnNumber;
    private TextView tvCredits;
    private TextView tvThreatCount;
    private TextView tvInflation;
    private Button btnNextTurn;
    private ImageButton btnSettings;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTurnNumber = findViewById(R.id.tv_turn_number);
        tvCredits = findViewById(R.id.tv_credits);
        tvThreatCount = findViewById(R.id.tv_threat_count);
        tvInflation = findViewById(R.id.tv_inflation);
        btnNextTurn = findViewById(R.id.btn_next_turn);
        btnSettings = findViewById(R.id.btn_settings);

        navView = findViewById(R.id.bottom_nav);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ColonyFragment())
                    .commit();
        }

        btnNextTurn.setOnClickListener(v -> {
            GameManager.getInstance().nextTurn();
            updateHUD();
            
            checkForMission();

            // Notify the current fragment to update its UI
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            updateFragmentUI(currentFragment);
        });

        btnSettings.setOnClickListener(v -> {
            SaveLoadBottomSheet bottomSheet = new SaveLoadBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "SaveLoadBottomSheet");
        });

        navView.setOnItemSelectedListener(item -> {
            if (GameManager.getInstance().isGameOver()) {
                if (item.getItemId() == R.id.nav_stats) {
                    switchToFragment(new StatsFragment());
                    return true;
                }
                return false;
            }

            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_colony) {
                selectedFragment = new ColonyFragment();
            } else if (itemId == R.id.nav_crew) {
                selectedFragment = new CrewFragment();
            } else if (itemId == R.id.nav_economy) {
                selectedFragment = new EconomyFragment();
            } else if (itemId == R.id.nav_mission) {
                selectedFragment = new MissionFragment();
            } else if (itemId == R.id.nav_stats) {
                selectedFragment = new StatsFragment();
            }

            if (selectedFragment != null) {
                switchToFragment(selectedFragment);
                return true;
            }
            return false;
        });

        updateHUD();
    }

    private void switchToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHUD();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        updateFragmentUI(currentFragment);
    }

    private void updateFragmentUI(Fragment currentFragment) {
        if (currentFragment instanceof ColonyFragment) {
            ((ColonyFragment) currentFragment).updateUI();
        } else if (currentFragment instanceof CrewFragment) {
            ((CrewFragment) currentFragment).updateUI();
        } else if (currentFragment instanceof EconomyFragment) {
            ((EconomyFragment) currentFragment).updateUI();
        } else if (currentFragment instanceof MissionFragment) {
            ((MissionFragment) currentFragment).updateUI();
        }
    }

    private void checkForMission() {
        if (GameManager.getInstance().isThreatActive()) {
            navView.setSelectedItemId(R.id.nav_mission);
        }
    }

    public void updateHUD() {
        GameManager gm = GameManager.getInstance();
        
        if (gm.isGameOver()) {
            showGameOver();
            return;
        }

        tvTurnNumber.setText(String.valueOf(gm.getTurn()));
        tvCredits.setText(getString(R.string.hud_credits_format, String.valueOf(gm.getCredits())));
        
        if (tvInflation != null) {
            tvInflation.setText(String.format("%.1fx", gm.getInflationRate()));
        }

        // Basic threat logic
        int threatIn = gm.getTurnsUntilThreat();
        boolean isThreatActive = gm.isThreatActive();
        
        if (isThreatActive) {
            tvThreatCount.setText("THREAT ACTIVE!");
            tvThreatCount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            
            if (gm.isThreatBattled() || !gm.hasLivingCrew()) {
                btnNextTurn.setEnabled(true);
                btnNextTurn.setAlpha(1.0f);
            } else {
                btnNextTurn.setEnabled(false);
                btnNextTurn.setAlpha(0.5f);
            }
        } else {
            tvThreatCount.setText(getString(R.string.hud_threat_format, threatIn));
            tvThreatCount.setTextColor(getResources().getColor(android.R.color.white));
            btnNextTurn.setEnabled(true);
            btnNextTurn.setAlpha(1.0f);
        }
    }

    private void showGameOver() {
        btnNextTurn.setEnabled(false);
        btnNextTurn.setAlpha(0.5f);
        tvThreatCount.setText("GAME OVER");
        tvThreatCount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        
        // Switch to stats fragment if not already there
        if (navView.getSelectedItemId() != R.id.nav_stats) {
            navView.setSelectedItemId(R.id.nav_stats);
        }

        // Optional: Show an alert dialog once
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("You have lost 3 battles. The colony has fallen.")
                .setPositiveButton("View Stats", null)
                .setCancelable(false)
                .show();
    }
}
