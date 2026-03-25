package org.dev.service;

import jakarta.ws.rs.core.Response;
import org.dev.dto.LoggingRequestDTO;
import org.dev.dto.PickupRequestDTO;
import org.dev.dto.TokenDTO;
import org.dev.entity.Admin;
import org.dev.util.HibernateUtil;
import org.dev.util.JWTUtil;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

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

}