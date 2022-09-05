/*
 * The MIT License
 *
 * Copyright (c) 2011 Takeru Ohta <phjgt308@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.alicebot.ab;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Chat;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.utils.CalendarUtils;
import org.alicebot.ab.utils.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Sraix {
    public static HashMap<String, String> custIdMap = new HashMap();
    private static String custid = "0";

    public static String sraix(Chat chatSession, String input, String defaultResponse, String hint, String host, String botid, String apiKey, String limit) {
        String response = host != null && botid != null ? Sraix.sraixPandorabots(input, chatSession, host, botid) : Sraix.sraixPannous(input, hint, chatSession);
        if (response.equals(MagicStrings.sraix_failed)) {
            if (chatSession != null && defaultResponse == null) {
                response = AIMLProcessor.respond(MagicStrings.sraix_failed, "nothing", "nothing", chatSession);
            } else if (defaultResponse != null) {
                response = defaultResponse;
            }
        }
        return response;
    }

    public static String sraixPandorabots(String input, Chat chatSession, String host, String botid) {
        String responseContent = Sraix.pandorabotsRequest(input, host, botid);
        if (responseContent == null) {
            return MagicStrings.sraix_failed;
        }
        return Sraix.pandorabotsResponse(responseContent, chatSession, host, botid);
    }

    public static String pandorabotsRequest(String input, String host, String botid) {
        try {
            custid = "0";
            String key = host + ":" + botid;
            if (custIdMap.containsKey(key)) {
                custid = custIdMap.get(key);
            }
            String spec = NetworkUtils.spec(host, botid, custid, input);
            String responseContent = NetworkUtils.responseContent(spec);
            return responseContent;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String pandorabotsResponse(String sraixResponse, Chat chatSession, String host, String botid) {
        int n1 = sraixResponse.indexOf("<that>");
        int n2 = sraixResponse.indexOf("</that>");
        String botResponse = MagicStrings.sraix_failed;
        if (n2 > n1) {
            botResponse = sraixResponse.substring(n1 + "<that>".length(), n2);
        }
        if ((n1 = sraixResponse.indexOf("custid=")) > 0) {
            custid = sraixResponse.substring(n1 + "custid=\"".length(), sraixResponse.length());
            n2 = custid.indexOf("\"");
            custid = n2 > 0 ? custid.substring(0, n2) : "0";
            String key = host + ":" + botid;
            custIdMap.put(key, custid);
        }
        if (botResponse.endsWith(".")) {
            botResponse = botResponse.substring(0, botResponse.length() - 1);
        }
        return botResponse;
    }

    public static String sraixPannous(String input, String hint, Chat chatSession) {
        try {
            if (hint == null) {
                hint = MagicStrings.sraix_no_hint;
            }
            input = " " + input + " ";
            input = input.replace(" point ", ".");
            input = input.replace(" rparen ", ")");
            input = input.replace(" lparen ", "(");
            input = input.replace(" slash ", "/");
            input = input.replace(" star ", "*");
            input = input.replace(" dash ", "-");
            input = input.trim();
            input = input.replace(" ", "+");
            int offset = CalendarUtils.timeZoneOffset();
            String locationString = "";
            if (Chat.locationKnown) {
                locationString = "&location=" + Chat.latitude + "," + Chat.longitude;
            }
            String url = "https://weannie.pannous.com/api?input=" + input + "&locale=en_US&timeZone=" + offset + locationString + "&login=" + MagicStrings.pannous_login + "&ip=" + NetworkUtils.localIPAddress() + "&botid=0&key=" + MagicStrings.pannous_api_key + "&exclude=Dialogues,ChatBot&out=json";
            if (MagicBooleans.trace_mode) {
                System.out.println("Sraix url='" + url + "'");
            }
            String page = NetworkUtils.responseContent(url);
            if (MagicBooleans.trace_mode) {
                System.out.println("Sraix: " + page);
            }
            String text = "";
            String imgRef = "";
            if (page != null && page.length() != 0) {
                JSONArray outputJson = new JSONObject(page).getJSONArray("output");
                if (outputJson.length() == 0) {
                    text = MagicStrings.sraix_failed;
                } else {
                    JSONObject sObj;
                    Object obj;
                    JSONObject firstHandler = outputJson.getJSONObject(0);
                    JSONObject actions = firstHandler.getJSONObject("actions");
                    if (actions.has("reminder")) {
                        obj = actions.get("reminder");
                        if (obj instanceof JSONObject) {
                            sObj = (JSONObject)obj;
                            String date = sObj.getString("date");
                            date = date.substring(0, "2012-10-24T14:32".length());
                            String duration = sObj.getString("duration");
                            Pattern datePattern = Pattern.compile("(.*)-(.*)-(.*)T(.*):(.*)");
                            Matcher m = datePattern.matcher(date);
                            String year = "";
                            String month = "";
                            String day = "";
                            String hour = "";
                            String minute = "";
                            if (m.matches()) {
                                year = m.group(1);
                                month = String.valueOf(Integer.parseInt(m.group(2)) - 1);
                                day = m.group(3);
                                hour = m.group(4);
                                minute = m.group(5);
                                text = "<year>" + year + "</year>" + "<month>" + month + "</month>" + "<day>" + day + "</day>" + "<hour>" + hour + "</hour>" + "<minute>" + minute + "</minute>" + "<duration>" + duration + "</duration>";
                            } else {
                                text = MagicStrings.schedule_error;
                            }
                        }
                    } else if (actions.has("say") && !hint.equals(MagicStrings.sraix_pic_hint)) {
                        obj = actions.get("say");
                        if (obj instanceof JSONObject) {
                            sObj = (JSONObject)obj;
                            text = sObj.getString("text");
                            if (sObj.has("moreText")) {
                                JSONArray arr = sObj.getJSONArray("moreText");
                                for (int i = 0; i < arr.length(); ++i) {
                                    text = text + " " + arr.getString(i);
                                }
                            }
                        } else {
                            text = obj.toString();
                        }
                    }
                    if (actions.has("show") && !text.contains("Wolfram") && actions.getJSONObject("show").has("images")) {
                        int i;
                        JSONArray arr = actions.getJSONObject("show").getJSONArray("images");
                        imgRef = arr.getString(i = (int)((double)arr.length() * Math.random()));
                        if (imgRef.startsWith("//")) {
                            imgRef = "http:" + imgRef;
                        }
                        imgRef = "<a href=\"" + imgRef + "\"><img src=\"" + imgRef + "\"/></a>";
                    }
                }
                if (hint.equals(MagicStrings.sraix_event_hint) && !text.startsWith("<year>")) {
                    return MagicStrings.sraix_failed;
                }
                if (text.equals(MagicStrings.sraix_failed)) {
                    return AIMLProcessor.respond(MagicStrings.sraix_failed, "nothing", "nothing", chatSession);
                }
                text = text.replace("&#39;", "'");
                text = text.replace("&apos;", "'");
                text = text.replaceAll("\\[(.*)\\]", "");
                String[] sentences = text.split("\\. ");
                String clippedPage = sentences[0];
                for (int i = 1; i < sentences.length; ++i) {
                    if (clippedPage.length() >= 500) continue;
                    clippedPage = clippedPage + ". " + sentences[i];
                }
                clippedPage = clippedPage + " " + imgRef;
                return clippedPage;
            }
            text = MagicStrings.sraix_failed;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Sraix '" + input + "' failed");
        }
        return MagicStrings.sraix_failed;
    }
}

