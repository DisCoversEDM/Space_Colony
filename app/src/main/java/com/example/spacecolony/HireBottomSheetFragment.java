package com.example.spacecolony;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HireBottomSheetFragment extends BottomSheetDialogFragment {

    private RecyclerView rvHireOptions;
    private HireAdapter adapter;
    private Runnable onCrewHired;
    private List<String> firstNames = new ArrayList<>();
    private List<String> lastNames = new ArrayList<>();

    public void setOnCrewHiredListener(Runnable listener) {
        this.onCrewHired = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_hire, container, false);
        rvHireOptions = view.findViewById(R.id.rv_hire_options);
        rvHireOptions.setLayoutManager(new LinearLayoutManager(getContext()));
        
        loadNamesFromJson();
        
        adapter = new HireAdapter(getHireOptions());
        rvHireOptions.setAdapter(adapter);
        
        return view;
    }

    private void loadNamesFromJson() {
        try {
            InputStream is = getContext().getAssets().open("names.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);
            
            JSONArray firstNamesArray = obj.getJSONArray("firstNames");
            for (int i = 0; i < firstNamesArray.length(); i++) {
                firstNames.add(firstNamesArray.getString(i));
            }
            
            JSONArray lastNamesArray = obj.getJSONArray("lastNames");
            for (int i = 0; i < lastNamesArray.length(); i++) {
                lastNames.add(lastNamesArray.getString(i));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            // Fallback names if JSON fails
            firstNames.add("Kael");
            lastNames.add("Stellar");
        }
    }

    private List<HireOption> getHireOptions() {
        List<HireOption> options = new ArrayList<>();
        options.add(new HireOption(getString(R.string.role_soldier), getString(R.string.desc_soldier), R.color.teal_400, "Soldier"));
        options.add(new HireOption(getString(R.string.role_special_soldier), getString(R.string.desc_special_soldier), R.color.role_special_soldier, "SpecialSoldier"));
        options.add(new HireOption(getString(R.string.role_medic), getString(R.string.desc_medic), R.color.amber_400, "Medic"));
        options.add(new HireOption(getString(R.string.role_miner), getString(R.string.desc_miner), R.color.gray_400, "Miner"));
        options.add(new HireOption(getString(R.string.role_builder), getString(R.string.desc_builder), R.color.blue_400, "Builder"));
        return options;
    }

    private class HireOption {
        String roleName;
        String description;
        int colorRes;
        String type;

        HireOption(String roleName, String description, int colorRes, String type) {
            this.roleName = roleName;
            this.description = description;
            this.colorRes = colorRes;
            this.type = type;
        }
    }

    private class HireAdapter extends RecyclerView.Adapter<HireAdapter.ViewHolder> {
        private List<HireOption> options;
        private Random random = new Random();

        HireAdapter(List<HireOption> options) {
            this.options = options;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hire_option, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HireOption option = options.get(position);
            holder.tvRoleName.setText(option.roleName);
            holder.tvDescription.setText(option.description);

            // Preview stats and price from an instance of the member
            CrewMember previewMember = createMember(option.type, "Preview");
            
            int inflatedPrice = GameManager.getInstance().getInflatedPrice(previewMember.getPrice());
            holder.tvPrice.setText(getString(R.string.price_format, inflatedPrice));
            holder.viewRoleStrip.setBackgroundColor(ContextCompat.getColor(getContext(), option.colorRes));

            holder.tvStatHp.setText(getString(R.string.stat_hp_short, previewMember.maxHP));
            holder.tvStatEnergy.setText(getString(R.string.stat_energy_short, previewMember.maxEnergy));

            holder.btnHire.setOnClickListener(v -> {
                CrewMember newMember = createMember(option.type, generateRandomName());
                
                boolean success = Storage.getInstance().recruitCrewMember(newMember);
                
                if (success) {
                    Toast.makeText(getContext(), getString(R.string.hire_success, newMember.name), Toast.LENGTH_SHORT).show();
                    
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateHUD();
                    }

                    if (onCrewHired != null) onCrewHired.run();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), getString(R.string.not_enough_credits), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private CrewMember createMember(String type, String name) {
            switch (type) {
                case "Soldier": return new Soldier(name);
                case "SpecialSoldier": return new SpecialSoldier(name);
                case "Medic": return new Medic(name);
                case "Miner": return new Miner(name);
                case "Builder": return new Builder(name);
                default: return new Soldier(name);
            }
        }

        private String generateRandomName() {
            if (firstNames.isEmpty() || lastNames.isEmpty()) {
                return "Unknown Entity " + random.nextInt(100);
            }
            String firstName = firstNames.get(random.nextInt(firstNames.size()));
            String lastName = lastNames.get(random.nextInt(lastNames.size()));
            return firstName + " " + lastName;
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View viewRoleStrip;
            TextView tvRoleName, tvDescription, tvStatHp, tvStatEnergy, tvPrice;
            Button btnHire;

            ViewHolder(View itemView) {
                super(itemView);
                viewRoleStrip = itemView.findViewById(R.id.view_role_strip);
                tvRoleName = itemView.findViewById(R.id.tv_hire_role_name);
                tvDescription = itemView.findViewById(R.id.tv_hire_role_desc);
                tvStatHp = itemView.findViewById(R.id.tv_hire_stat_hp);
                tvStatEnergy = itemView.findViewById(R.id.tv_hire_stat_energy);
                tvPrice = itemView.findViewById(R.id.tv_hire_price);
                btnHire = itemView.findViewById(R.id.btn_hire);
            }
        }
    }
}
