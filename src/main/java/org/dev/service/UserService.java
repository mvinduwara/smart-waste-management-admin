package org.dev.service;


import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.core.Response;
import org.dev.dto.LoggingRequestDTO;
import org.dev.dto.TokenDTO;
import org.dev.entity.User;
import org.dev.util.HibernateUtil;
import org.dev.util.JWTUtil;
import org.hibernate.Session;

import java.util.UUID;

public class UserService {

    public Response validateUser(LoggingRequestDTO loggingRequestDTO) {

        Session hibernatesession = HibernateUtil.getSessionFactory().openSession();
        User user = hibernatesession.createQuery("FROM User u WHERE u.email = :email AND u.password=:password", User.class)
                .setParameter("email", loggingRequestDTO.getEmail())
                .setParameter("password", loggingRequestDTO.getPassword())
                .getSingleResultOrNull();
        hibernatesession.close();

        if (user == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String token = "";

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setAccessToken(JWTUtil.generateToken(user.getEmail()));
        tokenDTO.setRequestToken(JWTUtil.generateToken(user.getEmail()));
        return Response.status(Response.Status.OK).entity(tokenDTO).build();
    }

}
