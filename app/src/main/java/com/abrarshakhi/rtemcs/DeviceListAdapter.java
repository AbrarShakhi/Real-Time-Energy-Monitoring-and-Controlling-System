package com.abrarshakhi.rtemcs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.abrarshakhi.rtemcs.model.DeviceInfo;

import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<DeviceInfo> {
    private final Context context;
    private final List<DeviceInfo> deviceList;
    private final LayoutInflater inflater;

    public DeviceListAdapter(Context context, List<DeviceInfo> deviceList) {
        super(context, R.layout.device_list_item, deviceList);
        this.context = context;
        this.deviceList = deviceList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.device_list_item, parent, false);
            holder = new ViewHolder();
            holder.tvDeviceName = convertView.findViewById(R.id.tvDeviceNameListItem);
            holder.tvDeviceInfo = convertView.findViewById(R.id.tvDeviceInfoListItem);
            holder.tvDeviceStatus = convertView.findViewById(R.id.tvDeviceStatusListItem);
            holder.ivStatusIcon = convertView.findViewById(R.id.ivStatusIcon); // optional — rename in XML if needed
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DeviceInfo device = deviceList.get(position);
        holder.tvDeviceName.setText(device.getDeviceName());
        holder.tvDeviceInfo.setText("ID: " + device.getDeviceId());
        holder.ivStatusIcon.setImageResource(device.isRunning() ? R.drawable.ic_play : R.drawable.ic_pause);
        holder.tvDeviceStatus.setText(device.isRunning() ? "Running" : "Stopped");
        convertView.setOnClickListener(
            v ->
                context.startActivity(new Intent(context, DeviceDetailActivity.class).putExtra("ID", device.getId()))
        );

        return convertView;
    }

    static class ViewHolder {
        TextView tvDeviceName;
        TextView tvDeviceInfo;
        TextView tvDeviceStatus;
        ImageView ivStatusIcon;
    }
}
