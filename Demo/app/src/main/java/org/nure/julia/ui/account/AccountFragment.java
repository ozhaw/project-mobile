package org.nure.julia.ui.account;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.squareup.picasso.Picasso;

import org.nure.julia.R;
import org.nure.julia.auth.AuthenticationService;

public class AccountFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);
        final ImageView userPhoto = root.findViewById(R.id.user_photo);
        Picasso.get().load(AuthenticationService.INSTANCE.getAccountDto().getPhotoUri())
                .into(userPhoto);

        final TextView username = root.findViewById(R.id.account_username);
        username.setText(AuthenticationService.INSTANCE.getAccountDto().getName());

        final TextView email = root.findViewById(R.id.account_email);
        email.setText(AuthenticationService.INSTANCE.getAccountDto().getEmail());

        final ImageView auth = root.findViewById(R.id.account_auth_with);
        if (AuthenticationService.INSTANCE.getAccountDto().getSystemType() == null) {
            auth.setImageBitmap(invertImage(BitmapFactory.decodeResource(getResources(), R.drawable.react_icon)));
        } else {
            switch (AuthenticationService.INSTANCE.getAccountDto().getSystemType()) {
                case GOOGLE:
                    auth.setImageResource(R.drawable.common_google_signin_btn_icon_light);
                    break;
                case FACEBOOK:
                    auth.setImageResource(R.drawable.com_facebook_favicon_blue);
                    break;
                case NATIVE:
                default:
                    auth.setImageBitmap(invertImage(BitmapFactory.decodeResource(getResources(), R.drawable.react_icon)));
            }
        }
        return root;
    }

    private Bitmap invertImage(Bitmap src) {
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        int A, R, G, B;
        int pixelColor;

        int height = src.getHeight();
        int width = src.getWidth();

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {

                pixelColor = src.getPixel(x, y);

                A = Color.alpha(pixelColor);

                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);

                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return bmOut;
    }
}