package org.dev.service;


import jakarta.ws.rs.core.Response;
import org.dev.dto.LoggingRequestDTO;
import org.dev.entity.User;
import org.dev.util.HibernateUtil;
import org.hibernate.Session;

import java.util.UUID;

public class UserService {

    public Response validateUser(LoggingRequestDTO loggingRequestDTO) {

        Session hibernatesession = HibernateUtil.getSessionFactory().openSession();
        User user = hibernatesession.createQuery("FROM User u WHERE u.email : email AND u.password:password", User.class)
                .setParameter("email", loggingRequestDTO.getEmail())
                .setParameter("password", loggingRequestDTO.getPassword())
                .getSingleResultOrNull();
        hibernatesession.close();

        if (user == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String token = "";

        return Response.status(Response.Status.OK).entity(token).build();
    }

}
