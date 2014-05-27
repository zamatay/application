package ru.vkb.model.service;

import com.foxykeep.datadroid.service.RequestService;

import ru.vkb.model.RequestFactory;
import ru.vkb.model.operations.BaseOperations;
import ru.vkb.model.operations.dbDisposalNotifyOperation;
import ru.vkb.model.operations.dbOperations;


/**
 * Created by Zamuraev_av on 26.02.14.
 */
public class RestService extends RequestService {

    @Override
    public Operation getOperationForType(int requestType) {
        switch (requestType){
            case -1:
            case RequestFactory.REQUEST_LOGIN:
            case RequestFactory.REQUEST_SEND_COMMENT: return new BaseOperations();
            case RequestFactory.REQUEST_DISPOSAL_NOTE: return new dbDisposalNotifyOperation();
            default: return new dbOperations();
        }
    }

}