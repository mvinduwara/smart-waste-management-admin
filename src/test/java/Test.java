import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import jakarta.ws.rs.Encoded;
import org.dev.util.HibernateUtil;
import org.hibernate.SessionFactory;

import javax.crypto.SecretKey;

public class Test {
    public static void main(String[] args) {
//        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        SecretKey key  = Jwts.SIG.HS256.key().build();
        String secretkey = Encoders.BASE64.encode(key.getEncoded());
        System.out.println(secretkey);

    }

}