package org.dev.service;

import jakarta.ws.rs.core.Response;
import org.dev.dto.PickupRequestDTO;
import org.dev.entity.PickupRequest;
import org.dev.entity.User;
import org.dev.entity.WastePricing;
import org.dev.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;

public class PickupService {
    public Response createPickupRequest(PickupRequestDTO dto) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // 1. Verify User Exists
            User user = session.find(User.class, dto.getUserId());
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            // 2. Calculate Estimated Price
            WastePricing pricing = session.createQuery("FROM WastePricing w WHERE w.material_type = :type", WastePricing.class)
                    .setParameter("type", dto.getWasteType())
                    .uniqueResultOptional().orElse(null);

            double estimatedValue = 0.0;
            if (pricing != null) {
                try {
                    double weight = Double.parseDouble(dto.getWeight());
                    estimatedValue = weight * pricing.getPrice();
                } catch (NumberFormatException e) {
                    System.out.println("Weight not a valid number, defaulting value to 0.0");
                }
            }

            // 3. Build & Save the Entity
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
}
