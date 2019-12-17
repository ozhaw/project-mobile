package org.nure.julia.ui.logout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.loadingview.LoadingDialog;

import org.nure.julia.LoginActivity;
import org.nure.julia.auth.AuthenticationService;
import org.nure.julia.database.PersistenceContext;

import java.util.Optional;

import static org.nure.julia.mapper.SystemType.NATIVE;

public class LogoutFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Optional.ofNullable(PersistenceContext.INSTANCE.getConnection()
                .userRepository()
                .getById(AuthenticationService.INSTANCE.getAccountDto().getUserId()))
                .ifPresent(user -> PersistenceContext.INSTANCE.getConnection()
                        .userRepository().deleteAll());

        Optional.ofNullable(AuthenticationService.INSTANCE.getAccountDto())
                .ifPresent(accountDto -> {
                    AuthenticationService.INSTANCE.logoutGoogleAccount();
                    AuthenticationService.INSTANCE.logoutFacebookAccount();
                    AuthenticationService.INSTANCE.setAccountDto(null);
                });

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);

        return null;
    }
}