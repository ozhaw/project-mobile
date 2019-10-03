package org.nure.julia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;
import org.nure.julia.auth.AuthenticationService;
import org.nure.julia.dto.AccountDto;
import org.nure.julia.mapper.FacebookAccountToAccountDtoMapper;
import org.nure.julia.mapper.GoogleSignInAccountToAccountDtoMapper;
import org.nure.julia.mapper.Mapper;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "AndroidClarified";

    private static final Mapper<GoogleSignInAccount, AccountDto> GOOGLE_TO_ACCOUNT =
            new GoogleSignInAccountToAccountDtoMapper();
    private static final Mapper<JSONObject, AccountDto> FACEBOOK_TO_ACCOUNT =
            new FacebookAccountToAccountDtoMapper();

    private static final int GOOGLE_REQUEST_CODE = 1501;

    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerGoogleServices();

        registerFacebookServices();
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (lastSignedInAccount != null) {
            onLoggedIn(GOOGLE_TO_ACCOUNT.map(lastSignedInAccount));
        } else {
            Log.d(TAG, "Google Authentication is not detected. Trying to use Facebook");
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if (accessToken != null) {
                useLoginInformation(accessToken);
            } else {
                Log.d(TAG, "Facebook Authentication is rejected");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == GOOGLE_REQUEST_CODE) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onLoggedIn(GOOGLE_TO_ACCOUNT.map(account));
            } catch (ApiException e) {
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    private void onLoggedIn(AccountDto accountDto) {
        Intent intent = new Intent(this, MainActivity.class);

        AuthenticationService.INSTANCE.setGoogleSignInClient(googleSignInClient);
        AuthenticationService.INSTANCE.setAccountDto(accountDto);

        startActivity(intent);
        finish();
    }

    private void registerGoogleServices() {
        SignInButton googleSignInButton = findViewById(R.id.google_sign_in_button);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE);
        });
    }

    private void registerFacebookServices() {
        LoginButton facebookSignInButton = findViewById(R.id.facebook_sign_in_button);
        facebookSignInButton.setPermissions(Arrays.asList("email", "public_profile"));

        callbackManager = CallbackManager.Factory.create();

        facebookSignInButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                useLoginInformation(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    private void useLoginInformation(AccessToken accessToken) {
        GraphRequest request = GraphRequest
                .newMeRequest(accessToken, (object, response) -> onLoggedIn(FACEBOOK_TO_ACCOUNT.map(object)));

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);

        request.executeAsync();
    }
}
