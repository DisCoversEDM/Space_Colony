package com.example.spacecolony;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ColonyFragment extends Fragment {

    private TextView tvHospitalCapacity;
    private TextView tvSimulatorCapacity;
    private TextView tvQuartersCapacity;
    
    private ImageView ivHospitalWrench;
    private ImageView ivSimulatorWrench;
    
    private ProgressBar progressHospital;
    private ProgressBar progressSimulator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_colony, container, false);

        // Initialize Capacity/Info TextViews
        tvHospitalCapacity = view.findViewById(R.id.tv_hospital_capacity);
        tvSimulatorCapacity = view.findViewById(R.id.tv_simulator_capacity);
        tvQuartersCapacity = view.findViewById(R.id.tv_quarters_capacity);

        // Initialize Wrench Icons (Builder indicators)
        ivHospitalWrench = view.findViewById(R.id.iv_hospital_wrench);
        ivSimulatorWrench = view.findViewById(R.id.iv_simulator_wrench);

        // Initialize Progress Bars
        progressHospital = view.findViewById(R.id.progress_hospital_upgrade);
        progressSimulator = view.findViewById(R.id.progress_simulator_upgrade);

        // Tap targets
        View hospitalBuilding = view.findViewById(R.id.building_hospital);
        View simulatorBuilding = view.findViewById(R.id.building_simulator);
        View quartersArea = view.findViewById(R.id.building_quarters);

        // Set click listeners
        hospitalBuilding.setOnClickListener(v -> {
            if (GameManager.getInstance().isGameOver()) return;
            BuildingBottomSheet sheet = BuildingBottomSheet.newInstance(getString(R.string.building_hospital), Hospital.getInstance());
            sheet.show(getChildFragmentManager(), "hospital_sheet");
        });
        
        simulatorBuilding.setOnClickListener(v -> {
            if (GameManager.getInstance().isGameOver()) return;
            BuildingBottomSheet sheet = BuildingBottomSheet.newInstance(getString(R.string.building_simulator), Simulator.getInstance());
            sheet.show(getChildFragmentManager(), "simulator_sheet");
        });
        
        // Quarters area is now just a label, no special click needed as turn is in HUD
        quartersArea.setOnClickListener(null);

        updateUI();

        return view;
    }

    public void updateUI() {
        if (!isAdded()) return;
        
        GameManager gm = GameManager.getInstance();
        Hospital hospital = Hospital.getInstance();
        Simulator simulator = Simulator.getInstance();

        // Update capacity for real buildings
        tvHospitalCapacity.setText(getString(R.string.building_capacity_format, gm.getHospitalQueue().size(), hospital.getCapacity()));
        tvSimulatorCapacity.setText(getString(R.string.building_capacity_format, gm.getSimulatorQueue().size(), simulator.getCapacity()));
        
        // Quarters shows a label
        tvQuartersCapacity.setText(R.string.quarters_label);

        // Hide wrenches and progress bars until implemented
        ivHospitalWrench.setVisibility(View.GONE);
        ivSimulatorWrench.setVisibility(View.GONE);
        progressHospital.setVisibility(View.GONE);
        progressSimulator.setVisibility(View.GONE);

        View root = getView();
        if (root != null) {
            if (gm.isGameOver()) {
                root.setAlpha(0.5f);
            } else {
                root.setAlpha(1.0f);
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}
