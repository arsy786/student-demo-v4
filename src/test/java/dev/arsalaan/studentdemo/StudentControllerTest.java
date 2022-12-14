package dev.arsalaan.studentdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.arsalaan.studentdemo.controller.StudentController;
import dev.arsalaan.studentdemo.model.Student;
import dev.arsalaan.studentdemo.service.StudentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;


@WebMvcTest(StudentController.class)
public class StudentControllerTest {

    @Autowired
    StudentController studentController;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    StudentService studentService;

    Student student1 = new Student(1L, "name1", "email1@gmail.com", LocalDate.parse("2001-01-01"));
    Student student2 = new Student(2L, "name2", "email2@gmail.com", LocalDate.parse("2002-02-02"));
    Student student3 = new Student(3L, "name3", "email3@gmail.com", LocalDate.parse("2003-03-03"));

    @Test //Annotation for JUnit to add Tests in a list to be ran
    public void getAllStudents_success() throws Exception {

        List<Student> students = new ArrayList<>(Arrays.asList(student1, student2, student3));

        // Mocktio.when().thenReturn() chain method mocks the getStudents() method call in the Service
        // everytime the method is called within the controller, it will return 'students' list
        // (instead of actually making a database call.)
        Mockito.when(studentService.getAllStudents()).thenReturn(students);

        // MockMvc.perform() accepts a MockMvcRequest and mocks the API call given the fields of the object.
        // we built a request via the MockMvcRequestBuilders, and only specified the GET path and contentType
        // property since the API endpoint does not accept any parameters.
        // After perform() is ran, andExpect() methods are subsequently chained to it and tests against the results returned by the method.
        // For this call, we've set 3 assertions within the andExpect() methods: that the response returns a 200 or an OK status code,
        // the response returns a list of size 3, and the third Student object from the list has a name property of name3.
        // The statically referenced methods here - jsonPath(), hasSize() and is() belong to the MockMvcResultMatchers and Matchers classes respectively:
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/student/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].name", is("name3")));
    }

    @Test
    public void getAllStudents_emptyList() throws Exception {

        //List<Student> students = new ArrayList<>(Arrays.asList(null, null, null));

        Mockito.when(studentService.getAllStudents()).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/student/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void getStudentById_success() throws Exception {

        Mockito.when(studentService.getStudentById(student1.getId())).thenReturn(student1);

        // Here, we're checking if the result is null, asserting that it isn't and checking if the name field
        // of the returned object is equal to "name1".
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/student/{studentId}", student1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("name1")));
    }

    @Test
    public void getStudentById_notFound() throws Exception {

        Mockito.when(studentService.getStudentById(100L)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/student/{studentId}", student1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test //FIXED
    public void createStudent_success() throws Exception {

        // The POST request handler accepts a POST request and maps the provided values into a PatientRecord POJO via the @RequestBody annotation.
        // Our test unit will also accept JSON and map the values into a Student POJO via the ObjectMapper we've autowired before.

        //Student student100 = new Student("name100", "email100@gmail.com", LocalDate.parse("2004-04-04"));
        /*Mockito.when(studentService.createStudent(Mockito.any(Student.class))).thenReturn(2); FIX */

        //Student studentToPost = new Student("name100", "email100@gmail.com", LocalDate.parse("2004-04-04"));
        //Mockito.doReturn(true).when(studentService).createStudent(studentToPost);

        // We'll also save a reference to the returned MockHttpServletRequestBuilder after it's been generated by MockMvcRequestBuilders
        // so that we can test the returned values
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .post("/api/v1/student/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student1));

        // As we are testing a void method, we do not expect any content except a HttpStatus Code 201
        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated());
    }

    @Test
    public void createStudent_emailTaken() throws Exception {

        Student student4 = new Student("name4", "email3@gmail.com", LocalDate.parse("2004-04-04"));

        //Mockito.when(studentService.createStudent(student3)).thenReturn(2);
        /* Mockito.when(studentService.createStudent(Mockito.any(Student.class))).thenReturn(1); FIX */

        // We'll also save a reference to the returned MockHttpServletRequestBuilder after it's been generated by MockMvcRequestBuilders
        // so that we can test the returned values
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .post("/api/v1/student/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student4));

        // As we are testing a void method, we do not expect any content except a HttpStatus Code 409
        mockMvc.perform(mockRequest)
                .andExpect(status().isConflict());
    }

    // The PUT request handler has a bit more logic to it than the two before this. It checks whether we've
    // provided an ID, resulting in an exception if it's missing. Then, it checks if the ID actually belongs to a
    // record in the database, throwing an exception if it doesn't. Then it checks DB if the email is already taken.
    // Only then does it actually update a record in the DB.

    // We'll create three test methods to check if all three facets of this method are working: one for success,
    // and one for each of the erroneous states that can occur:

    @Test
    public void updateStudent_success() throws Exception {

        Student studentUpdate = new Student ("name1.1", "email1.1@gmail.com", LocalDate.parse("2001-01-01"));

        Mockito.when(studentService.getStudentById(student1.getId())).thenReturn(student1);
        /* Mockito.when(studentService.updateStudent(student1.getId(), studentUpdate.getName(), studentUpdate.getEmail())).thenReturn(3); FIX */

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .put("/api/v1/student/{studentId}", student1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentUpdate));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk());

    }

    // Though, in cases where either the input data isn't right or the database simply doesn't contain the entity we're trying to update,
    // the application should respond with an exception. Let's test that:

    @Test //FIXED
    public void updateStudent_emailTaken() throws Exception {

        Student studentOneUpdate = new Student ("name1.1", "email1", LocalDate.parse("2001-01-01"));

        Mockito.when(studentService.getStudentById(student1.getId())).thenReturn(student1);
        /* Mockito.when(studentService.updateStudent(student1.getId(), studentOneUpdate.getName(), studentOneUpdate.getEmail())).thenReturn(2); FIX */

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .put("/api/v1/student/{studentId}", student1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentOneUpdate));

        mockMvc.perform(mockRequest)
                .andExpect(status().isConflict());

    }

    @Test //FIXED
    public void updateStudent_studentNotFound() throws Exception {

        Student studentUpdate = new Student (100L, "name1.1", "email1.1@gmail.com", LocalDate.parse("2001-01-01"));

        /* Mockito.when(studentService.updateStudent(studentUpdate.getId(), studentUpdate.getName(), studentUpdate.getEmail())).thenReturn(1); FIX */

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .put("/api/v1/student/{studentId}", studentUpdate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentUpdate));

        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound());
    }


    // For DELETE request handler test: creating a test for the successful outcome and a test for the unsuccessful outcome

    @Test
    public void deleteStudentById_success() throws Exception {

        Mockito.when(studentService.getStudentById(student2.getId())).thenReturn(student2);
        /* Mockito.when(studentService.deleteStudentById(student2.getId())).thenReturn(true); FIX */

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/student/{studentId}", student2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }


    @Test //FIXED
    public void deleteStudentById_notFound() throws Exception {

        Student studentDelete = new Student (100L, "name1.1", "email1.1@gmail.com", LocalDate.parse("2001-01-01"));

        /* Mockito.when(studentService.deleteStudentById(studentDelete.getId())).thenReturn(false); FIX */

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/student/{studentId}", studentDelete.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
