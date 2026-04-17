package com.example.spacecolony;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Locale;

public class StatsFragment extends Fragment implements GameManager.OnCreditsChangedListener {

    private TextView tvMissions, tvWinLoss, tvCredits, tvThreat, tvTopCrew;
    private Button btnNewGame;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        tvMissions = view.findViewById(R.id.tv_stat_missions);
        tvWinLoss = view.findViewById(R.id.tv_stat_winloss);
        tvCredits = view.findViewById(R.id.tv_stat_credits);
        tvThreat = view.findViewById(R.id.tv_stat_threat);
        tvTopCrew = view.findViewById(R.id.tv_stat_top_crew);
        btnNewGame = view.findViewById(R.id.btn_new_game);

        btnNewGame.setOnClickListener(v -> resetGame());

        updateStats();

        return view;
    }

    private void resetGame() {
        GameManager.getInstance().reset();
        Storage.reset();
        Hospital.reset();
        Simulator.reset();
        Quarters.reset();

        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.updateHUD();
            // Programmatically click the Colony tab to navigate back
            activity.findViewById(R.id.nav_colony).performClick();
        }
    }

    private void updateStats() {
        if (!isAdded()) return;

        GameManager gm = GameManager.getInstance();

        tvMissions.setText(String.valueOf(gm.getMissionsCompleted()));

        tvWinLoss.setText(getString(R.string.stats_win_loss_format,
                gm.getMissionsWon(), gm.getMissionsLost()));

        onCreditsChanged(gm.getTotalCreditsEarned());

        tvThreat.setText(getString(R.string.stats_threat_format, gm.getHighestThreatDefeated()));

        CrewMember topCrew = gm.getMostUsedCrew();
        if (topCrew != null) {
            int count = gm.getMissionCountForCrew(topCrew);
            String role = topCrew.getClass().getSimpleName();
            tvTopCrew.setText(getString(R.string.stats_top_crew_format, topCrew.name, role, count));
        } else {
            tvTopCrew.setText(getString(R.string.stats_no_missions));
        }

        // Show New Game button only if the game is over
        if (gm.isGameOver()) {
            btnNewGame.setVisibility(View.VISIBLE);
        } else {
            btnNewGame.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreditsChanged(int totalCreditsEarned) {
        if (tvCredits != null) {
            String formattedCredits = String.format(Locale.getDefault(), "%,d", totalCreditsEarned);
            tvCredits.setText(getString(R.string.stats_credits_format, formattedCredits));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        GameManager.getInstance().setOnCreditsChangedListener(this);
        updateStats();
    }

    @Override
    public void onPause() {
        super.onPause();
        GameManager.getInstance().setOnCreditsChangedListener(null);
    }
}
