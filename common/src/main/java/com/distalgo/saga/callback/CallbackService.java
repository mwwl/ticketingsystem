package com.distalgo.saga.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// to deleteee
@Service
public class CallbackService {
    @Autowired
    private CallbackRepo callbackRepo;

    public void storeCallbackURL(String sessionID, String clientIP, String clientPort) {
        System.out.println("in service, storing url");
        String callbackURL = createCallbackURL(sessionID, clientIP, clientPort);
        CallbackEntity savedCallback = callbackRepo.save(new CallbackEntity(sessionID, callbackURL));
        System.out.println("saved url");
    }

    public String getCallbackURL(String sessionID) {
        System.out.println("retrieving callbackurl, all: " + callbackRepo.findAll());
        return callbackRepo.findById(sessionID)
                .map(CallbackEntity::getCallbackURL)
                .orElse(null);
    }

    private String createCallbackURL(String sessionID, String clientIP, String clientPort) {
        return String.format("http://%s:%s/callback/%s", clientIP, clientPort, sessionID);
    }
}