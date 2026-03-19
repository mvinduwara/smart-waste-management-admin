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
            // Find driver by CONTACT NUMBER
            // Note: We use loggingRequestDTO.getUsername() because that is the field name in your JSON/DTO
            Driver driver = session.createQuery("FROM Driver d WHERE d.contact = :contact", Driver.class)
                    .setParameter("contact", loggingRequestDTO.getUsername())
                    .uniqueResultOptional().orElse(null);

            // Check if driver exists AND password matches the hashed version
            if (driver == null || !BCrypt.checkpw(loggingRequestDTO.getPassword(), driver.getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid contact number or password").build();
            }

            // Generate JWT Tokens using the CONTACT NUMBER
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setAccessToken(JWTUtil.generateToken(driver.getContact()));
            tokenDTO.setRequestToken(JWTUtil.generateToken(driver.getContact() + "_refresh"));

            return Response.status(Response.Status.OK).entity(tokenDTO).build();
        } finally {
            session.close();
        }
    }

    // 3. PROFILE UPDATE LOGIC
    public Response updateProfile(int driverId, Driver updatedData) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            Driver existingDriver = session.find(Driver.class, driverId);

            if (existingDriver == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Driver not found").build();
            }

            // Update allowed fields
            existingDriver.setUsername(updatedData.getUsername());
            existingDriver.setContact(updatedData.getContact());
            existingDriver.setVehicle_type(updatedData.getVehicle_type());
            existingDriver.setVehicle_reg_no(updatedData.getVehicle_reg_no());
            existingDriver.setLicense_number(updatedData.getLicense_number());

            // Only update password if a new one is provided
            if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
                existingDriver.setPassword(BCrypt.hashpw(updatedData.getPassword(), BCrypt.gensalt()));
            }

            session.merge(existingDriver);
            transaction.commit();

            return Response.status(Response.Status.OK).entity("Driver profile updated successfully").build();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Update failed").build();
        } finally {
            session.close();
        }
    }
}