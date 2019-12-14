package org.nure.julia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.loadingview.LoadingDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.nure.julia.auth.AuthenticationService;
import org.nure.julia.database.PersistenceContext;
import org.nure.julia.database.entity.User;
import org.nure.julia.dto.AccountDto;
import org.nure.julia.mapper.FacebookAccountToAccountDtoMapper;
import org.nure.julia.mapper.GoogleSignInAccountToAccountDtoMapper;
import org.nure.julia.mapper.Mapper;
import org.nure.julia.mapper.SystemType;
import org.nure.julia.rest.RestClient;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "AndroidClarified";

    private static final Mapper<GoogleSignInAccount, AccountDto> GOOGLE_TO_ACCOUNT =
            new GoogleSignInAccountToAccountDtoMapper();
    private static final Mapper<JSONObject, AccountDto> FACEBOOK_TO_ACCOUNT =
            new FacebookAccountToAccountDtoMapper();

    private static final int GOOGLE_REQUEST_CODE = 1501;

    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private LoadingDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = LoadingDialog.Companion.get(this);

        findExisting();
    }

    @Override
    public void onStart() {
        super.onStart();
        /*GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (lastSignedInAccount != null) {
            validate(GOOGLE_TO_ACCOUNT.map(lastSignedInAccount));
        } else {
            Log.d(TAG, "Google Authentication is not detected. Trying to use Facebook");
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if (accessToken != null) {
                useLoginInformation(accessToken);
            } else {
                Log.d(TAG, "Facebook Authentication is rejected");
            }
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == GOOGLE_REQUEST_CODE) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                validate(GOOGLE_TO_ACCOUNT.map(account));
            } catch (ApiException e) {
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    private void onLoggedIn(AccountDto accountDto) {
        final User user = new User();
        user.id = accountDto.getUserId();
        user.email = accountDto.getEmail();
        user.password = accountDto.getId();

        PersistenceContext.INSTANCE.getConnection().userRepository()
                .deleteAll();
        PersistenceContext.INSTANCE.getConnection().userRepository()
                .insert(user);

        Intent intent = new Intent(this, MainActivity.class);

        AuthenticationService.INSTANCE.setGoogleSignInClient(googleSignInClient);
        AuthenticationService.INSTANCE.setAccountDto(accountDto);

        if (this.getIntent() != null && this.getIntent().getExtras() != null) {
            intent.putExtras(this.getIntent().getExtras());
        }

        startActivity(intent);
        finish();
    }

    private void validate(AccountDto accountDto) {
        dialog.show();

        final JsonObject json = new JsonObject();
        json.addProperty("email", accountDto.getEmail());
        json.addProperty("password", accountDto.getId());

        final HttpEntity httpEntity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);

        RestClient.post(this, "/user/api/user/authorization", null, httpEntity,
                ContentType.APPLICATION_JSON.toString(), new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        if (statusCode == HttpStatus.SC_OK) {
                            try {
                                Stream.of(headers).filter(header -> header.getName().startsWith("Securitytoken"))
                                        .findFirst()
                                        .ifPresent(header -> accountDto.setAccessToken(header.getValue()));

                                accountDto.setUserId(response.getLong("id"));
                                accountDto.setPhotoUri(response.getString("photoUrl"));
                                accountDto.setName(response.getString("username"));

                                onLoggedIn(accountDto);
                            } catch (JSONException e) {
                                dialog.hide();

                                e.printStackTrace();
                            }
                        }
                    }
                });
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

        final Context context = this;

        facebookSignInButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                useLoginInformation(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                dialog.hide();

                Toast.makeText(context, "Cannot resiter with Facebook", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onError(FacebookException error) {
                dialog.hide();

                Toast.makeText(context, "Cannot resiter with Facebook", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void useLoginInformation(AccessToken accessToken) {
        GraphRequest request = GraphRequest
                .newMeRequest(accessToken, (object, response) -> validate(FACEBOOK_TO_ACCOUNT.map(object)));

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);

        request.executeAsync();
    }

    private void findExisting() {
        Optional<User> user = PersistenceContext.INSTANCE.getConnection()
                .userRepository()
                .getAll()
                .stream()
                .findFirst();

        if (user.isPresent()) {
            final AccountDto accountDto = new AccountDto();
            accountDto.setId(user.get().password);
            accountDto.setEmail(user.get().email);

            validate(accountDto);
        } else {
            setContentView(R.layout.activity_login);

            final EditText usernameEditText = findViewById(R.id.username);
            final EditText passwordEditText = findViewById(R.id.password);
            final Button loginButton = findViewById(R.id.login);
            final ProgressBar loadingProgressBar = findViewById(R.id.loading);

            passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    validate(AccountDto.builder()
                            .setEmail(usernameEditText.getText().toString())
                            .setId(passwordEditText.getText().toString())
                            .setSystemType(SystemType.NATIVE)
                            .build()
                    );
                }
                return false;
            });

            loginButton.setOnClickListener(v -> {
                loadingProgressBar.setVisibility(View.VISIBLE);
                validate(AccountDto.builder()
                        .setEmail(usernameEditText.getText().toString())
                        .setId(passwordEditText.getText().toString())
                        .setSystemType(SystemType.NATIVE)
                        .build()
                );
            });

            registerGoogleServices();

            registerFacebookServices();
        }
    }
}
