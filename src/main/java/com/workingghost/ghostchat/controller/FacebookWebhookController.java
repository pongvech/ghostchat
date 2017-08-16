package com.workingghost.ghostchat.controller;

import com.replyyes.facebook.messenger.FacebookMessengerClient;
import com.replyyes.facebook.messenger.bean.Callback;
import com.replyyes.facebook.messenger.bean.Entry;
import com.replyyes.facebook.messenger.bean.FacebookMessengerSendException;
import com.replyyes.facebook.messenger.bean.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class FacebookWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(FacebookWebhookController.class);
    String flyingZeppelinToken = "EAAEhgfZBZB910BABQMJB1mz53s5L0dLoTboOQYxsW639aeFNZAQu6M6oDzCiwYyyAb6GvQPhpA5JFdOqgkpCjDgKfXd7xZBrzg3CaYL8CwEQiUOwvBpKzd2odwaD4tQd7cVl6VlT0xuNKNvJEaRZB8Cxn1PeXVZCiqfkX4rX0IhgZDZD";

    @RequestMapping(path = "/webhook", method = RequestMethod.GET)
    public String defaultWebhook(@RequestParam(value = "hub.challenge") String challenge){
        logger.debug("Got webhook request!");
        logger.debug(challenge);
        return challenge;
    }

    @RequestMapping(path = "/webhook", method = RequestMethod.POST)
    public String postWebhook(@RequestBody String request){
        logger.debug("Got webhook request!");
        logger.debug(request);

        FacebookMessengerClient client = new FacebookMessengerClient();
        Callback callback = client.deserializeCallback(request);
        String senderId = null;
        String replyText = null;

        logger.debug("Object = {}", callback.getObject());
        logger.debug("Found {} entry elements", callback.getEntry().size());
        for (Entry entry : callback.getEntry()) {
            logger.debug("id = {}", entry.getId());
            logger.debug("time = {}", entry.getTime());
            logger.debug("Found {} messaging elements", entry.getMessaging().size());
            for (Messaging messaging : entry.getMessaging()) {
                logger.debug("sender = {}", messaging.getSender().getId());
                logger.debug("recipient = {}", messaging.getRecipient().getId());
                logger.debug("timestamp = {}", messaging.getTimestamp());
                logger.debug("mid = {}", messaging.getMessage().getMid());
                logger.debug("sec = {}", messaging.getMessage().getSeq());
                logger.debug("text = {}", messaging.getMessage().getText());
                senderId = messaging.getSender().getId();
                replyText = messaging.getMessage().getText();
            }
        }
        if (senderId != null && replyText != null) {
            try {
                if (replyText.equals("parn")) {
                    client.sendTextMessage(flyingZeppelinToken, senderId, "I love you :)");
                }
                client.sendTextMessage(flyingZeppelinToken, senderId, replyText);
            } catch (FacebookMessengerSendException e) {
                logger.error("Something went wrong!!");
                e.printStackTrace();
            }
        } else {
            logger.error("Either senderId or replyText is NULL!!!");
        }
        return request;
    }
}
