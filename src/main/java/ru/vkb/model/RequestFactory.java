package ru.vkb.model;

import com.foxykeep.datadroid.requestmanager.Request;

import ru.vkb.common.utils;

/**
 * Created by Zamuraev_av on 26.02.14.
 */
public class RequestFactory {
    public static final int REQUEST_NO_ID = -1;
    public static final int REQUEST_LOGIN = 0;
    public static final int REQUEST_DISPOSAL_LIST = 1;
    public static final int REQUEST_DISPOSAL_NOTE = 2;
    public static final int REQUEST_SEND_COMMENT = 3;
    public static final int REQUEST_STAFF = 4;
    public static final String method_name = "method";
    public static String HOST;
    private static String mLogin;
    private static String mPassword;

    public static void setSessionID(String sessionID) {
        SessionID = sessionID;
    }

    protected static String SessionID;


    public static String getSessionID() {
        return SessionID;
    }

    public static void setPassword(Request request){
        request.put("login", mLogin);
        request.put("password", mPassword);
        if (SessionID != null)
            request.put("Pragma: dssession", getSessionID());
    }

    public static Request getDisposalList() {
        Request request = getRequest(REQUEST_DISPOSAL_LIST, null, "getDisposalList", true);
        return request;
    }

    public static Request getLogin(String login, String password, String host) {
        mLogin = login;
        mPassword = password;
        HOST = host;
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

    public static void setClearFilterFlag(Request request){
        request.put("clearFilter", true);
    }

    public static Boolean isClearFilterFlag(Request request){
        return request != null ? request.getBoolean("clearFilter") : false;
    }

    public static void setUpdateFlag(Request request){
        request.put("withUpdate", true);
    }

    public static Boolean isUpdateFlag(Request request){
        return request != null ? request.getBoolean("withUpdate") : false;
    }

    public static void setResyncFlag(Request request){
        request.put("resync", true);
    }

    public static Boolean isResyncFlag(Request request){
        return request != null ? request.getBoolean("resync") : false;
    }

    public static void setNotDeleteFlag(Request request){
        request.put("notDeleted", true);
    }

    public static Boolean isNotDeleteFlag(Request request){
        return request != null ? request.getBoolean("notDeleted") : false;
    }

}
