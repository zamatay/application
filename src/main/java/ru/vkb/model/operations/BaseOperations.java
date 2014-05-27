package ru.vkb.model.operations;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.vkb.model.RequestFactory;
import ru.vkb.model.provider.Contract;

import static com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;

/**
 * Created by Zamuraev_av on 26.02.14.
 */


public class BaseOperations implements RequestService.Operation {
    private String _url = "test1";
    //private String _url = "http://test3:8081";
    private String _extended = "/datasnap/rest/TSMethods/";
    protected Context mContext;
    protected Request mRequest;

    protected String get_url(){
        String result = PreferenceManager.getDefaultSharedPreferences(mContext).getString("text_host", "");
        if (result.length() == 0)
          return _url + _extended;
        else
          return result + _extended;
    }

    protected String getMethod(Request request){
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        sb.append(request.getString(RequestFactory.method_name));
        sb.append("\"");
        return sb.toString();
    }

    protected void getParam(Request request, NetworkConnection connection){
        if (request.contains("param_count") && request.getInt("param_count") == 0)
                return;
        if (!request.contains("param_Names"))
            return;

        String[] paramNames = request.getString("param_Names").split(",");
        JSONObject jo = new JSONObject();
        for (Integer i = 0; i < paramNames.length; i++){
            String paramName = paramNames[i];
            if (!request.contains(paramName))
                continue;
            Object paramValue = getParamValue(request, paramName);

            try {
                jo.put(paramName, paramValue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        connection.setPostText(jo.toString());
        connection.setMethod(NetworkConnection.Method.POST);
    }

    private Object getParamValue(Request request, String paramName) {
        final int TYPE_BOOLEAN = 1;
        final int TYPE_BYTE = 2;
        final int TYPE_CHAR = 3;
        final int TYPE_SHORT = 4;
        final int TYPE_INT = 5;
        final int TYPE_LONG = 6;
        final int TYPE_FLOAT = 7;
        final int TYPE_DOUBLE = 8;
        final int TYPE_STRING = 9;
        final int TYPE_CHARSEQUENCE = 10;
        final int TYPE_PARCELABLE = 11;

        switch (request.getType(paramName)){
            case TYPE_STRING: return checkIfArray(request.getString(paramName));
            case TYPE_INT: return request.getIntAsString(paramName);
            case TYPE_BOOLEAN: return request.getBoolean(paramName);
            case TYPE_BYTE: return request.getByte(paramName);
            case TYPE_CHAR: return request.getChar(paramName);
            case TYPE_SHORT: return request.getShort(paramName);
            case TYPE_LONG: return request.getLong(paramName);
            case TYPE_FLOAT: return request.getFloat(paramName);
            case TYPE_DOUBLE: return request.getDouble(paramName);
            case TYPE_CHARSEQUENCE: return request.getCharSequence(paramName);
            case TYPE_PARCELABLE: return request.getParcelable(paramName);
            default: return request.getString(paramName);
        }
    }

    private Object checkIfArray(String value) {
        if (value == null){
            return null;
        } else if (value.startsWith("[") & value.endsWith("]")){
            value = value.substring(1, value.length() - 1);
            String[] arStr = value.split(",");
            JSONArray ar = new JSONArray();
            for (String s : arStr){
                ar.put(s);
            }
            return ar;
        } else {
            return value;
        }
    }

    protected ConnectionResult sendRequest(Request request){
        // непосредственно сам запрос
        StringBuilder url = new StringBuilder(get_url());
        url.append(getMethod(request));
        NetworkConnection connection = new NetworkConnection(mContext, url.toString());
        getParam(request, connection);
        //url.append();
        if (request.contains("login"))
            connection.setCredentials(new UsernamePasswordCredentials(request.getString("login"), request.getString("password")));
        try {
            return connection.execute();
        } catch (ConnectionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Bundle execute(Context context, Request request) throws DataException{
        mContext = context;
        mRequest = request;
        // получаем результат запроса
        ConnectionResult result = sendRequest(request);

        try {
            return getResult(getContentValues(result));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public Bundle getResult(JSONObject object) throws JSONException {
        // формируем ответ
        JSONArray data = object.getJSONArray("result");
        Bundle result = new Bundle();
        result.putString("result", data.getString(0));
        return result;
    }

    public JSONObject getContentValues(ConnectionResult сonnectionResult) throws JSONException, DataException {
        JSONObject result = new JSONObject(сonnectionResult.body);
        return result;
    }

    protected Uri getContentUri() {
        switch (mRequest.getRequestType()) {
            case RequestFactory.REQUEST_DISPOSAL_LIST:
                return Contract.disposals.CONTENT_URI;
            case RequestFactory.REQUEST_DISPOSAL_NOTE:
                return Contract.disposal_comment.CONTENT_URI;
        }
        return null;


    }
}
