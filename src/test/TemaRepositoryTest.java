package test;

import domain.Tema;
import domain.validators.TemaValidator;
import domain.validators.ValidationException;
import domain.validators.Validator;
import org.junit.Before;
import org.junit.Test;
import repository.RepositoryException;
import repository.TemaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TemaRepositoryTest {
    Tema t;
    TemaRepository temaRepository;

    @Before
    public void setUp(){
        Validator<Tema> validatorTema = new TemaValidator();
        temaRepository = new TemaRepository(validatorTema);
        t = new Tema("1", "lab1", 1, 2);
        temaRepository.save(t);
    }

    @Test
    public void update(){
        assertThrows(IllegalArgumentException.class, ()->{
           temaRepository.update(null);
        });
        assertThrows(ValidationException.class, ()->{
           Tema t1 = new Tema("100","lab2", 2, 4);
           temaRepository.update(t1);
        });
        assertThrows(ValidationException.class, ()->{
            Tema t2 = new Tema("", "", 0, 15);
            temaRepository.update(t2);
        });
    }
}
