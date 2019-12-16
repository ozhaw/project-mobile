package org.nure.julia.auth;

import android.content.Context;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.gson.JsonObject;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.nure.julia.database.PersistenceContext;
import org.nure.julia.dto.AccountDto;
import org.nure.julia.rest.RestClient;

import java.util.function.Consumer;
import java.util.stream.Stream;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;

public final class AuthenticationService {

    private AccountDto accountDto;
    private GoogleSignInClient googleSignInClient;

    public static final AuthenticationService INSTANCE = new AuthenticationService();

    private AuthenticationService() {
    }

    public boolean logoutGoogleAccount() {
        if (googleSignInClient.signOut().isSuccessful()) {
            accountDto = null;
            return true;
        } else {
            return false;
        }
    }

    public boolean logoutFacebookAccount() {
        LoginManager.getInstance().logOut();
        accountDto = null;
        return true;
    }

    public AccountDto getAccountDto() {
        return accountDto;
    }

    public void setAccountDto(AccountDto accountDto) {
        this.accountDto = accountDto;
    }

    public void setGoogleSignInClient(GoogleSignInClient googleSignInClient) {
        this.googleSignInClient = googleSignInClient;
    }

    public Header getAuthorizationHeader() {
        return accountDto != null && accountDto.getAccessToken() != null
                ? new BasicHeader("Authorization", "Bearer " + accountDto.getAccessToken())
                : null;
    }

    public void verify(Context context, Consumer<AccountDto> onSuccess) {
        Header header = getAuthorizationHeader();
        if (header != null) {
            RestClient.get(context, "/authentication/api/authentication/verify",
                    new Header[]{getAuthorizationHeader()}, null, new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            if (statusCode != HttpStatus.SC_OK) {
                                revokeAccess(context, onSuccess);
                            } else {
                                onSuccess.accept(accountDto);
                            }
                        }
                    });
        } else {
            revokeAccess(context, onSuccess);
        }
    }

    private void revokeAccess(Context context, Consumer<AccountDto> onSuccess) {
        final JsonObject json = new JsonObject();
        json.addProperty("email", accountDto.getEmail());
        json.addProperty("password", accountDto.getId());

        final HttpEntity httpEntity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);

        RestClient.post(context, "/user/api/user/authorization", null, httpEntity,
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

                                onSuccess.accept(accountDto);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
    }

    public AuthenticationService load() {
        PersistenceContext.INSTANCE.getConnection()
                .userRepository()
                .getAll()
                .stream()
                .findFirst()
                .ifPresent(user -> {
                    accountDto = new AccountDto();
                    accountDto.setId(user.password);
                    accountDto.setEmail(user.email);
                });

        return this;
    }

}
