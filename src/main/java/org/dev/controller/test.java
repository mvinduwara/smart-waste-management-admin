package org.dev.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dev.entity.Student;
import org.dev.util.HibernateUtil;
import org.hibernate.Session;

import java.awt.*;

@Path("/test")
public class test {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "done";
    }


//    @GET
//    @Path("/students")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getAllStudents(){
//        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
//        List<Student> fromStudentS = hibernateSession.createQuery("FROM Student s", Student.class)
//                .getResultList();
//        hibernateSession.close();
//        return Response.ok().entity(fromStudentS).build();
//    }

}