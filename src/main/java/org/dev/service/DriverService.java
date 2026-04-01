package org.dev.service;

import org.dev.dto.CollectionHistoryDTO;
import org.dev.dto.DriverEarningsDTO;
import org.dev.dto.LoggingRequestDTO;
import org.dev.dto.TokenDTO;
import org.dev.entity.Driver;
import org.dev.entity.PickupRequest;
import org.dev.entity.Transactiondto;
import org.dev.entity.VerificationStatus;
import org.dev.util.HibernateUtil;
import org.dev.util.JWTUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

public class DriverService {

    //REGISTRATION LOGIC
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

    //LOGIN LOGIC
    public Response validateDriver(LoggingRequestDTO loggingRequestDTO) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
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

    //FETCH PROFILE
    public Response getDriverProfile(int driverId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Driver driver = session.find(Driver.class, driverId);
            if (driver == null) return Response.status(404).build();
            driver.setPassword(null);
            return Response.ok(driver).build();
        }
    }

    //PROFILE UPDATE LOGIC
    public Response updateProfile(int id, Driver updated) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Driver existing = session.find(Driver.class, id);
            if (existing == null) return Response.status(404).build();

            existing.setUsername(updated.getUsername());
            existing.setContact(updated.getContact());

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

    //MESSAGE TOKEN
    public Response updateFcmToken(int driverId, String fcmToken) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Driver driver = session.find(Driver.class, driverId);
            if (driver != null) {
                driver.setFcm_token(fcmToken);
                session.merge(driver);
                transaction.commit();
                return Response.ok("Token updated").build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Driver not found").build();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            return Response.serverError().entity("Error updating token").build();
        } finally {
            session.close();
        }
    }

    //Driver Earnings Calculation
    public Response getDriverEarningsSummary(int driverId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<PickupRequest> completedJobs = session.createQuery(
                            "FROM PickupRequest p WHERE p.driver.id = :did AND p.status = 'COMPLETED'", PickupRequest.class)
                    .setParameter("did", driverId)
                    .getResultList();

            double totalWaste = 0.0;
            for (PickupRequest job : completedJobs) {
                try {
                    if (job.getTotal_weight() != null) {
                        String weightStr = job.getTotal_weight().replaceAll("[^\\d.]", "");
                        if (!weightStr.isEmpty()) totalWaste += Double.parseDouble(weightStr);
                    }
                } catch (Exception ignored) {}
            }

            List<Transactiondto> transactionList = session.createQuery(
                            "FROM Transactiondto t WHERE t.pickupRequest.driver.id = :did ORDER BY t.timestamp DESC", Transactiondto.class)
                    .setParameter("did", driverId)
                    .getResultList();

            double todaysPayout = 0.0;
            List<CollectionHistoryDTO> history = new ArrayList<>();
            java.time.LocalDate today = java.time.LocalDate.now();

            for (Transactiondto t : transactionList) {
                if (t.getTimestamp() != null && t.getTimestamp().toLocalDateTime().toLocalDate().isEqual(today)) {
                    todaysPayout += (t.getAmount_paid() != null ? t.getAmount_paid() : 0.0);
                }
                CollectionHistoryDTO dto = new CollectionHistoryDTO();
                dto.setRequestId(t.getPickupRequest().getId());
                dto.setDate(t.getTimestamp() != null ? t.getTimestamp().toLocalDateTime().toLocalDate().toString() : "Unknown");
                dto.setWasteType(t.getPickupRequest().getWaste_type());
                dto.setWeight(t.getPickupRequest().getTotal_weight());
                dto.setAmountPaid(t.getAmount_paid() != null ? t.getAmount_paid() : 0.0);
                history.add(dto);
            }

            DriverEarningsDTO result = new DriverEarningsDTO();
            result.setTodaysPayout(todaysPayout);
            result.setTotalWasteCollected(totalWaste);
            result.setHistory(history);

            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error loading earnings").build();
        } finally {
            session.close();
        }
    }
}