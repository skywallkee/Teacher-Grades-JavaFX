package test;

import domain.Student;
import domain.validators.StudentValidator;
import domain.validators.ValidationException;
import domain.validators.Validator;
import org.junit.Before;
import org.junit.Test;
import repository.RepositoryException;
import repository.xml.StudentXMLRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;



public class temaXMLTest {
    Student s;
    StudentXMLRepository studentRepository;

    @Before
    public void setUp() throws Exception{
        Validator<Student> validatorStudent = new StudentValidator();
        StudentXMLRepository studentXMLRepository = new StudentXMLRepository(validatorStudent, "studentiTestXML.xml");
        s = new Student("1", "Aldea", "Vipera", "vip@gmail.com", 223, "Vescan Andreea");
        studentRepository.save(s);
    }

    @Test
    public void update(){
        assertEquals(s, studentRepository.update(s));
        assertThrows(IllegalArgumentException.class, ()->{
            studentRepository.update(null);
        });
        assertThrows(RepositoryException.class, ()->{
            Student s2 = new Student("100", "A","B","C", 221,"D");
            studentRepository.update(s2);
        });
        assertThrows(ValidationException.class, ()->{
            Student s3 = new Student("","","","", -1, "");
            studentRepository.update(s3);
        });
    }
}
