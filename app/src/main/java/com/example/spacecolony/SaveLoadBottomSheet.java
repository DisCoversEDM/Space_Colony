package com.example.spacecolony;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaveLoadBottomSheet extends BottomSheetDialogFragment {

    private TextView tvLastSave;
    private Button btnSave;
    private Button btnLoad;
    private Button btnNewGame;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_save_load, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLastSave = view.findViewById(R.id.tv_last_save);
        btnSave = view.findViewById(R.id.btn_save_game);
        btnLoad = view.findViewById(R.id.btn_load_game);
        btnNewGame = view.findViewById(R.id.btn_new_game);

        updateLastSaveText();

        btnSave.setOnClickListener(v -> {
            saveGame();
            dismiss();
        });

        btnLoad.setOnClickListener(v -> {
            loadGame();
            dismiss();
        });

        btnNewGame.setOnClickListener(v -> {
            resetGame();
            dismiss();
        });
    }

    private void updateLastSaveText() {
        SharedPreferences prefs = requireContext().getSharedPreferences("SpaceColonyPrefs", Context.MODE_PRIVATE);
        long lastSaveTime = prefs.getLong("last_save_time", 0);
        if (lastSaveTime == 0) {
            tvLastSave.setText(R.string.no_save_found);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String dateStr = sdf.format(new Date(lastSaveTime));
            tvLastSave.setText(getString(R.string.last_save_format, dateStr));
        }
    }

    private void saveGame() {
        if (SaveManager.saveGame(requireContext())) {
            Toast.makeText(getContext(), "Game Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to save game", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGame() {
        if (SaveManager.loadGame(requireContext())) {
            Toast.makeText(getContext(), "Game Loaded", Toast.LENGTH_SHORT).show();
            refreshUI();
        } else {
            Toast.makeText(getContext(), "No save found or failed to load", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetGame() {
        GameManager.getInstance().reset();
        Storage.reset();
        Hospital.reset();
        Simulator.reset();
        Quarters.reset();
        
        Toast.makeText(getContext(), "New Game Started", Toast.LENGTH_SHORT).show();
        refreshUI();
    }

    private void refreshUI() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.updateHUD();
            // Refresh current fragment
            activity.onResume();
        }
    }
}
