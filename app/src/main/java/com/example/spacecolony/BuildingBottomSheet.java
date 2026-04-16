package com.example.spacecolony;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;

public class BuildingBottomSheet extends BottomSheetDialogFragment {

    private String buildingName;
    private Building building;
    private CrewAdapter adapter;

    public static BuildingBottomSheet newInstance(String name, Building building) {
        BuildingBottomSheet fragment = new BuildingBottomSheet();
        fragment.buildingName = name;
        fragment.building = building;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_building, container, false);

        TextView tvName = view.findViewById(R.id.tv_building_name);
        TextView tvCapacity = view.findViewById(R.id.tv_building_capacity);
        ImageView ivIcon = view.findViewById(R.id.iv_building_icon);
        RecyclerView rvCrew = view.findViewById(R.id.rv_building_crew);
        Button btnUpgrade = view.findViewById(R.id.btn_upgrade_building);

        tvName.setText(buildingName);
        tvCapacity.setText(getString(R.string.building_capacity_label_format, getAssignedCrew().size(), building.getCapacity()));
        
        if (building instanceof Hospital) {
            ivIcon.setImageResource(R.drawable.ic_building_hospital);
        } else if (building instanceof Simulator) {
            ivIcon.setImageResource(R.drawable.ic_building_simulator);
        }

        rvCrew.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CrewAdapter(GameManager.getInstance().getAllCrew(), crew -> {
            if (building instanceof Hospital) {
                if (crew instanceof Medic && !GameManager.getInstance().getHospitalQueue().contains(crew)) {
                    showMedicRoleDialog(crew, tvCapacity);
                } else {
                    GameManager.getInstance().sendToHospital(crew);
                    updateUI(tvCapacity);
                }
            } else if (building instanceof Simulator) {
                GameManager.getInstance().sendToSimulator(crew);
                updateUI(tvCapacity);
            }
        });
        rvCrew.setAdapter(adapter);

        updateUpgradeButton(btnUpgrade);
        btnUpgrade.setOnClickListener(v -> {
            if (building.isUpgradePending()) {
                Toast.makeText(getContext(), "Upgrade already in progress!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Find available builders in Quarters
            ArrayList<Builder> availableBuilders = new ArrayList<>();
            for (CrewMember cm : GameManager.getInstance().getAllCrew()) {
                if (cm instanceof Builder && cm.isAlive() &&
                    !GameManager.getInstance().getHospitalQueue().contains(cm) && 
                    !GameManager.getInstance().getSimulatorQueue().contains(cm)) {
                    
                    // Also check if they are already assigned to another building upgrade
                    if (Hospital.getInstance().getAssignedBuilder() != cm && 
                        Simulator.getInstance().getAssignedBuilder() != cm) {
                        availableBuilders.add((Builder) cm);
                    }
                }
            }

            if (availableBuilders.isEmpty()) {
                Toast.makeText(getContext(), "No builders available in Quarters!", Toast.LENGTH_LONG).show();
                return;
            }

            showBuilderSelectionDialog(availableBuilders, btnUpgrade, tvCapacity);
        });

        return view;
    }

    private void updateUpgradeButton(Button btnUpgrade) {
        if (building.isUpgradePending()) {
            btnUpgrade.setText("Upgrade Pending (" + building.getAssignedBuilder().name + ")");
            btnUpgrade.setEnabled(false);
        } else {
            btnUpgrade.setText(getString(R.string.building_upgrade) + " (¢" + building.getUpgradeCost() + ")");
            btnUpgrade.setEnabled(true);
        }
    }

    private void showBuilderSelectionDialog(ArrayList<Builder> builders, Button btnUpgrade, TextView tvCapacity) {
        String[] builderNames = new String[builders.size()];
        for (int i = 0; i < builders.size(); i++) {
            builderNames[i] = builders.get(i).name;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select a Builder")
                .setItems(builderNames, (dialog, which) -> {
                    Builder selectedBuilder = builders.get(which);
                    if (GameManager.getInstance().spendCredits(building.getUpgradeCost())) {
                        building.setUpgradePending(true, selectedBuilder);
                        updateUpgradeButton(btnUpgrade);
                        updateUI(tvCapacity);
                        Toast.makeText(getContext(), "Upgrade started by " + selectedBuilder.name, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Not enough credits!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showMedicRoleDialog(CrewMember crew, TextView tvCapacity) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Assign Medic")
                .setMessage("How would you like to assign " + crew.name + "?")
                .setPositiveButton("Staff", (dialog, which) -> {
                    GameManager.getInstance().sendToHospital(crew, true);
                    updateUI(tvCapacity);
                })
                .setNegativeButton("Patient", (dialog, which) -> {
                    GameManager.getInstance().sendToHospital(crew, false);
                    updateUI(tvCapacity);
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void updateUI(TextView tvCapacity) {
        tvCapacity.setText(getString(R.string.building_capacity_label_format, getAssignedCrew().size(), building.getCapacity()));
        adapter.notifyDataSetChanged();
    }

    private ArrayList<CrewMember> getAssignedCrew() {
        if (building instanceof Hospital) return GameManager.getInstance().getHospitalQueue();
        if (building instanceof Simulator) return GameManager.getInstance().getSimulatorQueue();
        return new ArrayList<>();
    }

    // Simple Adapter for testing
    private class CrewAdapter extends RecyclerView.Adapter<CrewAdapter.ViewHolder> {
        private ArrayList<CrewMember> crewList;
        private OnCrewClickListener listener;

        public CrewAdapter(ArrayList<CrewMember> crewList, OnCrewClickListener listener) {
            this.crewList = crewList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CrewMember cm = crewList.get(position);
            String status = "";
            String type = cm.getClass().getSimpleName();
            boolean isBusy = false;
            boolean isDeceased = !cm.isAlive();
            
            if (isDeceased) {
                status = " (DECEASED)";
            } else if (Hospital.getInstance().getAssignedBuilder() == cm) {
                status = " (Upgrading Hospital)";
                isBusy = true;
            } else if (Simulator.getInstance().getAssignedBuilder() == cm) {
                status = " (Upgrading Simulator)";
                isBusy = true;
            } else if (cm instanceof Miner && ((Miner) cm).isMining()) {
                status = " (Mining)";
                isBusy = true;
            } else if (GameManager.getInstance().getHospitalQueue().contains(cm)) {
                if (cm instanceof Medic) {
                    status = ((Medic) cm).isActingAsStaff() ? " (Staff)" : " (Patient)";
                } else {
                    status = " (Patient)";
                }
            } else if (GameManager.getInstance().getSimulatorQueue().contains(cm)) {
                status = " (Simulator)";
            } else {
                status = " (Quarters)";
            }
            
            holder.textView.setText(cm.name + " - " + type + status);
            
            // Prevent clicking deceased or busy crew
            holder.itemView.setEnabled(!isBusy && !isDeceased);
            holder.itemView.setAlpha((isBusy || isDeceased) ? 0.5f : 1.0f);

            holder.itemView.setOnClickListener(v -> listener.onCrewClick(cm));
        }

        @Override
        public int getItemCount() { return crewList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }

    interface OnCrewClickListener {
        void onCrewClick(CrewMember crew);
    }
}
