package org.nure.julia.rest;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;

public class RestClient {
    private static final String BASE_URL = "https://julia-gateway.herokuapp.com";

    private static final AsyncHttpClient CLIENT = new AsyncHttpClient();

    public static void get(Context context, String url, Header[] headers, RequestParams params,
                           AsyncHttpResponseHandler responseHandler) {

        CLIENT.get(context, getAbsoluteUrl(url), headers, params, responseHandler);
    }

    public static void post(Context context, String url, Header[] headers, HttpEntity httpEntity,
                            String contentType, AsyncHttpResponseHandler responseHandler) {

        CLIENT.post(context, getAbsoluteUrl(url), headers, httpEntity, contentType, responseHandler);
    }

    public static void put(Context context, String url, Header[] headers, HttpEntity httpEntity,
                           String contentType, AsyncHttpResponseHandler responseHandler) {

        CLIENT.put(context, getAbsoluteUrl(url), headers, httpEntity, contentType, responseHandler);
    }

    public static void patch(Context context, String url, Header[] headers, HttpEntity httpEntity,
                             String contentType, AsyncHttpResponseHandler responseHandler) {

        CLIENT.patch(context, getAbsoluteUrl(url), headers, httpEntity, contentType, responseHandler);
    }

    public static void delete(Context context, String url, Header[] headers, RequestParams params,
                              AsyncHttpResponseHandler responseHandler) {

        CLIENT.get(context, getAbsoluteUrl(url), headers, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
