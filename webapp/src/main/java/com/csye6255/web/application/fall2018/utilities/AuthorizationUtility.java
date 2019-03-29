package com.csye6255.web.application.fall2018.utilities;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author rkirouchenaradjou
 */
public class AuthorizationUtility {

    public static String[] getHeaderValues(String authorization)
    {
        // Authorization: Basic base64credentials
        String base64Credentials = authorization.substring("Basic".length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        // credentials = username:password
        final String[] values = credentials.split(":", 2);
        return values;
    }


}
