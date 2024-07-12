package com.daon.fido.sdk.sample;

import android.content.Context;
import android.util.Log;

import com.daon.fido.sdk.sample.exception.CommunicationsException;
import com.daon.fido.sdk.sample.exception.ServerError;
import com.daon.fido.sdk.sample.model.Error;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HTTP {
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int READ_TIMEOUT = 20000;
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE = "application/json";
    private static final String SESSION_IDENTIFIER_HEADER = "Session-Id";

    private static final String POST_METHOD = "POST";
    private static final String GET_METHOD = "GET";
    private static final String DELETE_METHOD = "DELETE";
    private GsonBuilder builder;
    private Context mContext;

    public class HttpResponse {
        private final String payload;
        private final int httpStatusCode;

        HttpResponse(String payload, int httpStatusCode) {
            this.payload = payload;
            this.httpStatusCode = httpStatusCode;
        }

        public String getPayload() {
            return this.payload;
        }

        public int getHttpStatusCode() {
            return this.httpStatusCode;
        }
    }

    public HTTP(Context context) {
        builder = new GsonBuilder();
        mContext = context;
    }

    protected Context getContext() {
        return this.mContext;
    }

    public <T> T get(String resource, String id, Class<T> clazz) {
        return this.get(resource + "/" + id, clazz);
    }

    public <T> T get(String resource, Class<T> clazz) {
        HttpResponse response = get(resource);
        if (response.getHttpStatusCode() == HttpURLConnection.HTTP_CREATED || response.getHttpStatusCode() == HttpURLConnection.HTTP_OK) {
            Gson outputGson = builder.create();
            Log.d(CoreApplication.TAG, "get response:" + response.getPayload());
            return outputGson.fromJson(response.getPayload(), clazz);
        } else {
            Gson outputGson = builder.create();
            Error error;
            try {
                error = outputGson.fromJson(response.getPayload(), Error.class);
            } catch (Exception e) {
                Log.e("SampleRpApp", "Server communications error. HttpResponse code: " + response.getHttpStatusCode());
                error = new Error(-4, "Server communications error. See client ADB logs for more detail.");
            }
            throw new ServerError(error);
        }
    }

    protected HttpResponse get(String relativeUrl) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = createConnection(relativeUrl, GET_METHOD, false);

            int httpResult = urlConnection.getResponseCode();
            Log.d(CoreApplication.TAG, "RPSAService get httpResult:" + httpResult);
            String response;
            if (httpResult == HttpURLConnection.HTTP_CREATED || httpResult == HttpURLConnection.HTTP_OK) {
                response = this.readStream(urlConnection.getInputStream());
            } else if (httpResult == HttpURLConnection.HTTP_UNAVAILABLE) {
                throw new IOException("The server connection is unavailable.");
            } else {
                response = this.readStream(urlConnection.getErrorStream());
            }

            return new HttpResponse(response, httpResult);
        } catch (MalformedURLException e) {
            Error error = new Error();
            error.setCode(-1);
            error.setMessage("Unable to connect to the server - likely a programming error");
            throw new CommunicationsException(error);
        } catch (IOException e) {
            Error error = new Error();
            error.setCode(-2);
            error.setMessage("Unable to connect to the server.\nCheck the internet connection and server URL.");
            throw new CommunicationsException(error);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    protected <T> T post(String resource, Object object, Class<T> clazz) {

        Gson inputGson = builder.create();
        String payload = inputGson.toJson(object);
        HttpResponse response = this.post(resource, payload);
        if (response.getHttpStatusCode() == HttpURLConnection.HTTP_CREATED || response.getHttpStatusCode() == HttpURLConnection.HTTP_OK) {
            Gson outputGson = builder.create();
            Log.d(CoreApplication.TAG, "post response:" + response.getPayload());
            return outputGson.fromJson(response.getPayload(), clazz);
        } else {
            Gson outputGson = builder.create();
            Error error = null;
            try {
                Log.d(CoreApplication.TAG, "post error response :" + response.getPayload());
                error = outputGson.fromJson(response.getPayload(), Error.class);
            } catch (Exception e) {
                Log.e("SampleRpApp", "Server communications error. HttpResponse code: " + response.getHttpStatusCode());
                error = new Error(-4, "Server communications error. See client ADB logs for more detail.");
            }
            throw new ServerError(error);
        }
    }

    protected HttpResponse post(String relativeUrl, String payload) {

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = this.createConnection(relativeUrl, POST_METHOD, true);
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(payload);
            out.close();

            Log.d(CoreApplication.TAG, "**POST**");
            Log.d(CoreApplication.TAG, "URL: " + urlConnection.getURL().toString());
            Log.d(CoreApplication.TAG, "Payload: " + payload);

            int httpResult = urlConnection.getResponseCode();
            Log.d(CoreApplication.TAG, "HTTP result: " + urlConnection.getResponseCode());
            String response;
            if (httpResult == HttpURLConnection.HTTP_CREATED || httpResult == HttpURLConnection.HTTP_OK) {
                response = this.readStream(urlConnection.getInputStream());
            } else {
                response = this.readStream(urlConnection.getErrorStream());
            }

            Log.d(CoreApplication.TAG, "Response: " + response);

            return new HttpResponse(response, httpResult);

        } catch (MalformedURLException e) {
            Error error = new Error();
            error.setCode(-1);
            error.setMessage("Unable to connect to the server - likely a programming error");
            throw new CommunicationsException(error);
        } catch (IOException e) {
            Error error = new Error();
            error.setCode(-2);
            error.setMessage("Unable to connect to the server.\nCheck the internet connection and server URL");
            throw new CommunicationsException(error);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    protected String deleteResource(String resource, String resourceId, boolean withOutput) {

        HttpResponse response = this.delete(resource, resourceId, withOutput);
        if (response.getHttpStatusCode() == HttpURLConnection.HTTP_OK) {
            if (withOutput) {
                return response.getPayload();
            } else {
                return null;
            }
        } else {
            Gson outputGson = builder.create();
            Error error;
            try {
                error = outputGson.fromJson(response.getPayload(), Error.class);
            } catch (Exception e) {
                Log.e("SampleRpApp", "Server communications error. HttpResponse code: " + response.getHttpStatusCode());
                error = new Error(-4, "Server communications error. See client ADB logs for more detail.");
            }
            throw new ServerError(error);
        }
    }

    protected <T> T deleteResource(String resource, String resourceId, Class<T> clazz) {

        HttpResponse response = this.delete(resource, resourceId, true);
        if (response.getHttpStatusCode() == HttpURLConnection.HTTP_OK) {
            Gson outputGson = builder.create();
            return outputGson.fromJson(response.getPayload(), clazz);
        } else {
            Gson outputGson = builder.create();
            Error error;
            try {
                error = outputGson.fromJson(response.getPayload(), Error.class);
            } catch (Exception e) {
                error = new Error(-4, "Unknown server error");
            }
            throw new ServerError(error);
        }
    }

    protected HttpResponse delete(String relativeUrl, String id, boolean withOutput) {
        return this.delete(relativeUrl + "/" + id, withOutput);
    }

    protected HttpResponse delete(String relativeUrl, boolean withOutput) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = this.createConnection(relativeUrl, DELETE_METHOD, false);
            int httpResult = urlConnection.getResponseCode();
            String response = null;
            if (httpResult == HttpURLConnection.HTTP_OK) {
                if (withOutput) {
                    response = this.readStream(urlConnection.getInputStream());
                }
                return new HttpResponse(response, httpResult);
            } else {
                response = this.readStream(urlConnection.getErrorStream());
                return new HttpResponse(response, httpResult);
            }

        } catch (MalformedURLException e) {

            Error error = new Error();
            error.setCode(-1);
            error.setMessage("Unable to connect to the server - likely a programming error");
            throw new CommunicationsException(error);
        } catch (IOException e) {

            Error error = new Error();
            error.setCode(-2);
            error.setMessage("Unable to connect to the server.\nCheck the internet connection and server URL");
            throw new CommunicationsException(error);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    protected HttpURLConnection createConnection(String relativeUrl, String method, boolean output) throws IOException {
        Log.d(CoreApplication.TAG, "createConnection URL:" + getAbsoluteUrl(relativeUrl) + " method:" + method);
        URL url = new URL(getAbsoluteUrl(relativeUrl));
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(output);
        httpURLConnection.setRequestMethod(method);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        httpURLConnection.setReadTimeout(READ_TIMEOUT);
        httpURLConnection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE);
        //This is required for CreateRegRequest
        Log.d(CoreApplication.TAG, "createConnection sessionId :" + ((CoreApplication) mContext.getApplicationContext()).getSessionId());
        if (((CoreApplication) mContext.getApplicationContext()).getSessionId() != null)
            httpURLConnection.setRequestProperty(SESSION_IDENTIFIER_HEADER, ((CoreApplication) mContext.getApplicationContext()).getSessionId());
        httpURLConnection.connect();
        return httpURLConnection;
    }

    protected String readStream(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    protected String getAbsoluteUrl(String relativeUrl) throws MalformedURLException {
        return getBaseUrl() + relativeUrl;
    }

    protected String getBaseUrl() throws MalformedURLException {

        String serverUrl = UserPreferences.instance().getString(SettingsActivity.PREF_SERVER_URL, "qc35.identityx-dev.com");
        String port = UserPreferences.instance().getString(SettingsActivity.PREF_SERVER_PORT, "8080");
        String scheme = UserPreferences.instance().getBoolean(SettingsActivity.PREF_SERVER_SECURE, false) ? "https" : "http";
        URL url = new URL(scheme + "://" + serverUrl);
        return scheme + "://" + url.getHost() + ":" + port + url.getPath() + "/";
    }


}
