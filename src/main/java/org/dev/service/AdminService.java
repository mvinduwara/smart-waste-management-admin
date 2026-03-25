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
            List<org.dev.entity.PickupRequest> requests = session.createQuery(
                            "FROM PickupRequest p WHERE p.status = :status ORDER BY p.id DESC", org.dev.entity.PickupRequest.class)
                    .setParameter("status", status)
                    .getResultList();

            List<PickupRequestDTO> dtoList = new ArrayList<>();
            for (org.dev.entity.PickupRequest req : requests) {
              PickupRequestDTO dto = new PickupRequestDTO();
                dto.setId(req.getId());
                dto.setWasteType(req.getWaste_type());
                dto.setWeight(req.getTotal_weight());
                dto.setAddress(req.getAddress());
                dto.setStatus(req.getStatus());

                if (req.getUser() != null) {
                    dto.setUserId(req.getUser().getId());
                }
                if (req.getCreated_at() != null) {
                    dto.setCreatedAt(req.getCreated_at().toString());
                }
                dtoList.add(dto);
            }
            return Response.ok(dtoList).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error fetching requests").build();
        } finally {
            session.close();
        }
    }


}