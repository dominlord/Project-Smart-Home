package com.example.smarthome;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private List<HashMap<String, Object>> deviceList;
    private OnDeviceClickListener clickListener;
    private OnDeviceStateChangedListener stateChangedListener;
    private List<Integer> selectedItems = new ArrayList<>();

    public DeviceAdapter(List<HashMap<String, Object>> deviceList,
                         OnDeviceClickListener clickListener,
                         OnDeviceStateChangedListener stateChangedListener) {
        this.deviceList = deviceList;
        this.clickListener = clickListener;
        this.stateChangedListener = stateChangedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, Object> device = deviceList.get(position);
        holder.txtDeviceName.setText((String) device.get("name"));
        holder.switchDevice.setChecked((Boolean) device.get("isOn"));

        if (selectedItems.contains(position)) {
            holder.itemView.setBackgroundResource(R.drawable.selected_item_background);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.default_item_background);
        }

        holder.switchDevice.setOnCheckedChangeListener(null);

        holder.switchDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            device.put("isOn", isChecked);

            if (stateChangedListener != null) {
                stateChangedListener.onDeviceStateChanged();
            }
        });


        holder.itemView.setOnClickListener(v -> {
            if (selectedItems.contains(position)) {
                selectedItems.remove(Integer.valueOf(position));
            } else {
                selectedItems.add(position);
            }
            notifyItemChanged(position);
            clickListener.onDeviceClick(device);
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDeviceName;
        Switch switchDevice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDeviceName = itemView.findViewById(R.id.txtDeviceName);
            switchDevice = itemView.findViewById(R.id.switchDevice);
        }
    }

    public interface OnDeviceClickListener {
        void onDeviceClick(HashMap<String, Object> device);
    }

    public List<Integer> getSelectedItems() {
        return selectedItems;
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }
}
