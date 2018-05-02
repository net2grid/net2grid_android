package nl.wittig.net2grid_ble.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import nl.wittig.net2grid_ble.BuildConfig;
import nl.wittig.net2grid_ble.helpers.PersistentHelper;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {

    private static Api api;

    public MeterApiService meter;
    public MedaApiService meda;
    private OkHttpClient client;

    final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };

    public static Api getDefaultInstance() {

        if (api == null) {

            String ip = PersistentHelper.getSmartBridgeHost();

            if (ip == null) {
                return null;
            }

            api = new Api(ip);

            return api;
        }
        else {

            return api;
        }
    }

    public static void resetApi() {

        api = null;
    }

    public Api(String baseUrl) {
        Gson gson = new GsonBuilder()
                .create();

        client = getOkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + baseUrl + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        meter = retrofit.create(MeterApiService.class);
        meda = retrofit.create(MedaApiService.class);
    }

    private SSLContext provideSSlContext() {
        SSLContext ssl = null;
        try {
            ssl = SSLContext.getInstance("SSL");
            ssl.init(null, trustAllCerts, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssl;
    }

    private OkHttpClient getOkHttpClient() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .sslSocketFactory(provideSSlContext().getSocketFactory(), (X509TrustManager)trustAllCerts[0])
                .connectTimeout(4, TimeUnit.SECONDS)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                });

        if (BuildConfig.DEBUG) {

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            builder.addInterceptor(interceptor);
        }

        client = builder.build();

        return client;
    }
}
