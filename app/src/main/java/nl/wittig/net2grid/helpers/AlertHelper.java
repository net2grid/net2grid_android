package nl.wittig.net2grid.helpers;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.widget.ImageView;

import com.kaopiz.kprogresshud.KProgressHUD;

import nl.wittig.net2grid.R;

public class AlertHelper {

    private static KProgressHUD hud;

    public static KProgressHUD errorOccured(Context context, String message) {

        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.icon_cross_white);

        hud = KProgressHUD.create(context)
                .setDimAmount(0.8f)
                .setCustomView(imageView)
                .setLabel(message)
                .setWindowColor(Color.TRANSPARENT);

        scheduleDismiss();

        return hud;
    }

    public static void scheduleDismiss() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                hud.dismiss();
            }
        }, 5000);
    }
}
