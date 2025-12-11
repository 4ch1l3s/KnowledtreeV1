package com.example.knowledtree.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knowledtree.ParkingRecord;
import com.example.knowledtree.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<ParkingRecord> data;

    public HistoryAdapter(List<ParkingRecord> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ánh xạ layout item_parking_record.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingRecord record = data.get(position);

        // 1. Hiển thị Biển số và Trạng thái
        holder.tvPlate.setText(record.getPlateNumber());
        holder.tvStatus.setText(record.getStatusText());

        // Thay đổi màu sắc trạng thái
        if (record.getCheckoutTime() == null) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        }

        // 2. Hiển thị Thời gian vào
        holder.tvCheckin.setText("Vào: " + record.getCheckinTime());

        // 3. Xử lý thông tin Checkout/Tiền (Chỉ hiện khi xe đã ra)
        if (record.getCheckoutTime() != null) {
            String checkoutInfo = String.format("Ra: %s - Tiền: %s",
                    record.getCheckoutTime(),
                    record.getTongTienText());
            holder.tvCheckoutInfo.setText(checkoutInfo);
            holder.tvCheckoutInfo.setVisibility(View.VISIBLE);
        } else {
            holder.tvCheckoutInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // Lớp ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlate, tvStatus, tvCheckin, tvCheckoutInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlate = itemView.findViewById(R.id.tv_plate_number);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCheckin = itemView.findViewById(R.id.tv_checkin_time);
            tvCheckoutInfo = itemView.findViewById(R.id.tv_checkout_info);
        }
    }

    public void updateData(List<ParkingRecord> newRecords) {
        this.data.clear();
        this.data.addAll(newRecords);
        notifyDataSetChanged();
    }
}