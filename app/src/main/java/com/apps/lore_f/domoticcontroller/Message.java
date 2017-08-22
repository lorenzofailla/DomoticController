package com.apps.lore_f.domoticcontroller;

/**
 * Created by lore_f on 20/08/2017.
 */

public class Message {

    private String header;
    private String body;
    private String replyto;


    public Message() {

    }

    public Message(String header, String body, String replyto) {

        this.header = header;
        this.body = body;
        this.replyto = replyto;

    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public String getReplyto() {
        return replyto;
    }

}
