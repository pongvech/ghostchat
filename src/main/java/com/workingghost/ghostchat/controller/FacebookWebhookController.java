package com.workingghost.ghostchat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.workingghost.ghostchat.messenger.FacebookMessengerClient;
import com.workingghost.ghostchat.messenger.bean.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
public class FacebookWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(FacebookWebhookController.class);

    @Autowired
    private FacebookMessengerClient client;

    private static final String SENDER_ACTION_TYPING_ON = "typing_on";
    private static final String SENDER_ACTION_MARK_SEEN = "mark_seen";
    private static final String SENDER_ACTION_TYPING_OFF = "typing_off";

    private static final String BUTTON_TYPE_WEB_URL = "web_url";
    private static final String BUTTON_TYPE_POSTBACK = "postback";
    private static final String BUTTON_TYPE_PHONE_NUMBER = "phone_number";
    private static final String BUTTON_TYPE_SHARE = "element_share";

    private static final String QUICKREP_CONTENT_TYPE_TEXT = "text";
    private static final String QUICKREP_CONTENT_TYPE_LOC = "location";

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

    @RequestMapping(path = "/sendmessage/{psid}", method = RequestMethod.POST)
    public String sendMessage(@RequestBody String request, @PathVariable String psid) throws FacebookMessengerSendException {
        FacebookMessengerClient client = new FacebookMessengerClient();
        MessageResponse response = client.sendTextMessage(token, psid, request);
        return response.toString();
    }

    @RequestMapping(path = "/webhook", method = RequestMethod.POST)
    public String postWebhook(@RequestBody String request,
                              @RequestHeader("X-Hub-Signature") String signature) throws FacebookMessengerSendException, JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        logger.debug("Got webhook request!");
        logger.debug("X-Hub-Signature = {}", signature);
        logger.debug(request);

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

                client.sendSenderAction(token,senderId,SENDER_ACTION_MARK_SEEN);
                client.sendSenderAction(token,senderId,SENDER_ACTION_TYPING_ON);

                if (messaging.getMessage() != null) {
                    replyText = messaging.getMessage().getText();
                    if (replyText != null) {
                        if (replyText.equalsIgnoreCase("parn")) {
                            client.sendSenderAction(token,senderId,SENDER_ACTION_TYPING_OFF);
                            client.sendTextMessage(token, senderId, "I love you babe!!! :)");
                        } else if (replyText.equalsIgnoreCase("generics")) {
                            List<Element> elementList = new ArrayList<>();
                            Element flyingZeppelin = new Element();

                            List<Button> buttonList = new ArrayList<>();
                            Button button = new Button();
                            button.setType(BUTTON_TYPE_SHARE);
                            buttonList.add(button);
                            Button callButton = new Button();
                            callButton.setType(BUTTON_TYPE_PHONE_NUMBER);
                            callButton.setTitle("Call developer");
                            callButton.setPayload("+66844490505");
                            buttonList.add(callButton);

                            flyingZeppelin.setTitle("What is Flying Zeppelin?");
                            flyingZeppelin.setSubtitle("A Zeppelin was a type of rigid airship named after the German Count " +
                                    "Ferdinand von Zeppelin (German pronunciation: [ˈt͡sɛpəliːn]) who pioneered rigid " +
                                    "airship development at the beginning of the 20th century.");
                            flyingZeppelin.setImageUrl("https://s3-ap-southeast-1.amazonaws.com/fzpublic/fz1.jpg");
                            flyingZeppelin.setItemUrl("https://en.wikipedia.org/wiki/Zeppelin");
                            flyingZeppelin.setButtons(buttonList);

                            Element design = new Element();
                            design.setTitle("Design History");
                            design.setSubtitle("Count Ferdinand von Zeppelin's serious interest in airship development " +
                                    "began in 1874, when he took inspiration from a lecture given by Heinrich von Stephan " +
                                    "on the subject of \"World Postal Services and Air Travel\" to outline the basic " +
                                    "principle of his later craft in a diary entry dated 25 March 1874.");
                            design.setImageUrl("https://s3-ap-southeast-1.amazonaws.com/fzpublic/fz2.jpg");
                            design.setItemUrl("https://en.wikipedia.org/wiki/Zeppelin#History");
                            design.setButtons(buttonList);

                            Element hindenburg = new Element();
                            hindenburg.setTitle("Hindenburg disaster");
                            hindenburg.setSubtitle("The Hindenburg disaster occurred on May 6, 1937.");
                            hindenburg.setImageUrl("https://s3-ap-southeast-1.amazonaws.com/fzpublic/fz3.jpg");
                            hindenburg.setItemUrl("https://en.wikipedia.org/wiki/Hindenburg_disaster");
                            hindenburg.setButtons(buttonList);

                            List<QuickReply> quickReplieList = new ArrayList<>();
                            QuickReply quickReply1 = new QuickReply();
                            quickReply1.setContentType(QUICKREP_CONTENT_TYPE_LOC);
                            QuickReply quickReply2 = new QuickReply();
                            quickReply2.setTitle("What is my PSID?");
                            quickReply2.setPayload("PSID");
                            quickReplieList.add(quickReply1);
                            quickReplieList.add(quickReply2);

                            elementList.add(flyingZeppelin);
                            elementList.add(design);
                            elementList.add(hindenburg);
                            client.sendSenderAction(token,senderId,SENDER_ACTION_TYPING_OFF);
                            client.sendGenericMessage(token, senderId, elementList, quickReplieList);
                        } else {
                            client.sendSenderAction(token,senderId,SENDER_ACTION_TYPING_OFF);
                            client.sendTextMessage(token, senderId, "You said \""+replyText+"\"? :) Hint: try \"generics\"");
                        }
                    }
                    if (messaging.getMessage().getQuickReply() != null
                            && messaging.getMessage().getQuickReply().getPayload().equalsIgnoreCase("PSID")) {
                        client.sendSenderAction(token,senderId,SENDER_ACTION_TYPING_OFF);
                        replyText = "Your PSID is" + senderId;
                        client.sendTextMessage(token, senderId, ""+replyText);
                    }
                    if (messaging.getMessage().getAttachments() != null) {
                        for (Attachment attachment : messaging.getMessage().getAttachments()) {
                            if (attachment.getType().equalsIgnoreCase("location")) {
                                client.sendSenderAction(token,senderId,SENDER_ACTION_TYPING_OFF);
                                client.sendTextMessage(token, senderId, "Ohh! You sharing me a location!");
                            }
                        }
                    }

                } else if (messaging.getPostback() != null && messaging.getPostback().getPayload().equals("GET_STARTED_PAYLOAD")) {
                    replyText = "Greeting! Welcome to Flying Zeppelin! Your PSID is " + senderId;
                    client.sendSenderAction(token,senderId,SENDER_ACTION_TYPING_OFF);
                    client.sendTextMessage(token, senderId, ""+replyText);
                }
            }
        }
        return request;
    }
}
