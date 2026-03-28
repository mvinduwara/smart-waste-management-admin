package org.dev.service;

import jakarta.ws.rs.core.Response;
import org.dev.dto.CollectionHistoryDTO;
import org.dev.dto.DriverEarningsDTO;
import org.dev.dto.PickupRequestDTO;
import org.dev.dto.SellerHistoryDTO;
import org.dev.entity.*;
import org.dev.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PickupService {

    //Create request
    public Response createPickupRequest(PickupRequestDTO dto) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            User user = session.find(User.class, dto.getUserId());
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            WastePricing pricing = session.createQuery("FROM WastePricing w WHERE w.material_type = :type", WastePricing.class).setParameter("type", dto.getWasteType()).uniqueResultOptional().orElse(null);

            double estimatedValue = 0.0;
            if (pricing != null) {
                try {
                    double weight = Double.parseDouble(dto.getWeight());
                    estimatedValue = weight * pricing.getPrice();
                } catch (NumberFormatException e) {
                    System.out.println("Weight not a valid number, defaulting value to 0.0");
                }
            }

            PickupRequest request = new PickupRequest();
            request.setUser(user);
            request.setWaste_type(dto.getWasteType());
            request.setTotal_weight(dto.getWeight());
            request.setAddress(dto.getAddress());
            request.setNotes(dto.getNotes());
            request.setLatitude(dto.getLatitude());
            request.setLongitude(dto.getLongitude());
            request.setImage_base64(dto.getImageBase64());

            request.setEstimated_value(String.valueOf(estimatedValue));
            request.setStatus("PENDING");
            request.setCreated_at(LocalDateTime.now());

            session.persist(request);
            transaction.commit();

            return Response.status(Response.Status.CREATED).entity("Pickup Request Broadcasted!").build();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to create request").build();
        } finally {
            session.close();
        }
    }

    //Get Active Requests Data
    public Response getActiveRequestByUser(int userId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            // Find the most recent active request (e.g., ACCEPTED or EN_ROUTE)
            PickupRequest request = session.createQuery("FROM PickupRequest p WHERE p.user.id = :uid AND p.status IN ('ACCEPTED', 'EN_ROUTE') ORDER BY p.id DESC", PickupRequest.class).setParameter("uid", userId).setMaxResults(1).uniqueResultOptional().orElse(null);

            if (request == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No active requests found").build();
            }

            PickupRequestDTO dto = new PickupRequestDTO();
            dto.setId(request.getId());
            dto.setWasteType(request.getWaste_type());
            dto.setStatus(request.getStatus());
            dto.setLatitude(request.getLatitude());
            dto.setLongitude(request.getLongitude());

            // If a driver is assigned, pass their details
            if (request.getDriver() != null) {
                dto.setDriverName(request.getDriver().getUsername());
                dto.setDriverContact(request.getDriver().getContact());

                String vehicle = request.getDriver().getVehicle_type() + " - " + request.getDriver().getVehicle_reg_no();
                dto.setVehicleInfo(vehicle);
            } else {
                dto.setDriverName("Pending Assignment");
                dto.setVehicleInfo("N/A");
            }

            return Response.status(Response.Status.OK).entity(dto).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error fetching active request").build();
        } finally {
            session.close();
        }
    }

    //Cancel Requests
    public Response cancelPickupRequest(int requestId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            PickupRequest request = session.find(PickupRequest.class, requestId);

            if (request == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Request not found").build();
            }

            if (!"PENDING".equals(request.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Cannot cancel request because it is already " + request.getStatus()).build();
            }

            request.setStatus("CANCELLED");
            session.merge(request);
            transaction.commit();

            return Response.status(Response.Status.OK).entity("Request cancelled successfully").build();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to cancel request").build();
        } finally {
            session.close();
        }
    }

    //Available Checking
    public Response getAvailableRequests() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<PickupRequest> requests = session.createQuery("FROM PickupRequest p WHERE p.status = 'PENDING' ORDER BY p.id DESC", PickupRequest.class).getResultList();

            List<PickupRequestDTO> dtoList = new ArrayList<>();
            for (PickupRequest request : requests) {
                PickupRequestDTO dto = new PickupRequestDTO();
                dto.setId(request.getId());

                if (request.getUser() != null) {
                    dto.setUserId(request.getUser().getId());
                }

                dto.setWasteType(request.getWaste_type());
                dto.setWeight(request.getTotal_weight());
                dto.setAddress(request.getAddress());
                dto.setNotes(request.getNotes());
                dto.setLatitude(request.getLatitude());
                dto.setLongitude(request.getLongitude());
                dto.setStatus(request.getStatus());

                if (request.getCreated_at() != null) {
                    dto.setCreatedAt(request.getCreated_at().toString());
                }
                if (request.getEstimated_value() != null) {
                    dto.setEstimatedValue(request.getEstimated_value().toString());
                }

                dtoList.add(dto);
            }

            return Response.status(Response.Status.OK).entity(dtoList).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error fetching available requests").build();
        } finally {
            session.close();
        }
    }

    //Accept Request
    public Response acceptPickupRequest(int requestId, int driverId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            PickupRequest request = session.find(PickupRequest.class, requestId);
            if (request == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Request not found").build();
            }

            if (!"PENDING".equals(request.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Sorry, this job is no longer available.").build();
            }

            Driver driver = session.find(Driver.class, driverId);
            if (driver == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Driver not found").build();
            }

            request.setDriver(driver);
            request.setStatus("ACCEPTED");

            session.merge(request);
            transaction.commit();
            return Response.status(Response.Status.OK).entity("Job accepted successfully!").build();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to accept job").build();
        } finally {
            session.close();
        }
    }

    // Active Driver Request
    public Response getActiveDriverRequest(int driverId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            PickupRequest request = session.createQuery("FROM PickupRequest p WHERE p.driver.id = :did AND p.status = 'ACCEPTED' ORDER BY p.id DESC", PickupRequest.class).setParameter("did", driverId).setMaxResults(1).uniqueResult();

            if (request == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No active job").build();
            }

            PickupRequestDTO dto = new PickupRequestDTO();
            dto.setId(request.getId());
            dto.setWasteType(request.getWaste_type());
            dto.setWeight(request.getTotal_weight());
            dto.setAddress(request.getAddress());
            dto.setLatitude(request.getLatitude());
            dto.setLongitude(request.getLongitude());
            dto.setStatus(request.getStatus());

            return Response.status(Response.Status.OK).entity(dto).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error fetching active job").build();
        } finally {
            session.close();
        }
    }

    // Arrive To Location & Process Payment
    public Response completePickupRequest(int requestId, double finalAmount) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            PickupRequest request = session.find(PickupRequest.class, requestId);
            if (request == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Request not found").build();
            }

            User seller = request.getUser();
            double currentBalance = seller.getWalletBalance();
            seller.setWalletBalance(currentBalance + finalAmount);

            request.setStatus("COMPLETED");
            session.merge(request);
            session.merge(seller);

            Transactiondto paymentTransaction = new Transactiondto();
            paymentTransaction.setPickupRequest(request);
            paymentTransaction.setAmount_paid(finalAmount);
            paymentTransaction.setPayment_token("TXN_CARD_" + System.currentTimeMillis());
            paymentTransaction.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));

            session.persist(paymentTransaction);
            transaction.commit();
            return Response.status(Response.Status.OK).entity("Job successfully completed!").build();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to complete job").build();
        } finally {
            session.close();
        }
    }

    //Get Seller History Summary
    public Response getSellerHistorySummary(int userId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            // 1. Get ALL requests made by this seller (newest first)
            List<PickupRequest> userRequests = session.createQuery(
                            "FROM PickupRequest p WHERE p.user.id = :uid ORDER BY p.id DESC", PickupRequest.class)
                    .setParameter("uid", userId)
                    .getResultList();

            // 2. Calculate Total Earned from Transactions linked to this seller
            Double totalEarned = session.createQuery(
                            "SELECT SUM(t.amount_paid) FROM Transactiondto t WHERE t.pickupRequest.user.id = :uid", Double.class)
                    .setParameter("uid", userId)
                    .uniqueResult();

            if (totalEarned == null) {
                totalEarned = 0.0;
            }

            // 3. Convert Entity list to DTO list
            List<PickupRequestDTO> historyList = new ArrayList<>();
            for (PickupRequest req : userRequests) {
                PickupRequestDTO dto = new PickupRequestDTO();
                dto.setId(req.getId());
                dto.setWasteType(req.getWaste_type());
                dto.setWeight(req.getTotal_weight()); // Or getTotal_weight() depending on your entity!
                dto.setStatus(req.getStatus());
                historyList.add(dto);
            }

            // 4. Package and return
            SellerHistoryDTO result = new SellerHistoryDTO();
            result.setTotalRequests(userRequests.size());
            result.setTotalEarned(totalEarned);
            result.setHistory(historyList);

            return Response.ok(result).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error loading seller history").build();
        } finally {
            session.close();
        }
    }

    // Get all requests for a specific user
    public Response getUserRequests(int userId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<PickupRequest> requests = session.createQuery(
                            "FROM PickupRequest p WHERE p.user.id = :uid ORDER BY p.id DESC", PickupRequest.class)
                    .setParameter("uid", userId)
                    .getResultList();

            List<PickupRequestDTO> dtoList = new ArrayList<>();
            for (PickupRequest req : requests) {
                PickupRequestDTO dto = new PickupRequestDTO();
                dto.setId(req.getId());
                dto.setWasteType(req.getWaste_type());
                dto.setWeight(req.getTotal_weight());
                dto.setEstimatedValue(req.getEstimated_value()); // Critical for your dashboard stats
                dto.setStatus(req.getStatus());

                if (req.getCreated_at() != null) {
                    dto.setCreatedAt(req.getCreated_at().toString());
                }

                dtoList.add(dto);
            }

            return Response.status(Response.Status.OK).entity(dtoList).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error fetching user requests").build();
        } finally {
            session.close();
        }
    }
}
