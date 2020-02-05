package com.apps.lore_f.domoticcontroller.generic.classes;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lore_f on 20/08/2017.
 */

public class MessageStructure {

    private String header;
    private String body;
    private String replyto;

    // required empty constructor
    public MessageStructure(){}

    public MessageStructure(String header, String body, String replyto) {

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

    public String getMessageAsJSONString(){

        try {
            JSONObject result = new JSONObject();
            result.put("header", header);
            result.put("body", body);
            result.put("replyto",replyto);

            return result.toString();

        } catch (JSONException e){

        }

        return null;
    }

}
