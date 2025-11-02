package com.abrarshakhi.rtemcs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter {

    private Context context;
    private List<Device> deviceList;

    // Constructor
    public DeviceListAdapter(Context context, List<Device> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.device_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.deviceNameTextView = convertView.findViewById(R.id.tvDeviceNameListItem);
            viewHolder.deviceInfoTextView = convertView.findViewById(R.id.tvDeviceInfoListItem);
            viewHolder.deviceStatusTextView = convertView.findViewById(R.id.tvDeviceStatusListItem);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Device device = deviceList.get(position);

        viewHolder.deviceNameTextView.setText(device.getDeviceName());
        viewHolder.deviceInfoTextView.setText(device.getDeviceInfo());
        viewHolder.deviceStatusTextView.setText(device.getDeviceStatus());

        return convertView;
    }


    // View holder pattern for better performance
    private static class ViewHolder {
        TextView deviceNameTextView;
        TextView deviceInfoTextView;
        TextView deviceStatusTextView;
    }

    // Device model class to store device data
    public static class Device {
        private final String deviceName;
        private final String deviceInfo;
        private final String deviceStatus;

        public Device(String deviceName, String deviceInfo, String deviceStatus) {
            this.deviceName = deviceName;
            this.deviceInfo = deviceInfo;
            this.deviceStatus = deviceStatus;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getDeviceInfo() {
            return deviceInfo;
        }

        public String getDeviceStatus() {
            return deviceStatus;
        }
    }
}
