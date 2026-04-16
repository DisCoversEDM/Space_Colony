package com.example.spacecolony;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.ArrayList;

public class CrewFragment extends Fragment {

    private RecyclerView rvCrew;
    private CrewAdapter adapter;
    private ExtendedFloatingActionButton fabHire;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crew, container, false);

        rvCrew = view.findViewById(R.id.rv_crew);
        fabHire = view.findViewById(R.id.fab_hire);

        rvCrew.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CrewAdapter(GameManager.getInstance().getAllCrew());
        rvCrew.setAdapter(adapter);

        fabHire.setOnClickListener(v -> {
            if (GameManager.getInstance().isGameOver()) return;
            HireBottomSheetFragment hireSheet = new HireBottomSheetFragment();
            hireSheet.setOnCrewHiredListener(this::updateUI);
            hireSheet.show(getParentFragmentManager(), "HireBottomSheet");
        });

        updateUI();

        return view;
    }

    public void updateUI() {
        if (!isAdded()) return;
        
        GameManager gm = GameManager.getInstance();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (gm.isGameOver()) {
            fabHire.setEnabled(false);
            fabHire.setAlpha(0.5f);
            if (getView() != null) {
                getView().setAlpha(0.5f);
            }
        } else {
            fabHire.setEnabled(true);
            fabHire.setAlpha(1.0f);
            if (getView() != null) {
                getView().setAlpha(1.0f);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private class CrewAdapter extends RecyclerView.Adapter<CrewAdapter.ViewHolder> {
        private ArrayList<CrewMember> crewList;

        public CrewAdapter(ArrayList<CrewMember> crewList) {
            this.crewList = crewList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crew_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CrewMember cm = crewList.get(position);
            holder.tvName.setText(cm.name);
            
            // Set Role
            String role = cm.getClass().getSimpleName();
            holder.tvRole.setText(role);
            int roleColor = ContextCompat.getColor(getContext(), R.color.teal_300);
            if (cm instanceof Medic) roleColor = ContextCompat.getColor(getContext(), R.color.amber_400);
            else if (cm instanceof Builder) roleColor = ContextCompat.getColor(getContext(), R.color.blue_400);
            else if (cm instanceof Miner) roleColor = ContextCompat.getColor(getContext(), R.color.gray_400);
            
            holder.tvRole.setTextColor(roleColor);

            // Set XP
            holder.tvXp.setText(getString(R.string.crew_xp_format, cm.experience));

            // Set Status
            String status = getString(R.string.status_quarters);
            boolean isBusy = false;
            
            if (Hospital.getInstance().getAssignedBuilder() == cm) {
                status = "Upgrading Hospital";
                isBusy = true;
            } else if (Simulator.getInstance().getAssignedBuilder() == cm) {
                status = "Upgrading Simulator";
                isBusy = true;
            } else if (cm instanceof Miner && ((Miner) cm).isMining()) {
                status = "Mining";
                isBusy = true;
            } else if (GameManager.getInstance().getHospitalQueue().contains(cm)) {
                if (cm instanceof Medic) {
                    status = ((Medic) cm).isActingAsStaff() ? "Hospital (Staff)" : "Hospital (Patient)";
                } else {
                    status = "Hospital (Patient)";
                }
            } else if (GameManager.getInstance().getSimulatorQueue().contains(cm)) {
                status = "Simulator";
            }
            
            holder.tvStatus.setText(status);

            // HP
            holder.progressHp.setMax(cm.maxHP);
            holder.progressHp.setProgress(cm.healthPoints);
            holder.tvHpValue.setText(cm.healthPoints + "/" + cm.maxHP);

            // Energy
            holder.progressEnergy.setMax(cm.maxEnergy);
            holder.progressEnergy.setProgress(cm.energy);
            holder.tvEnergyValue.setText(cm.energy + "/" + cm.maxEnergy);
            
            // Greyscale if dead or busy or Game Over
            if (!cm.isAlive()) {
                holder.itemView.setAlpha(0.5f);
                holder.tvStatus.setText("DECEASED");
            } else if (isBusy || GameManager.getInstance().isGameOver()) {
                holder.itemView.setAlpha(0.6f);
            } else {
                holder.itemView.setAlpha(1.0f);
            }
        }

        @Override
        public int getItemCount() {
            return crewList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole, tvStatus, tvHpValue, tvEnergyValue, tvXp;
            ProgressBar progressHp, progressEnergy;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_crew_name);
                tvRole = itemView.findViewById(R.id.tv_crew_role);
                tvStatus = itemView.findViewById(R.id.tv_crew_status);
                tvHpValue = itemView.findViewById(R.id.tv_crew_hp_value);
                tvEnergyValue = itemView.findViewById(R.id.tv_crew_energy_value);
                tvXp = itemView.findViewById(R.id.tv_crew_xp);
                progressHp = itemView.findViewById(R.id.progress_crew_hp);
                progressEnergy = itemView.findViewById(R.id.progress_crew_energy);
            }
        }
    }
}
