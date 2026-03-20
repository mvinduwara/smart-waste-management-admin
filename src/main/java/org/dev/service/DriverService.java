package org.dev.service;

import org.dev.dto.LoggingRequestDTO;
import org.dev.dto.TokenDTO;
import org.dev.entity.Driver;
import org.dev.entity.VerificationStatus;
import org.dev.util.HibernateUtil;
import org.dev.util.JWTUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.ws.rs.core.Response;

public class DriverService {

    // 1. REGISTRATION LOGIC
    public Response registerDriver(Driver newDriver) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // Check if CONTACT NUMBER already exists (Instead of username)
            Long contactCount = session.createQuery("SELECT COUNT(d) FROM Driver d WHERE d.contact = :contact", Long.class)
                    .setParameter("contact", newDriver.getContact())
                    .uniqueResult();

            if (contactCount > 0) {
                return Response.status(Response.Status.CONFLICT).entity("This contact number is already registered").build();
            }

            // Assign default Verification Status (ID 1 = PENDING)
            VerificationStatus defaultStatus = session.find(VerificationStatus.class, 1);
            if (defaultStatus == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Verification Status ID 1 not found in database!").build();
            }
            newDriver.setVerificationStatus(defaultStatus);

            // Hash the password
            String hashedPassword = BCrypt.hashpw(newDriver.getPassword(), BCrypt.gensalt());
            newDriver.setPassword(hashedPassword);

            // Save to database
            session.persist(newDriver);
            transaction.commit();

            return Response.status(Response.Status.CREATED).entity("Driver registered successfully as PENDING").build();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Registration failed").build();
        } finally {
            session.close();
        }
    }

    // 2. LOGIN LOGIC
    public Response validateDriver(LoggingRequestDTO loggingRequestDTO) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            // We use loggingRequestDTO.getUsername() because that is the field name in your JSON/DTO
            Driver driver = session.createQuery(
                            "FROM Driver d WHERE d.username = :user OR d.contact = :user", Driver.class)
                    .setParameter("user", loggingRequestDTO.getUsername())
                    .uniqueResultOptional()
                    .orElse(null);

            if (driver == null || !BCrypt.checkpw(loggingRequestDTO.getPassword(), driver.getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid contact number or password").build();
            }

            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setAccessToken(JWTUtil.generateToken(driver.getUsername()));
            tokenDTO.setRequestToken(JWTUtil.generateToken(driver.getContact() + "_refresh"));
            tokenDTO.setId(driver.getId());

            return Response.status(Response.Status.OK).entity(tokenDTO).build();
        } finally {
            session.close();
        }
    }

    // 2. FETCH PROFILE
    public Response getDriverProfile(int driverId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Driver driver = session.find(Driver.class, driverId);
            if (driver == null) return Response.status(404).build();
            driver.setPassword(null);
            return Response.ok(driver).build();
        }
    }

    // 3. PROFILE UPDATE LOGIC
    public Response updateProfile(int id, Driver updated) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Driver existing = session.find(Driver.class, id);
            if (existing == null) return Response.status(404).build();

            existing.setContact(updated.getContact());
            existing.setVehicle_type(updated.getVehicle_type());
            existing.setVehicle_reg_no(updated.getVehicle_reg_no());
            existing.setLicense_number(updated.getLicense_number());

            session.merge(existing);
            tx.commit();
            return Response.ok("Updated").build();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return Response.status(500).build();
        } finally {
            session.close();
        }
    }
}