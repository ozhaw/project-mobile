package org.nure.julia.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.nure.julia.R;
import org.nure.julia.database.PersistenceContext;
import org.nure.julia.database.entity.Notification;

import java.util.List;

import static java.lang.String.format;

public class HomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        populateData(root);

        FloatingActionButton sync = getActivity().findViewById(R.id.sync);
        sync.setOnClickListener(view -> populateData(root));

        return root;
    }

    private void populateData(View root) {
        Notification healthNotification = PersistenceContext.INSTANCE.getConnection()
                .notificationRepository()
                .getLastCriticalHealthSeverity();

        Notification deviceNotification = PersistenceContext.INSTANCE.getConnection()
                .notificationRepository()
                .getLastCriticalDeviceSeverity();

        List<Notification> notifications = PersistenceContext.INSTANCE.getConnection()
                .notificationRepository()
                .getLast50();

        if (healthNotification != null) {
            TextView healthSeverity = root.findViewById(R.id.health_severity);
            healthSeverity.setText(format(getString(R.string.severity), healthNotification.severity));

            TextView healthAdvice = root.findViewById(R.id.health_advice);
            healthAdvice.setText(format(getString(R.string.advice), healthNotification.advice));

            TextView healthTime = root.findViewById(R.id.health_time);
            healthTime.setText(format(getString(R.string.time), healthNotification.time));
        }

        if (deviceNotification != null) {
            TextView deviceSeverity = root.findViewById(R.id.device_severity);
            deviceSeverity.setText(format(getString(R.string.severity), deviceNotification.severity));

            TextView deviceAdvice = root.findViewById(R.id.device_advice);
            deviceAdvice.setText(format(getString(R.string.advice), deviceNotification.advice));

            TextView deviceTime = root.findViewById(R.id.device_time);
            deviceTime.setText(format(getString(R.string.time), deviceNotification.time));
        }

        if (notifications != null && !notifications.isEmpty()) {
            LinearLayout linearLayout = root.findViewById(R.id.notification_list);

            linearLayout.removeAllViews();

            notifications.forEach(notification -> {
                TextView textView = new TextView(getContext());
                textView.setText(notification.severity + " - " + notification.time);
                textView.setTextColor(getResources().getColor(R.color.browser_actions_bg_grey));

                linearLayout.addView(textView);
            });
        }
    }
}