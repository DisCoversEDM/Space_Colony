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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EconomyFragment extends Fragment {

    private RecyclerView rvMiners;
    private RecyclerView rvShop;
    private TextView tvInflationIndicator;
    private MinerAdapter minerAdapter;
    private ShopAdapter shopAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_economy, container, false);

        rvMiners = view.findViewById(R.id.rv_miners);
        rvShop = view.findViewById(R.id.rv_shop);
        tvInflationIndicator = view.findViewById(R.id.tv_inflation_indicator);

        rvMiners.setLayoutManager(new LinearLayoutManager(getContext()));
        minerAdapter = new MinerAdapter();
        rvMiners.setAdapter(minerAdapter);

        rvShop.setLayoutManager(new LinearLayoutManager(getContext()));
        shopAdapter = new ShopAdapter();
        rvShop.setAdapter(shopAdapter);

        updateUI();

        return view;
    }

    public void updateUI() {
        if (tvInflationIndicator != null) {
            double inflation = GameManager.getInstance().getInflationRate();
            tvInflationIndicator.setText(getString(R.string.economy_inflation_format, inflation));
        }
        if (minerAdapter != null) {
            ArrayList<Miner> miners = new ArrayList<>();
            int minersMiningAsteroids = 0;
            for (CrewMember cm : GameManager.getInstance().getAllCrew()) {
                if (cm instanceof Miner && cm.isAlive()) {
                    Miner miner = (Miner) cm;
                    miners.add(miner);
                    if (miner.isMining() && miner.isMiningAsteroid()) {
                        minersMiningAsteroids++;
                    }
                }
            }
            int totalSpaceships = Storage.getInstance().getSpaceships().size();
            minerAdapter.setData(miners, totalSpaceships, minersMiningAsteroids);
        }
        if (shopAdapter != null) {
            shopAdapter.updateItems();
        }

        if (GameManager.getInstance().isGameOver() && getView() != null) {
            getView().setAlpha(0.5f);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private class MinerAdapter extends RecyclerView.Adapter<MinerAdapter.ViewHolder> {
        private List<Miner> miners = new ArrayList<>();
        private int totalSpaceships = 0;
        private int minersMiningAsteroids = 0;

        public void setData(List<Miner> miners, int totalSpaceships, int minersMiningAsteroids) {
            this.miners = miners;
            this.totalSpaceships = totalSpaceships;
            this.minersMiningAsteroids = minersMiningAsteroids;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_miner_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Miner miner = miners.get(position);
            holder.tvName.setText(miner.name);
            holder.tvLastPayout.setText(getString(R.string.miner_last_payout_format, String.valueOf(miner.getLastPayout())));

            boolean hasSpaceship = totalSpaceships > 0;
            boolean asteroidSlotAvailable = minersMiningAsteroids < totalSpaceships;
            boolean isGameOver = GameManager.getInstance().isGameOver();
            
            if (miner.isMining()) {
                String miningType = miner.isMiningAsteroid() ? " (Asteroid)" : " (Land)";
                holder.tvStatus.setText(getString(R.string.miner_mining) + miningType);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_role_badge);
                
                holder.btnMineLand.setVisibility(View.GONE);
                holder.btnMineAsteroid.setVisibility(View.GONE);
                holder.spacerBtns.setVisibility(View.GONE);
                holder.btnStopMining.setVisibility(View.VISIBLE);
                holder.btnStopMining.setEnabled(!isGameOver);
            } else {
                if (hasSpaceship) {
                    holder.tvStatus.setText(getString(R.string.asteroid_unlocked) + " (" + (totalSpaceships - minersMiningAsteroids) + " slots)");
                } else {
                    holder.tvStatus.setText(R.string.asteroid_locked);
                }
                holder.tvStatus.setBackgroundResource(R.drawable.bg_role_badge);
                
                holder.btnMineLand.setVisibility(View.VISIBLE);
                holder.btnMineAsteroid.setVisibility(View.VISIBLE);
                holder.spacerBtns.setVisibility(View.VISIBLE);
                holder.btnStopMining.setVisibility(View.GONE);

                boolean canMine = miner.energy > 0 && !isGameOver;
                holder.btnMineLand.setEnabled(canMine);
                holder.btnMineAsteroid.setEnabled(canMine && hasSpaceship && asteroidSlotAvailable);
                holder.btnMineLand.setAlpha(canMine ? 1.0f : 0.5f);
                holder.btnMineAsteroid.setAlpha((canMine && hasSpaceship && asteroidSlotAvailable) ? 1.0f : 0.5f);
            }

            holder.btnMineLand.setOnClickListener(v -> {
                miner.startMining(false);
                updateUI();
                Toast.makeText(getContext(), miner.name + " started mining land.", Toast.LENGTH_SHORT).show();
            });

            holder.btnMineAsteroid.setOnClickListener(v -> {
                miner.startMining(true);
                updateUI();
                Toast.makeText(getContext(), miner.name + " started mining asteroids.", Toast.LENGTH_SHORT).show();
            });

            holder.btnStopMining.setOnClickListener(v -> {
                miner.stopMining();
                updateUI();
                Toast.makeText(getContext(), miner.name + " stopped mining.", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return miners.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvStatus, tvLastPayout;
            Button btnMineLand, btnMineAsteroid, btnStopMining;
            View spacerBtns;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_miner_name);
                tvStatus = itemView.findViewById(R.id.tv_spaceship_status);
                tvLastPayout = itemView.findViewById(R.id.tv_last_payout);
                btnMineLand = itemView.findViewById(R.id.btn_mine_land);
                btnMineAsteroid = itemView.findViewById(R.id.btn_mine_asteroid);
                btnStopMining = itemView.findViewById(R.id.btn_stop_mining);
                spacerBtns = itemView.findViewById(R.id.spacer_btns);
            }
        }
    }

    private class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {
        private List<ShopItem> shopItems = new ArrayList<>();

        public ShopAdapter() {
            updateItems();
        }

        public void updateItems() {
            shopItems.clear();
            
            // Dynamic info from classes
            Spaceship previewShip = new Spaceship();
            shopItems.add(new ShopItem(getString(R.string.shop_spaceship), 
                "Allows one miner to reach asteroids for 5x profit.", 
                previewShip.getPrice(), R.drawable.ic_asteroid, "Spaceship"));

            EnergyRestore previewPotion = new EnergyRestore();
            shopItems.add(new ShopItem(getString(R.string.shop_potion), 
                "Restores " + previewPotion.getEnergyAmount() + " energy to a crew member.", 
                previewPotion.getPrice(), R.drawable.ic_potion, "EnergyRestore"));

            FirstAidKit previewKit = new FirstAidKit();
            shopItems.add(new ShopItem(getString(R.string.shop_first_aid), 
                "Fully restores health to a crew member.", 
                previewKit.getPrice(), R.drawable.ic_first_aid, "FirstAidKit"));

            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_entry, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ShopItem item = shopItems.get(position);
            holder.tvName.setText(item.name);
            holder.tvDesc.setText(item.description);
            holder.ivIcon.setImageResource(item.iconRes);

            int inflatedPrice = GameManager.getInstance().getInflatedPrice(item.basePrice);
            holder.tvPrice.setText(String.format(Locale.US, "¢ %d", inflatedPrice));

            boolean isGameOver = GameManager.getInstance().isGameOver();
            holder.btnBuy.setEnabled(!isGameOver);
            holder.btnBuy.setAlpha(isGameOver ? 0.5f : 1.0f);

            holder.btnBuy.setOnClickListener(v -> {
                if (GameManager.getInstance().getCredits() >= inflatedPrice) {
                    if (item.type.equals("Spaceship")) {
                        Storage.getInstance().buySpaceship(new Spaceship());
                    } else if (item.type.equals("EnergyRestore")) {
                        Storage.getInstance().buyItem(new EnergyRestore());
                    } else if (item.type.equals("FirstAidKit")) {
                        Storage.getInstance().buyItem(new FirstAidKit());
                    }
                    Toast.makeText(getContext(), "Purchased " + item.name, Toast.LENGTH_SHORT).show();
                    updateUI();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateHUD();
                    }
                } else {
                    Toast.makeText(getContext(), "Not enough credits!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return shopItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvName, tvDesc, tvPrice;
            Button btnBuy;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_shop_icon);
                tvName = itemView.findViewById(R.id.tv_shop_item_name);
                tvDesc = itemView.findViewById(R.id.tv_shop_item_desc);
                tvPrice = itemView.findViewById(R.id.tv_shop_price);
                btnBuy = itemView.findViewById(R.id.btn_buy);
            }
        }
    }

    private static class ShopItem {
        String name;
        String description;
        int basePrice;
        int iconRes;
        String type;

        ShopItem(String name, String description, int basePrice, int iconRes, String type) {
            this.name = name;
            this.description = description;
            this.basePrice = basePrice;
            this.iconRes = iconRes;
            this.type = type;
        }
    }
}
