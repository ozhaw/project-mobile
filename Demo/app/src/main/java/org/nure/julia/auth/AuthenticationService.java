package org.nure.julia.auth;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import org.nure.julia.dto.AccountDto;

public final class AuthenticationService {

    private AccountDto accountDto;
    private GoogleSignInClient googleSignInClient;

    public static final AuthenticationService INSTANCE = new AuthenticationService();

    private AuthenticationService() {
    }

    private boolean logoutGoogleAccount() {
        if (googleSignInClient.signOut().isSuccessful()) {
            accountDto = null;
            return true;
        } else {
            return false;
        }
    }

    private boolean logoutFacebookAccount() {
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
}
