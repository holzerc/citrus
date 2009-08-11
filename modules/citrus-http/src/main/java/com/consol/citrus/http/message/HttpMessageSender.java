package com.consol.citrus.http.message;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.util.HttpConstants;
import com.consol.citrus.http.util.HttpUtils;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.message.ReplyMessageHandler;
import com.consol.citrus.util.MessageUtils;

public class HttpMessageSender implements MessageSender {
    /** Http url as service detination */
    private String requestUrl;

    /** Request method */
    private String requestMethod = HttpConstants.HTTP_POST;

    /** Http socket */
    private Socket socket;
    
    private ReplyMessageHandler replyMessageHandler;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HttpMessageSender.class);
    
    public void send(Message<?> message) {
        Writer writer = null;
        BufferedReader reader = null;

        try {
            log.info("Sending message to: " + getRequestUrl());

            if (log.isDebugEnabled()) {
                log.debug("Message to be sent:");
                log.debug(message.getPayload().toString());
            }

            Map<String, Object> requestHeaders = new HashMap<String, Object>();
            
            requestHeaders.put("HTTPVersion", HttpConstants.HTTP_VERSION);
            requestHeaders.put("HTTPMethod", requestMethod);
            requestHeaders.put("HTTPUri", getUri());
            requestHeaders.put("HTTPHost", getHost());
            requestHeaders.put("HTTPPort", getPort());

            /* before sending set header values */
            for (Entry headerEntry : message.getHeaders().entrySet()) {
                final String key = headerEntry.getKey().toString();
                
                if(MessageUtils.isSpringIntegrationHeaderEntry(key)) {
                    continue;
                }
                
                final String value = (String) headerEntry.getValue();

                if (log.isDebugEnabled()) {
                    log.debug("Setting message property: " + key + " to: " + value);
                }

                requestHeaders.put(key, value);
            }

            Message request;
            if (requestMethod.equals(HttpConstants.HTTP_POST)) {
                request = MessageBuilder.withPayload(message.getPayload()).copyHeaders(requestHeaders).build();
            } else if (requestMethod.equals(HttpConstants.HTTP_GET)) {
                //TODO: implement GET method
                request = MessageBuilder.withPayload("").build();
            } else {
                throw new CitrusRuntimeException("Unsupported request method: " + requestMethod);
            }

            InetAddress addr = InetAddress.getByName(getHost());
            socket = new Socket(addr, Integer.valueOf(getPort()));

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF8"));
            writer.write(HttpUtils.generateRequest(request));
            writer.flush();

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append(HttpConstants.LINE_BREAK);
            }

            if(replyMessageHandler != null) {
                replyMessageHandler.onReplyMessage(MessageBuilder.withPayload(buffer.toString()).build());
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error("Error while closing OutputStream", e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error while closing InputStream", e);
                }
            }
        }
    }

    private String getPort() {
        Assert.isTrue(StringUtils.hasText(requestUrl),
                        "You must specify a requestUrl (e.g. http://localhost:8080/test");
        
        String port = requestUrl.substring("http://".length());
        
        if(port.contains(":")) {
            port = port.substring(port.indexOf(':')+1);
            
            if(port.contains("/")) {
                port = port.substring(0, port.indexOf('/'));
            }
            
            return port;
        } else {
            return "8080";
        }
    }

    private String getHost() {
        Assert.isTrue(StringUtils.hasText(requestUrl),
                        "You must specify a requestUrl (e.g. http://localhost:8080/test");
        
        String host = requestUrl.substring("http://".length());
        if(host.contains(":")) {
            host = host.substring(0, host.indexOf(":"));
        } else {
            host = host.substring(0, host.indexOf('/'));
        }
        
        return host;
    }

    private String getUri() {
        Assert.isTrue(StringUtils.hasText(requestUrl),
                        "You must specify a requestUrl (e.g. http://localhost:8080/test");
        
        String uri = requestUrl.substring("http://".length());
        
        if(uri.contains("/")) {
            uri = uri.substring(uri.indexOf("/"));
            return uri;
        } else {
            return "";
        }
    }

    /**
     * @return the urlPath
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * @param url the url to set
     */
    public void setRequestUrl(String url) {
        this.requestUrl = url;
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * @param socket the socket to set
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * @param requestMethod the requestMethod to set
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }
}