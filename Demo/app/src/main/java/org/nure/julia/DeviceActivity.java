package org.nure.julia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.nure.julia.auth.AuthenticationService;
import org.nure.julia.database.PersistenceContext;
import org.nure.julia.database.entity.Device;
import org.nure.julia.rest.RestClient;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;

public class DeviceActivity extends AppCompatActivity {
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";

    private Activity activity;

    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_added_activity);

        activity = this;

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        super.onNewIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask(this).doInBackground(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask(this).doInBackground(tag);
                    break;
                }
            }
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent
                .getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @SuppressLint("StaticFieldLeak")
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
        private Context context;

        public NdefReaderTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                NdefMessage ndefMessage = ndef.getCachedNdefMessage();

                NdefRecord[] records = ndefMessage.getRecords();
                for (NdefRecord ndefRecord : records) {
                    if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                        try {
                            String result = readText(ndefRecord);
                            verify(result);
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, "Unsupported Encoding", e);
                        }
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            byte[] payload = record.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 51;
            return new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
        }

        private void saveDevice(Device device) {
            PersistenceContext.INSTANCE.getConnection().deviceRepository().insert(device);

            startMainIntent(true);
        }

        private void verify(String result) {
            Device device = new Gson().fromJson(result, Device.class);

            setWaitMessage(device.deviceId, true);

            AuthenticationService.INSTANCE.load().verify(context, accountDto -> {
                Header[] headers = new Header[]{AuthenticationService.INSTANCE.getAuthorizationHeader()};

                RestClient.get(context, "/device/api/device/external/" + device.deviceId, headers, null,
                        new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                if (statusCode == HttpStatus.SC_OK) {
                                    Toast.makeText(context, "Sorry, device was already registered for the user", Toast.LENGTH_LONG).show();
                                    startMainIntent(false);
                                } else {
                                    addDevice(device);
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                                addDevice(device);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                                addDevice(device);
                            }
                        });
            });
        }

        private void addDevice(Device device) {
            Header[] headers = new Header[]{
                    AuthenticationService.INSTANCE.getAuthorizationHeader(),
                    new BasicHeader("userId", AuthenticationService.INSTANCE.getAccountDto().getUserId().toString())
            };

            final JsonObject json = new JsonObject();
            json.addProperty("deviceId", device.deviceId);
            json.addProperty("type", device.type);

            final HttpEntity httpEntity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);

            RestClient.post(context, "/device/api/device", headers, httpEntity, ContentType.APPLICATION_JSON.toString(),
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            if (statusCode == HttpStatus.SC_OK) {
                                try {
                                    device.id = response.getLong("id");

                                    saveDevice(device);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                            throwable.printStackTrace();
                            startMainIntent(false);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                            throwable.printStackTrace();
                            startMainIntent(false);
                        }
                    });
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(ApplicationInstance.getContext(), "Read content: " + result, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setWaitMessage(String message, boolean isProgressVisible) {
        findViewById(R.id.loadingDeviceAdd).setVisibility(isProgressVisible ? View.VISIBLE : View.GONE);

        ((TextView) findViewById(R.id.waitText))
                .setText(String.format(getString(R.string.deviceRegResource), message));
    }

    private void startMainIntent(boolean isSuccessful) {
        Intent intent = new Intent(activity, MainActivity.class);

        intent.putExtra("deviceWasAdded", isSuccessful);
        intent.putExtra("afterDeviceAdding", true);

        startActivity(intent);
        finish();
    }
}
