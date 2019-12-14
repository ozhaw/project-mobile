package org.nure.julia.mapper;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;
import org.nure.julia.dto.AccountDto;

public class FacebookAccountToAccountDtoMapper implements Mapper<JSONObject, AccountDto> {

    @Override
    public AccountDto map(JSONObject from) {
        AccountDto to = new AccountDto();
        try {
            to.setName(from.getString("name"));
            to.setEmail(from.getString("email"));
            to.setId(from.getString("id"));
            to.setPhotoUri(from.getJSONObject("picture").getJSONObject("data").getString("url"));
            to.setSystemType(SystemType.FACEBOOK);
        } catch (JSONException e) {
            Log.e(FacebookAccountToAccountDtoMapper.class.getName(), "", e);
            throw new RuntimeException(e);
        }

        return to;
    }

}
