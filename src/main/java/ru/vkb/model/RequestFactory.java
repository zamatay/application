package ru.vkb.model;

import com.foxykeep.datadroid.requestmanager.Request;

import ru.vkb.common.utils;

/**
 * Created by Zamuraev_av on 26.02.14.
 */
public class RequestFactory {
    public static final int REQUEST_LOGIN = 0;
    public static final int REQUEST_DISPOSAL_LIST = 1;
    public static final int REQUEST_DISPOSAL_NOTE = 2;
    public static final int REQUEST_SEND_COMMENT = 3;
    public static final String method_name = "method";
    private static String mLogin;
    private static String mPassword;
    public static Integer StaffID;

    public static void setPassword(Request request){
        request.put("login", mLogin);
        request.put("password", mPassword);
    }

    public static Request getDisposalList() {
        Request request = getRequest(REQUEST_DISPOSAL_LIST, null, "getDisposalList", true);
        return request;
    }

    public static Request getLogin(String login, String password) {
        mLogin = login;
        mPassword = password;
        Request request = getRequest(REQUEST_LOGIN, null, "Login", true);
        return request;
    }

    public static Request getDisposalNote(Integer disposalID) {
        Request request = getRequest(REQUEST_DISPOSAL_NOTE, new String[]{"disposal_id"}, "getDisposalNotes", true);
        request.put("disposal_id", disposalID);
        return request;
    }

    public static Request getRequestByParam(Integer id, String method, String[] params, String[] values) {
        Request request = getRequest(id, params, method, true);
        for (Integer i = 0; i < request.getInt("param_count"); i++){
            request.put(params[i], values[i]);
        }
        return request;
    }

    public static Request getRequestByParam(Integer id, String method, String[] params, String[] values, String[] extParamName, String[] extParamArg) {
        Request request = getRequestByParam(id, method, params, values);
        for (int i=0; i<extParamName.length; i++){
            request.put(extParamName[i], extParamArg[i]);
        }
        return request;
    }

    public static Request getRequest(Integer id, String[] params, String method, Boolean withPass){
        Request request = new Request(id);
        if (params!=null) {
            request.put("param_Names", utils.arrayToString(params));
            request.put("param_count", params.length);
        } else
            request.put("param_count", 0);
        request.put(method_name, method);
        if (withPass)
            setPassword(request);
        return  request;
    }

}
