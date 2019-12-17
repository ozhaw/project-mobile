package org.nure.julia.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.nure.julia.R;
import org.nure.julia.auth.AuthenticationService;
import org.nure.julia.database.entity.Device;
import org.nure.julia.rest.RestClient;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpStatus;

public class DeviceFragment extends Fragment {

    DeviceAdapter deviceAdapter;
    ArrayList<Device> devices;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_devices, container, false);

        devices = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(getContext(), devices);

        ListView listView = root.findViewById(R.id.devices_list);
        listView.setAdapter(deviceAdapter);

        FloatingActionButton sync = getActivity().findViewById(R.id.sync);
        sync.setOnClickListener(view -> updateDeviceList(root));

        updateDeviceList(root);

        return root;
    }

    private void updateDeviceList(View root) {
        AuthenticationService.INSTANCE.load().verify(getContext(), accountDto -> {
            Header[] headers = new Header[]{AuthenticationService.INSTANCE.getAuthorizationHeader()};

            RestClient.get(getContext(), String.format("/device/api/device/users/%s/devices", accountDto.getUserId()), headers, null,
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                            if (statusCode == HttpStatus.SC_OK) {
                                devices.clear();

                                devices.addAll(IntStream.range(0, array.length())
                                        .boxed()
                                        .map(i -> {
                                            try {
                                                return new Gson().fromJson(array.getJSONObject(i).toString(), Device.class);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                return null;
                                            }
                                        })
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList()));

                                deviceAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            Toast.makeText(getContext(), "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        }
    }
}