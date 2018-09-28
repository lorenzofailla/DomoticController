package com.apps.lore_f.domoticcontroller.generic.classes;

import java.io.UnsupportedEncodingException;

public class MessageStaticMethods {

    public static byte[] getMessageAsBytesArray(Message message) {

        return String.format("@COMMAND?header=%s&body=%s\n", message.getHeader(), message.getBody()).getBytes();

    }

    public static Message createMessageFromBytesArray(byte[] rawData) {

        Message message = new Message("", "", "");
        String rawString = "";
        try {

            rawString = new String(rawData, "UTF-8");

        } catch (UnsupportedEncodingException e) {

            return message;

        }

        String[] mainLine = rawString.split("[?]");
        if (mainLine.length != 2) {
            return message;
        }

        String[] lines = mainLine[1].split("[&]");

        if (lines.length != 3) {

            return message;

        } else {

            String header = "";
            String body = "";
            String replyto = "";

            for (String l : lines) {

                String[] struct = l.split("[=]");

                if (struct.length != 2) {

                    return message;

                } else {

                    switch (struct[0]) {
                        case "header":
                            header = struct[1];
                            break;
                        case "body":
                            body = struct[1];
                            break;
                        case "replyto":
                            replyto = struct[1];
                            break;

                    }

                }

            }

            return new Message(header, body, replyto);

        }

    }


}
