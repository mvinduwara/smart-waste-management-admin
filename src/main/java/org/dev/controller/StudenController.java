package org.dev.controller;


import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dev.entity.Student;
import org.dev.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

@Path("/students")
public class StudenController {
    @Path("/get-all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllStudents(){
        try(Session hibernateSession = HibernateUtil.getSessionFactory().openSession()){
            List<Student> studentList = hibernateSession.createQuery("FROM Student s", Student.class)
                    .getResultList();
            return Response.ok().entity(studentList).build();
        }
    }
}
