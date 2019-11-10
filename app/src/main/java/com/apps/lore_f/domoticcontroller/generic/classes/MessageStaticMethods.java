package com.apps.lore_f.domoticcontroller.generic.classes;

import java.io.UnsupportedEncodingException;

public class MessageStaticMethods {

    public static byte[] getMessageAsBytesArray(MessageStructure messageStructure) {

        return String.format("@COMMAND?header=%s&body=%s\n", messageStructure.getHeader(), messageStructure.getBody()).getBytes();

    }

    public static MessageStructure createMessageFromBytesArray(byte[] rawData) {

        MessageStructure messageStructure = new MessageStructure("", "", "");
        String rawString = "";
        try {

            rawString = new String(rawData, "UTF-8");

        } catch (UnsupportedEncodingException e) {

            return messageStructure;

        }

        String[] mainLine = rawString.split("[?]");
        if (mainLine.length != 2) {
            return messageStructure;
        }

        String[] lines = mainLine[1].split("[&]");

        if (lines.length != 3) {

            return messageStructure;

        } else {

            String header = "";
            String body = "";
            String replyto = "";

            for (String l : lines) {

                String[] struct = l.split("[=]");

                if (struct.length != 2) {

                    return messageStructure;

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

            return new MessageStructure(header, body, replyto);

        }

    }


}
