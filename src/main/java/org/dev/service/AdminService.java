package org.dev.service;

import jakarta.ws.rs.core.Response;
import org.dev.dto.LoggingRequestDTO;
import org.dev.dto.TokenDTO;
import org.dev.entity.Admin;
import org.dev.util.HibernateUtil;
import org.dev.util.JWTUtil;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;

public class AdminService {

    public Response validateAdmin(LoggingRequestDTO loggingRequestDTO) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            // Find admin by username
            Admin admin = session.createQuery("FROM Admin a WHERE a.admin_username = :username", Admin.class)
                    .setParameter("username", loggingRequestDTO.getUsername())
                    .uniqueResultOptional().orElse(null);

            // Check if admin exists AND password matches the hashed version
            if (admin == null || !BCrypt.checkpw(loggingRequestDTO.getPassword(), admin.getAdmin_password())) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid admin credentials").build();
            }

            // Generate JWT Tokens utilizing existing util
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setAccessToken(JWTUtil.generateToken(admin.getAdmin_username()));
            tokenDTO.setRequestToken(JWTUtil.generateToken(admin.getAdmin_username() + "_refresh"));
            tokenDTO.setId(admin.getId());

            return Response.status(Response.Status.OK).entity(tokenDTO).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Login failed due to server error").build();
        } finally {
            session.close();
        }
    }
}