package nl.wittig.net2grid_ble.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import nl.wittig.net2grid_ble.BuildConfig;
import nl.wittig.net2grid_ble.R;
import nl.wittig.net2grid_ble.onboarding.api.CodeMsgHandler;
import nl.wittig.net2grid_ble.onboarding.api.model.BaseInternalResponse;
import nl.wittig.net2grid_ble.onboarding.api.model.NetworkItemModel;
import nl.wittig.net2grid_ble.onboarding.api.responses.NetworkResponse;

public class Tools {

    private static final String EXTERNAL_SAMBA_DIRECTORY = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES) + File.separator + "samba" + File.separator;
    private static final String LOWER_UPPER_REGEX = "(.)([A-Z])";

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static Bitmap getColouredBitmap(Bitmap bmp, int color) {
        Bitmap resultBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Paint drawingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        drawingPaint.setColor(color);

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(bmp.extractAlpha(), 0, 0, drawingPaint);

        return resultBitmap;
    }

    public static Bitmap getColouredBitmap(Context context, int resId, int color) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), resId);
        Bitmap resultBitmap = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(), Bitmap.Config.ARGB_8888);
        Paint drawingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        drawingPaint.setColor(color);

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(icon.extractAlpha(), 0, 0, drawingPaint);

        return resultBitmap;
    }

    public static void showApiFailIfShould(Context context, BaseInternalResponse baseInternalResponse) {
        if (BuildConfig.DEBUG &&
                !isCodeInArray(baseInternalResponse.getCode(), CodeMsgHandler.REGISTRATION_FATAL_EXCEPTION_CODES) &&
                !isCodeInArray(baseInternalResponse.getCode(), CodeMsgHandler.NON_TOAST_EXCEPTION_CODES)) {

            String msg = new CodeMsgHandler(context).getStandardApiErrorMessage();
            if (baseInternalResponse.getCode() != BaseInternalResponse.NO_CODE) {
                msg += " (" + baseInternalResponse.getCode() + ")";
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public static String createMacAddressFromByteArray(byte[] array) {

        Formatter formatter = new Formatter();
        int index = 0;
        for (byte b : array) {

            String format = index == array.length - 1 ? "%02x" : "%02x:";

            formatter.format(format, b);
            index++;
        }
        return formatter.toString();
    }

    public static void showDialog(DialogFragment dialogFragment, FragmentManager fragmentManager) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        dialogFragment.show(ft, "dialog");
    }

    public static void showError(Context context, EditText editText, String error) {
        if (error != null && !error.isEmpty()) {
            editText.setError(error);
        }
        editText.setBackgroundResource(R.drawable.rectangle_red);
        editText.setTextColor(context.getResources().getColor(R.color.red));
        editText.setHintTextColor(context.getResources().getColor(R.color.red));
    }

    public static void hideError(Context context, EditText editText) {
        editText.setError(null);
        editText.setBackgroundResource(R.drawable.rectanglegrey_withshadow);
        editText.setTextColor(context.getResources().getColor(R.color.grey_dark));
        editText.setHintTextColor(context.getResources().getColor(R.color.grey));
    }

    public static boolean checkIfTextHasEnoughSpace(String text, TextView textView, int layoutWidth) {
        return checkIfSpecificTextFitIn(text, textView, layoutWidth, false);
    }

    public static boolean checkIfSpecificTextFitIn(String text, TextView textView, int layoutWidth,
                                                   boolean isWithMixedTypeface) {
        float marginMultiplier = 1.10f;
        if (isWithMixedTypeface) {
            marginMultiplier = 0.95f;
        }
        Rect bounds = new Rect();
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int width = bounds.width();
        if (width * marginMultiplier > layoutWidth) {
            return false;
        } else {
            return true;
        }
    }

    public static List<NetworkItemModel> castNetworkListResponseToViewModels(List<NetworkResponse> networkResponseList) {
        List<NetworkItemModel> modelList = new ArrayList<>();
        for (NetworkResponse networkResponse : networkResponseList) {
            modelList.add(new NetworkItemModel(networkResponse.getEncryption(), networkResponse.getSsid()));
        }
        return modelList;
    }

    public static String capFirstLetter(String input) {
        if (input != null && input.length() > 1) {
            return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
        } else if (input != null) {
            return input.toUpperCase();
        }
        return input;
    }

//    public static String createAppriopriateFormat(float cost, boolean shouldDisplayThreeDecimals) {
//        String formatted;
//        if (shouldDisplayThreeDecimals) {
//            formatted = String.format(Locale.getDefault(), "%.3f %s", cost, SmartEssApplication.getInstance().
//                    getUserProfile().getCurrencySymbol());
//            formatted = removeLastDecimalIfNeeded(formatted);
//        } else {
//            formatted = String.format(Locale.getDefault(), "%.2f %s", cost, SmartEssApplication.getInstance().
//                    getUserProfile().getCurrencySymbol());
//        }
//        return formatted;
//    }

//    public static String createNumberWithCurrencyAndOneDecimal(float count) {
//        return String.format(Locale.getDefault(), "%.1f %s", count, SmartEssApplication.getInstance().
//                getUserProfile().getCurrencySymbol());
//    }
//
//    public static String createCostWithCurrencyAndTwoDecimal(float count) {
//        return String.format(Locale.getDefault(), "%.2f %s", count, SmartEssApplication.getInstance().
//                getUserProfile().getCurrencySymbol());
//    }

    public static String createNumberWithOneDecimal(float count) {
        return String.format(Locale.getDefault(), "%.1f", count);
    }

    private static String removeLastDecimalIfNeeded(String formatted) {
        if (formatted.endsWith("0")) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }
        return formatted;
    }

    public static String splitLowerUppercase(String lowerUppercaseText) {
        return lowerUppercaseText.replaceAll(LOWER_UPPER_REGEX, "$1 $2");
    }

    public static int getAppriopriatePopupWidth(boolean isCompareChart) {
        int appriopriateDimenRes = R.dimen.chart_popup_width;
        if (!isCompareChart) {
            if (Locale.getDefault().equals(Locale.GERMANY)) {
                appriopriateDimenRes = R.dimen.chart_popup_germany_width;
            } else if (Locale.getDefault().equals(Locale.UK)) {
                appriopriateDimenRes = R.dimen.chart_popup_uk_width;
            }
        } else {
            if (Locale.getDefault().equals(Locale.GERMANY)) {
                appriopriateDimenRes = R.dimen.chart_popup_germany_average_width;
            } else if (Locale.getDefault().equals(Locale.UK)) {
                appriopriateDimenRes = R.dimen.chart_popup_uk_average_width;
            } else {
                appriopriateDimenRes = R.dimen.chart_popup_average_width;
            }
        }
        return appriopriateDimenRes;
    }

    public static Bitmap getBitmapFromView(View v) {
        v.buildDrawingCache();
        Bitmap tempBitmap = v.getDrawingCache();
        Bitmap b = tempBitmap.copy(Bitmap.Config.ARGB_8888, false);
        v.destroyDrawingCache();

        return b;
    }

    public static void saveBitmapFromView(Context context, View v, SaveBitmapFromViewCallback callback) {
        Bitmap bitmap = getBitmapFromView(v);
        SaveBitmapTask task = new SaveBitmapTask(context, bitmap, callback);
        task.execute();
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getAlbumStorageDir(String fileName) {
        File file = new File(EXTERNAL_SAMBA_DIRECTORY, fileName);
        new File(EXTERNAL_SAMBA_DIRECTORY).mkdirs();

        return file;
    }

    public interface SaveBitmapFromViewCallback {

        void onBitmapSaved(String path, Bitmap bmp);

        void onError();
    }

    public static boolean isCodeInArray(int code, int[] codeTab) {
        for (int c : codeTab) {
            if (code == c) {
                return true;
            }
        }
        return false;
    }

//    public static String getServiceEmailText(Resources resources, int errorCode) {
//        StringBuilder builder = new StringBuilder();
////        builder.append("-----------").append("\n");
////        builder.append("android: ").append(Build.VERSION.RELEASE).append("\n");
////        builder.append(Build.MANUFACTURER + " " + Build.MODEL + " " + Build.DEVICE).append("\n");
//        builder.append(resources.getString(R.string.registration_error_email_text)).append("\n\n");
//        builder.append(resources.getString(R.string.email_useremail) + " " + SmartEssApplication.getInstance().getPreferences().getEmailTempFilledByUser()).append("\n");
//        builder.append(resources.getString(R.string.email_macaddress) + " " + SmartEssApplication.getInstance().getPreferences().getUserMac()).append("\n");
//        builder.append(resources.getString(R.string.email_error_code) + " " + errorCode).append("\n\n");
//        builder.append(resources.getString(R.string.email_thank));
//        return builder.toString();
//    }

    private static class SaveBitmapTask extends AsyncTask<Void, Void, String> {

        private static final String fileName = "challenge_share.png";
        private Bitmap bitmap;
        private SaveBitmapFromViewCallback callback;
        private Context context;

        private SaveBitmapTask(Context context, Bitmap bitmap, SaveBitmapFromViewCallback callback) {
            this.bitmap = bitmap;
            this.callback = callback;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            if (bitmap == null)
                return null;

            String dirPath = context.getCacheDir() + File.separator + "images" + File.separator;
            File fullPathFile = Tools.getAlbumStorageDir(fileName);
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(fullPathFile.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                return fullPathFile.getAbsolutePath();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (callback == null)
                return;

            if (s == null)
                callback.onError();
            else
                callback.onBitmapSaved(s, bitmap);
        }
    }
}
