package org.dev.service;


import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.core.Response;
import org.dev.dto.LoggingRequestDTO;
import org.dev.dto.TokenDTO;
import org.dev.entity.User;
import org.dev.util.HibernateUtil;
import org.dev.util.JWTUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {

    // REGISTRATION LOGIC
    public Response registerUser(User newUser) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // Check if email already exists
            Long emailCount = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                    .setParameter("email", newUser.getEmail())
                    .uniqueResult();

            if (emailCount > 0) {
                return Response.status(Response.Status.CONFLICT).entity("Email already exists").build();
            }

            Long contactCount = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.contact = :contact", Long.class)
                    .setParameter("contact", newUser.getContact())
                    .uniqueResult();

            if (contactCount > 0) {
                return Response.status(Response.Status.CONFLICT).entity("Contact number already exists").build();
            }

            // Hash the password before saving
            String hashedPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
            newUser.setPassword(hashedPassword);

            // Save to database
            session.persist(newUser);
            transaction.commit();

            return Response.status(Response.Status.CREATED).entity("User registered successfully").build();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Registration failed").build();
        } finally {
            session.close();
        }
    }

    //LOGIN LOGIC
    public  Response validateUser(LoggingRequestDTO loggingRequestDTO) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            // Find user by username or email
            User user = session.createQuery("FROM User u WHERE u.username = :username OR u.email = :username", User.class)
                    .setParameter("username", loggingRequestDTO.getUsername())
                    .uniqueResultOptional().orElse(null);

            // Check if user exists AND password matches the hashed version
            if (user == null || !BCrypt.checkpw(loggingRequestDTO.getPassword(), user.getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
            }

            // Generate JWT Tokens
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setAccessToken(JWTUtil.generateToken(user.getEmail()));
            tokenDTO.setRequestToken(JWTUtil.generateToken(user.getEmail() + "_refresh"));

            return Response.status(Response.Status.OK).entity(tokenDTO).build();
        } finally {
            session.close();
        }
    }

    //UPDATE LOGIC
    public Response updateProfile(int userId, User updatedData) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            User existingUser = session.find(User.class, userId);

            if (existingUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            // Update allowed fields
            existingUser.setUsername(updatedData.getUsername());

            // Only update password if a new one is provided
            if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
                existingUser.setPassword(BCrypt.hashpw(updatedData.getPassword(), BCrypt.gensalt()));
            }

            session.merge(existingUser);
            transaction.commit();

            return Response.status(Response.Status.OK).entity("Profile updated successfully").build();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").build();
        } finally {
            session.close();
        }
    }

    // User Data View Logic
    public Response getUserById(int userId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            User user = session.find(User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }
            user.setPassword(null);
            return Response.status(Response.Status.OK).entity(user).build();
        } finally {
            session.close();
        }
    }
}
