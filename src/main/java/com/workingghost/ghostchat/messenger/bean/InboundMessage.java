package com.workingghost.ghostchat.messenger.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * InboundMessages are a type of event that can be received in a {@link Callback} @{link Entry}. They represent
 * text messages that are sent by users.
 *
 * https://developers.facebook.com/docs/messenger-platform/webhook-reference/message-received
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InboundMessage {
    private String mid;
    private Long seq;
    private String text;

    private List<Attachment> attachments;

    @JsonProperty("quick_reply")
    private InboundPayload quickReply;
}
