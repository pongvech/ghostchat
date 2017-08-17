package com.workingghost.ghostchat.controller;

import com.replyyes.facebook.messenger.FacebookMessengerClient;
import com.replyyes.facebook.messenger.bean.Callback;
import com.replyyes.facebook.messenger.bean.Element;
import com.replyyes.facebook.messenger.bean.Entry;
import com.replyyes.facebook.messenger.bean.FacebookMessengerSendException;
import com.replyyes.facebook.messenger.bean.Messaging;
import com.replyyes.facebook.messenger.bean.QuickReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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
                    if (replyText.equalsIgnoreCase("parn")) {
                        client.sendTextMessage(token, senderId, "I love you babe!!! :)");
                    } else if (replyText.equalsIgnoreCase("generics")) {
                        List<Element> elementList = new ArrayList<>();
                        Element flyingZeppelin = new Element();

                        flyingZeppelin.setTitle("What is Flying Zeppelin?");
                        flyingZeppelin.setSubtitle("A Zeppelin was a type of rigid airship named after the German Count " +
                                "Ferdinand von Zeppelin (German pronunciation: [ˈt͡sɛpəliːn]) who pioneered rigid " +
                                "airship development at the beginning of the 20th century.");
                        flyingZeppelin.setImageUrl("https://s3-ap-southeast-1.amazonaws.com/fzpublic/fz1.jpg");
                        flyingZeppelin.setItemUrl("https://en.wikipedia.org/wiki/Zeppelin");

                        Element design = new Element();
                        design.setTitle("Design History");
                        design.setSubtitle("Count Ferdinand von Zeppelin's serious interest in airship development " +
                                "began in 1874, when he took inspiration from a lecture given by Heinrich von Stephan " +
                                "on the subject of \"World Postal Services and Air Travel\" to outline the basic " +
                                "principle of his later craft in a diary entry dated 25 March 1874.");
                        design.setImageUrl("https://s3-ap-southeast-1.amazonaws.com/fzpublic/fz2.jpg");
                        design.setItemUrl("https://en.wikipedia.org/wiki/Zeppelin#History");

                        List<QuickReply> quickReplieList = new ArrayList<>();
                        QuickReply quickReply1 = new QuickReply();
                        quickReply1.setTitle("Quick reply1");
                        quickReply1.setPayload("QUICK_1");
                        QuickReply quickReply2 = new QuickReply();
                        quickReply2.setTitle("Quick reply2");
                        quickReply2.setPayload("QUICK_2");
                        quickReplieList.add(quickReply1);
                        quickReplieList.add(quickReply2);

                        elementList.add(flyingZeppelin);
                        elementList.add(design);
                        client.sendGenericMessage(token, senderId, elementList, quickReplieList);
                    } else {
                        client.sendTextMessage(token, senderId, "You said \""+replyText+"\"? :) Hint: try \"generics\"");
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
