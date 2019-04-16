package edu.bit.felinae;
import com.alibaba.fastjson.JSON;
import com.google.common.hash.Hashing;
import fi.iki.elonen.NanoHTTPD;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;

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
                return handleQueryStatus(sess);
            default:
                return newFixedLengthResponse("404");
        }
    }
    private Session getSession(IHTTPSession sess) {
        CookieHandler cookie = sess.getCookies();
        String session_id = cookie.read("session");
        Jedis jedis = new Jedis("localhost");
        String session_str = jedis.get(session_id);
        Session session = JSON.parseObject(session_str, Session.class);
        return session;
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

    private Response handleQueryStatus(IHTTPSession sess){
        Session session = getSession(sess);
        return newFixedLengthResponse(session.getStatus().toString());
    }

    private Response handleSubmit(IHTTPSession sess){
        Session session = getSession(sess);
        Map<String, String> param = sess.getParms();

    }
}
