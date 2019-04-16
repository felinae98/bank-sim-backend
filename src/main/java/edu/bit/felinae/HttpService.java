package edu.bit.felinae;
import com.alibaba.fastjson.JSON;
import com.google.common.hash.Hashing;
import fi.iki.elonen.NanoHTTPD;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.security.SecureRandom;

public class HttpService extends NanoHTTPD {

    public HttpService() throws IOException {
        super(8001);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }
    @Override
    public Response serve(IHTTPSession sess) {
        String uri = sess.getUri();
        switch (uri){
            case "/get-ticket":
                return handleTicket(sess);
            case "/status":
                return handleStatus(sess);
            default:
                return newFixedLengthResponse("404");
        }
    }

    private Response handleTicket(IHTTPSession sess) {
        SecureRandom rand = new SecureRandom();
        Jedis jedis = new Jedis("localhost");
        String hashingRes;
        do {
            byte[] randBytes = new byte[30];
            rand.nextBytes(randBytes);
            hashingRes = Hashing.sha256().hashBytes(randBytes).toString();
        } while(jedis.get(hashingRes) != null);
        Session session = new Session();
        String session_json = JSON.toJSONString(session);
        jedis.set(hashingRes, session_json);
        Response res = newFixedLengthResponse(hashingRes);
        res.addHeader("Set-Cookie", "session="+hashingRes);
        return res;
    }

    private Response handleStatus(IHTTPSession sess){
        CookieHandler cookie = new CookieHandler(sess.getHeaders());
        String session_id = cookie.read("session");
        if(session_id == null) {
            return newFixedLengthResponse("your session plz");
        }
        Jedis jedis = new Jedis("localhost");
        String session_str = jedis.get(session_id);
        Session session = JSON.parseObject(session_str, Session.class);
        return newFixedLengthResponse(session.getStatus().toString());
    }
}
