package org.dev.service;

import jakarta.ws.rs.core.Response;
import org.dev.dto.LoggingRequestDTO;
import org.dev.dto.TokenDTO;
import org.dev.entity.Admin;
import org.dev.util.HibernateUtil;
import org.dev.util.JWTUtil;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Map;

public class AdminService {

    //Login
    public Response validateAdmin(LoggingRequestDTO loggingRequestDTO) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Admin admin = session.createQuery("FROM Admin a WHERE a.admin_username = :username", Admin.class)
                    .setParameter("username", loggingRequestDTO.getUsername())
                    .uniqueResultOptional().orElse(null);

            if (admin == null || !BCrypt.checkpw(loggingRequestDTO.getPassword(), admin.getAdmin_password())) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid admin credentials").build();
            }

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

    //Load Cancel Requests
    public Response getRequestsByStatus(String status) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            java.util.List<org.dev.entity.PickupRequest> requests = session.createQuery(
                            "FROM PickupRequest p WHERE p.status = :status ORDER BY p.id DESC", org.dev.entity.PickupRequest.class)
                    .setParameter("status", status)
                    .getResultList();

            // Using a List of Maps guarantees we bypass existing DTO limitations
            java.util.List<java.util.Map<String, Object>> responseList = new java.util.ArrayList<>();

            for (org.dev.entity.PickupRequest req : requests) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", req.getId());
                map.put("wasteType", req.getWaste_type());
                map.put("address", req.getAddress());
                map.put("status", req.getStatus());

                // New Fields mapped for the full table
                map.put("notes", req.getNotes());
                map.put("estimatedValue", req.getEstimated_value()); // Adjust to getEstimatedValue() if your entity uses camelCase
                map.put("latitude", req.getLatitude());
                map.put("longitude", req.getLongitude());

                if (req.getCreated_at() != null) {
                    map.put("createdAt", req.getCreated_at().toString());
                }

                // Safely extract User Data
                if (req.getUser() != null) {
                    map.put("userName", req.getUser().getUsername()); // Adjust to getFirst_name() if needed
                    map.put("userEmail", req.getUser().getEmail());
                }

                // Safely extract Driver Data (if assigned)
                if (req.getDriver() != null) {
                    map.put("driverName", req.getDriver().getUsername()); // Adjust to getFirst_name() if needed
                    map.put("driverContact", req.getDriver().getContact());
                }

                responseList.add(map);
            }
            return Response.ok(responseList).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error fetching requests").build();
        } finally {
            session.close();
        }
    }

    // Load All Users
    public Response getAllUsers() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            // Adjust "User" to match your exact entity name if different
            java.util.List<org.dev.entity.User> users = session.createQuery("FROM User", org.dev.entity.User.class).getResultList();

            java.util.List<java.util.Map<String, Object>> responseList = new java.util.ArrayList<>();

            for (org.dev.entity.User user : users) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", user.getId());
                map.put("username", user.getUsername());
                map.put("email", user.getEmail());
                // Assuming you have a contact/phone field. Update getter if necessary:
                map.put("contact", user.getContact());

                responseList.add(map);
            }
            return Response.ok(responseList).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error fetching users").build();
        } finally {
            session.close();
        }
    }

    // Load All Drivers
    public Response getAllDrivers() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            // Adjust "Driver" to match your exact entity name if different
            java.util.List<org.dev.entity.Driver> drivers = session.createQuery("FROM Driver", org.dev.entity.Driver.class).getResultList();

            java.util.List<java.util.Map<String, Object>> responseList = new java.util.ArrayList<>();

            for (org.dev.entity.Driver driver : drivers) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", driver.getId());
                map.put("username", driver.getUsername());
                map.put("contact", driver.getContact());

                responseList.add(map);
            }
            return Response.ok(responseList).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error fetching drivers").build();
        } finally {
            session.close();
        }
    }

    // Fetch a single User Profile for Admin
    public Response getUserById(int userId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            org.dev.entity.User user = session.find(org.dev.entity.User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            // Map the data to avoid exposing sensitive info or recursion
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            map.put("contact", user.getContact());
            // Add your totalEarnings and totalRequests logic here if you have it in the DB

            return Response.ok(map).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error fetching user profile").build();
        } finally {
            session.close();
        }
    }

    // Fetch a single Driver Profile for Admin
    public Response getDriverById(int driverId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            org.dev.entity.Driver driver = session.find(org.dev.entity.Driver.class, driverId);
            if (driver == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Driver not found").build();
            }

            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", driver.getId());
            map.put("username", driver.getUsername());
            map.put("contact", driver.getContact());
            map.put("license_number", driver.getLicense_number());
            map.put("vehicle_reg_no", driver.getVehicle_reg_no());
            map.put("vehicle_type", driver.getVehicle_type());
            // Add status if you have it in your entity

            return Response.ok(map).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error fetching driver profile").build();
        } finally {
            session.close();
        }
    }

    // Load Transactions
    public Response getAllTransactions() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            // Check your exact entity name (e.g., Transactiondto, Transaction, etc.)
            java.util.List<org.dev.entity.Transactiondto> transactions =
                    session.createQuery("FROM Transactiondto", org.dev.entity.Transactiondto.class).getResultList();

            java.util.List<java.util.Map<String, Object>> responseList = new java.util.ArrayList<>();

            for (org.dev.entity.Transactiondto transaction : transactions) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();

                // IMPORTANT: Change these .get() methods to match what is inside your Transactiondto.java!
                map.put("id", transaction.getId());
                // If it relates to a user/pickup request, fetch the name/details
                // map.put("customer", transaction.getUser().getUsername());
                // map.put("amount", transaction.getAmount());
                // map.put("date", transaction.getDate().toString());
                // map.put("status", transaction.getStatus());

                responseList.add(map);
            }
            return Response.ok(responseList).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error fetching transactions").build();
        } finally {
            session.close();
        }
    }

    // Fetch All Waste Pricing Types
    public Response getAllWastePricing() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            // Check your exact entity name (e.g., WastePricing)
            java.util.List<org.dev.entity.WastePricing> pricingList =
                    session.createQuery("FROM WastePricing", org.dev.entity.WastePricing.class).getResultList();

            java.util.List<java.util.Map<String, Object>> responseList = new java.util.ArrayList<>();

            for (org.dev.entity.WastePricing pricing : pricingList) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", pricing.getId());
                // Update getters to match your WastePricing.java entity
                map.put("name", pricing.getMaterial_type());
                map.put("price", pricing.getPrice());
                responseList.add(map);
            }
            return Response.ok(responseList).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error fetching waste pricing").build();
        } finally {
            session.close();
        }
    }

    // Update Waste Pricing
    public Response updateWastePricing(int id, Map<String, Object> requestData) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Transaction tx = null;

        try {
            tx = session.beginTransaction();
            org.dev.entity.WastePricing pricing = session.find(org.dev.entity.WastePricing.class, id);

            if (pricing == null) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Waste type not found\"}").build();
            }

            if (requestData.get("price") != null) {
                double newPrice = Double.parseDouble(requestData.get("price").toString());
                pricing.setPrice(newPrice);
            }

            tx.commit();

            return Response.ok("{\"message\":\"Pricing updated successfully!\"}").build();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return Response.serverError().entity("{\"error\":\"Error updating pricing\"}").build();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}