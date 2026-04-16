package com.example.spacecolony;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MissionFragment extends Fragment {

    private RecyclerView rvSquadSelect;
    private Button btnLaunch;
    private SquadAdapter adapter;
    private Set<CrewMember> selectedSquad = new HashSet<>();
    private TextView tvThreatName, tvThreatLevel, tvThreatDamage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission, container, false);

        rvSquadSelect = view.findViewById(R.id.rv_squad_select);
        btnLaunch = view.findViewById(R.id.btn_launch_mission);
        tvThreatName = view.findViewById(R.id.tv_threat_name);
        tvThreatLevel = view.findViewById(R.id.tv_threat_level);
        tvThreatDamage = view.findViewById(R.id.tv_threat_damage);

        setupThreatInfo();

        rvSquadSelect.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SquadAdapter(GameManager.getInstance().getAllCrew());
        rvSquadSelect.setAdapter(adapter);

        btnLaunch.setOnClickListener(v -> {
            if (selectedSquad.size() >= 1 && selectedSquad.size() <= 3) {
                Intent intent = new Intent(getActivity(), BattleActivity.class);
                intent.putIntegerArrayListExtra("squad_ids", getSelectedIds());
                startActivity(intent);
            }
        });

        return view;
    }

    public void updateUI() {
        if (!isAdded()) return;
        setupThreatInfo();
        updateLaunchButton();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void setupThreatInfo() {
        GameManager gm = GameManager.getInstance();
        if (gm.isThreatActive()) {
            int turn = gm.getTurn();
            int threatLevel = (turn / 6) + 1;
            tvThreatName.setText("Vortex Ravager");
            tvThreatLevel.setText("Threat level " + threatLevel);
            tvThreatDamage.setText("Deals " + (threatLevel * 10) + "–" + (threatLevel * 10 + 15) + " dmg / turn");
        } else {
            tvThreatName.setText("Area Secure");
            tvThreatLevel.setText("Next threat scanning...");
            tvThreatDamage.setText("");
        }
    }

    private ArrayList<Integer> getSelectedIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (CrewMember cm : selectedSquad) {
            ids.add(cm.getId());
        }
        return ids;
    }

    private void updateLaunchButton() {
        GameManager gm = GameManager.getInstance();
        boolean isThreatActive = gm.isThreatActive();
        boolean hasBattled = gm.isThreatBattled();
        boolean isGameOver = gm.isGameOver();
        
        btnLaunch.setEnabled(!isGameOver && isThreatActive && !hasBattled && selectedSquad.size() >= 1 && selectedSquad.size() <= 3);
        btnLaunch.setAlpha(btnLaunch.isEnabled() ? 1.0f : 0.5f);
    }

    private class SquadAdapter extends RecyclerView.Adapter<SquadAdapter.ViewHolder> {
        private List<CrewMember> crewList;

        public SquadAdapter(List<CrewMember> crewList) {
            this.crewList = crewList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crew_selectable, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CrewMember cm = crewList.get(position);
            holder.tvName.setText(cm.name);
            holder.tvRole.setText(cm.getClass().getSimpleName());
            
            int roleColor = ContextCompat.getColor(getContext(), R.color.teal_300);
            if (cm instanceof Medic) roleColor = ContextCompat.getColor(getContext(), R.color.amber_400);
            else if (cm instanceof Builder) roleColor = ContextCompat.getColor(getContext(), R.color.blue_400);
            else if (cm instanceof Miner) roleColor = ContextCompat.getColor(getContext(), R.color.gray_400);
            holder.tvRole.setTextColor(roleColor);

            holder.tvXp.setText(getString(R.string.crew_xp_format, cm.experience));

            holder.progressHp.setMax(cm.maxHP);
            holder.progressHp.setProgress(cm.healthPoints);
            holder.tvHpValue.setText(cm.healthPoints + "/" + cm.maxHP);

            holder.progressEnergy.setMax(cm.maxEnergy);
            holder.progressEnergy.setProgress(cm.energy);
            holder.tvEnergyValue.setText(cm.energy + "/" + cm.maxEnergy);

            boolean isSelected = selectedSquad.contains(cm);
            holder.viewOverlay.setAlpha(isSelected ? 0.3f : 0f);
            holder.ivCheck.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);

            // Disable if dead or busy or Game Over
            boolean isBusy = false;
            GameManager gm = GameManager.getInstance();
            if (Hospital.getInstance().getAssignedBuilder() == cm || 
                Simulator.getInstance().getAssignedBuilder() == cm ||
                (cm instanceof Miner && ((Miner) cm).isMining()) ||
                gm.getHospitalQueue().contains(cm) ||
                gm.getSimulatorQueue().contains(cm)) {
                isBusy = true;
            }

            if (!cm.isAlive() || isBusy || gm.isGameOver()) {
                holder.itemView.setEnabled(false);
                holder.itemView.setAlpha(0.5f);
                // If it was selected and now busy, deselect it
                if (isSelected) {
                    selectedSquad.remove(cm);
                    updateLaunchButton();
                }
            } else {
                holder.itemView.setEnabled(true);
                holder.itemView.setAlpha(1.0f);
                holder.itemView.setOnClickListener(v -> {
                    if (selectedSquad.contains(cm)) {
                        selectedSquad.remove(cm);
                    } else {
                        if (selectedSquad.size() < 3) {
                            selectedSquad.add(cm);
                        } else {
                            Toast.makeText(getContext(), "Max 3 squad members", Toast.LENGTH_SHORT).show();
                        }
                    }
                    notifyItemChanged(position);
                    updateLaunchButton();
                });
            }
        }

        @Override
        public int getItemCount() {
            return crewList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole, tvHpValue, tvEnergyValue, tvXp;
            ProgressBar progressHp, progressEnergy;
            View viewOverlay;
            ImageView ivCheck;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_sel_crew_name);
                tvRole = itemView.findViewById(R.id.tv_sel_crew_role);
                tvHpValue = itemView.findViewById(R.id.tv_sel_hp_value);
                tvEnergyValue = itemView.findViewById(R.id.tv_sel_energy_value);
                tvXp = itemView.findViewById(R.id.tv_sel_crew_xp);
                progressHp = itemView.findViewById(R.id.progress_sel_hp);
                progressEnergy = itemView.findViewById(R.id.progress_sel_energy);
                viewOverlay = itemView.findViewById(R.id.view_selection_overlay);
                ivCheck = itemView.findViewById(R.id.iv_selected_check);
            }
        }
    }
}
