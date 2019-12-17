package org.nure.julia.ui.device;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.nure.julia.R;
import org.nure.julia.database.entity.Device;

import java.util.ArrayList;
import java.util.Random;

public class DeviceAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Device> objects;

    private final int[] ICONS = new int[] {
            android.R.drawable.ic_lock_idle_low_battery,
            android.R.drawable.ic_dialog_alert,
            android.R.drawable.stat_notify_sdcard_usb,
            android.R.drawable.ic_lock_idle_charging
    };

    DeviceAdapter(Context context, ArrayList<Device> products) {
        ctx = context;
        objects = products;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.device_card, parent, false);
        }

        Device device = getDevice(position);

        ((TextView) view.findViewById(R.id.device_name)).setText(device.deviceId);
        ((TextView) view.findViewById(R.id.device_type)).setText(device.type);
        ((ImageView) view.findViewById(R.id.device_icon)).setImageResource(ICONS[new Random().nextInt(ICONS.length)]);

        return view;
    }

    Device getDevice(int position) {
        return ((Device) getItem(position));
    }

}