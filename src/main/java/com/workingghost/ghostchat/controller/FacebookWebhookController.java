package com.workingghost.ghostchat.controller;

import com.replyyes.facebook.messenger.FacebookMessengerClient;
import com.replyyes.facebook.messenger.bean.Callback;
import com.replyyes.facebook.messenger.bean.Entry;
import com.replyyes.facebook.messenger.bean.FacebookMessengerSendException;
import com.replyyes.facebook.messenger.bean.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class FacebookWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(FacebookWebhookController.class);

    @Value("${facebook_page_token}")
    private String token;
    @Value("${facebook_app_secret_key}")
    private String secretKey;

    @RequestMapping(path = "/webhook", method = RequestMethod.GET)
    public String defaultWebhook(@RequestParam(value = "hub.challenge") String challenge){
        logger.debug("Got webhook request!");
        logger.debug(challenge);
        return challenge;
    }

    @RequestMapping(path = "/getstartbutton", method = RequestMethod.POST)
    public String setGetStartButton(@RequestBody String request) {
        RestTemplate restTemplate = new RestTemplate();
        logger.debug("Create GetStarted with {}", request);
        return restTemplate.postForObject("https://graph.facebook.com/v2.6/me/messenger_profile?access_token="+token,
                request, String.class);
    }

    @RequestMapping(path = "/persistentmenu", method = RequestMethod.POST)
    public String setPersistentMenu(@RequestBody String request) {
        RestTemplate restTemplate = new RestTemplate();
        logger.debug("Create Persistent Menu with {}", request);
        return restTemplate.postForObject("https://graph.facebook.com/v2.6/me/messenger_profile?access_token="+token,
                request, String.class);
    }

    @RequestMapping(path = "/webhook", method = RequestMethod.POST)
    public String postWebhook(@RequestBody String request,
                              @RequestHeader("X-Hub-Signature") String signature) throws FacebookMessengerSendException {
        logger.debug("Got webhook request!");
        logger.debug("X-Hub-Signature = {}", signature);
        logger.debug(request);

        FacebookMessengerClient client = new FacebookMessengerClient();
        if (client.isValidRequest(secretKey, signature, request)) {
            logger.debug("Request is valid!");
        } else {
            logger.error("Request is invalid!");
            return null;
        }

        Callback callback = client.deserializeCallback(request);
        String senderId;
        String replyText;

        logger.debug("Object = {}", callback.getObject());
        logger.debug("Found {} entry elements", callback.getEntry().size());
        for (Entry entry : callback.getEntry()) {
            logger.debug("Found {} messaging elements", entry.getMessaging().size());
            for (Messaging messaging : entry.getMessaging()) {
                senderId = messaging.getSender().getId();
                if (messaging.getMessage() != null) {
                    replyText = messaging.getMessage().getText();
                    if (replyText.equals("parn")) {
                        client.sendTextMessage(token, senderId, "I love you babe!!! :)");
                    } else {
                        client.sendTextMessage(token, senderId, "You said \""+replyText+"\"? :)");
                    }
                } else if (messaging.getPostback() != null && messaging.getPostback().getPayload().equals("GET_STARTED_PAYLOAD")) {
                    replyText = "Greeting! Welcome to Flying Zeppelin!";
                    client.sendTextMessage(token, senderId, ""+replyText);
                }
            }
        }
        return request;
    }
}
