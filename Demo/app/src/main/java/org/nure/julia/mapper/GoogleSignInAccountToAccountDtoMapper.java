package org.nure.julia.mapper;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.nure.julia.dto.AccountDto;

public class GoogleSignInAccountToAccountDtoMapper implements Mapper<GoogleSignInAccount, AccountDto> {

    @Override
    public AccountDto map(GoogleSignInAccount from) {
        AccountDto to = new AccountDto();
        to.setName(from.getDisplayName());
        to.setEmail(from.getEmail());
        to.setId(from.getId());
        to.setPhotoUri(from.getPhotoUrl().toString());
        to.setSystemType(SystemType.GOOGLE);
        return to;
    }

}
