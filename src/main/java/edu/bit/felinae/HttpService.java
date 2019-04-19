package edu.bit.felinae;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.hash.Hashing;
import fi.iki.elonen.NanoHTTPD;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;

public class HttpService extends NanoHTTPD {

    public HttpService() throws IOException {
        super(8001);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }
    @Override
    public Response serve(IHTTPSession sess) {
        String uri = sess.getUri();
        switch (uri){
            case "/status":
                return handleQueryStatus(sess);
            case "/queue":
                return handleSubmit(sess);
            case "/result":
                return handleGetResult(sess);
            default:
                return newFixedLengthResponse("404");
        }
    }
    private Session getSession(IHTTPSession sess) {
        CookieHandler cookie = sess.getCookies();
        String session_id = cookie.read("session");
        if(session_id == null) return null;
        Jedis jedis = new Jedis("localhost");
        String session_str = jedis.get(session_id);
        if(session_str==null) return null;
        return JSON.parseObject(session_str, Session.class);
    }

    private void saveSession(String session_id, Session session) {
        Jedis jedis = new Jedis("localhost");
        String json = JSON.toJSONString(session);
        jedis.set(session_id, json);
    }

    private Response handleQueryStatus(IHTTPSession sess){
        Session session = getSession(sess);
        if(session ==  null)
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "error");
        return newFixedLengthResponse(session.status.toString());
    }

    private Response handleSubmit(IHTTPSession sess) {
        SecureRandom rand = new SecureRandom();
        Jedis jedis = new Jedis("localhost");
        String hashingRes;
        do {
            byte[] randBytes = new byte[30];
            rand.nextBytes(randBytes);
            hashingRes = Hashing.sha256().hashBytes(randBytes).toString();
        } while(jedis.get(hashingRes) != null);
        Session session = new Session();
        Response res = newFixedLengthResponse(hashingRes);
        res.addHeader("Set-Cookie", "session="+hashingRes);
        HashMap<String, String> map = new HashMap<>();
        try {
            sess.parseBody(map);
            String operation_string = map.get("postData");
            JSONObject root_obj = JSON.parseObject(operation_string);
            String transaction_type = root_obj.getString("transactionType");
            JSONObject account_obj = root_obj.getJSONObject("account");
            String username = account_obj.getString("username");
            String password = account_obj.getString("password");
            JSONObject transaction_obj = root_obj.getJSONObject("transactions");
            String transaction = transaction_obj.getString("type");
            session.username = username;
            session.password = password;
            switch (transaction_type){
                case "general":
                    session.transaction_type = TransactionType.general;
                    break;
                case "account":
                    session.transaction_type = TransactionType.account;
                    break;
            }
            switch (transaction){
                case "deposit":
                    session.transaction = Transaction.Deposit;
                    break;
                case "withdrawal":
                    session.transaction = Transaction.Withdrawal;
                    break;
                case "query":
                    session.transaction = Transaction.BalanceInquery;
                    break;
                case "register":
                    session.transaction = Transaction.Register;
                    break;
                case "delete":
                    session.transaction = Transaction.Delete;
                    break;
            }
            String session_json = JSON.toJSONString(session);
            jedis.set(hashingRes, session_json);
            MainQueue.getInstance().enqueue(hashingRes);
            return res;

        }catch (Exception e){
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "bad body");
        }

    }
    private Response handleGetResult(IHTTPSession sess) {
        Session session = getSession(sess);
        JSONObject json = new JSONObject();
        if(session ==  null)
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "error");
        if(session.status != SessionStatus.TransactionDone)
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "error");
        if(session.transaction == Transaction.Register) {
            json.put("result", session.res);
        }
        else if(session.transaction == Transaction.Withdrawal || session.transaction == Transaction.BalanceInquery||
        session.transaction == Transaction.Deposit) {
            json.put("result", session.res);
            json.put("balance", session.balance);
        }
        Jedis jedis = new Jedis("localhost");
        jedis.del(sess.getCookies().read("session"));
        return newFixedLengthResponse(json.toJSONString());
    }
}
