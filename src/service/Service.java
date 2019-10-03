package service;

import com.sun.tools.javac.util.Pair;
import domain.Nota;
import domain.StructuraAnUniversitar;
import domain.Student;
import domain.Tema;
import repository.CrudRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Service {
    private static Integer currentWeek;
    private CrudRepository<String, Student> studentRepository;
    private CrudRepository<String, Tema> temaRepository;
    private CrudRepository<Pair<String, String>, Nota> notaRepository;

    public Service(CrudRepository<String, Student> studentFileRepository, CrudRepository<String, Tema> temaFileRepository, CrudRepository<Pair<String, String>, Nota> notaFileRepository) {
        this.studentRepository = studentFileRepository;
        this.temaRepository = temaFileRepository;
        this.notaRepository = notaFileRepository;
    }

    public static void setCurrentWeek(Integer currentWeek){ Service.currentWeek = currentWeek; }

    //student
    public Student findOneStudent(String id){ return studentRepository.findOne(id); }
    public Iterable<Student> findAllStudent(){ return studentRepository.findAll(); }
    public Student saveStudent(String id, String nume, String prenume, String email, Integer grupa, String cadruDidacticIndrumatorLab){
        Student s = new Student(id, nume, prenume, email, grupa, cadruDidacticIndrumatorLab);
        return studentRepository.save(s);
    }
    public Student deleteStudent(String id){ return studentRepository.delete(id); }
    public Student updateStudent(String id, String nume, String prenume, String email, Integer grupa, String cadruDidacticIndurmatorLab){
        Student s = new Student(id, nume, prenume, email, grupa, cadruDidacticIndurmatorLab);
        return studentRepository.update(s);
    }

    //tema
    public Tema findOneTema(String id){ return temaRepository.findOne(id); }
    public Iterable<Tema> findAllTema(){ return temaRepository.findAll(); }
    public Tema saveTema(String id, String descriere, Integer startWeek, Integer deadlineWeek){
        Tema t = new Tema(id, descriere, startWeek, deadlineWeek);
        return temaRepository.save(t);
    }
    public Tema deleteTema(String id){ return temaRepository.delete(id); }
    public Tema updateTema(String id, String descriere, Integer startWeek, Integer deadlineWeek){
        Tema t = new Tema(id, descriere, startWeek, deadlineWeek);
        return temaRepository.update(t);
    }

    //nota
    public Nota findOneNota(Pair<String, String> id){ return notaRepository.findOne(id); }
    public Iterable<Nota> findAllNota(){ return notaRepository.findAll(); }
    public Nota saveNota(String idStudent, String idTema, LocalDate data, String profesor, Float valoare, float motivare, float intarziere, String feedback){
        Student student = studentRepository.findOne(idStudent);
        Tema tema = temaRepository.findOne(idTema);
        if(student == null || tema == null){
            throw new ServiceException("Nota nu se poate asigna, id invalid!");
        }
        Integer saptPredare = StructuraAnUniversitar.getInstance().getWeek(data, StructuraAnUniversitar.getInstance().getSem1());

        float intarziereStudent = saptPredare - tema.getDeadlineWeek();
        float result = intarziereStudent - intarziere - motivare;
        float valoareFinala = valoare;
        if(intarziereStudent <= 0)
            System.out.println("Nu a existat intarziere.");
        else
            System.out.println("Initial intarzierea a fost de " + (int) intarziereStudent + " sapt.");
        System.out.println("Studentul a avut motivare " + (int)motivare + " sapt.");
        System.out.println("Profesorul a intarziat introducerea notei cu " + (int)intarziere + " sapt.");

        if(result == 1 || result == 2){
            valoareFinala = valoare - result;
            System.out.println("In concluzie, a intarziat predarea temei "  + (int)result + " sapt, astfel nota finala nu va fi " + valoare + ", ci " + valoareFinala);
        }
        else if(result >= 3){
            System.out.println("In concluzie, a intarziat cu mai mult de 2 sapt predarea temei, astfel nota finala va fi 1.");
            valoareFinala = 1f;
        }
        else{
            System.out.println("In concluzie, nota finala va fi " + valoareFinala);
        }
        Nota nota = new Nota(new Pair<>(idStudent, idTema), data, profesor, valoareFinala);
        //
        saveToTextFile(nota, student.getNume(), feedback);
        //
        return notaRepository.save(nota);
    }
    public Nota deleteNota(Pair<String, String> id){ return notaRepository.delete(id); }
    public Nota updateNota(String idStudent, String idTema, LocalDate data, String profesor, Float valoare){
        Nota nota = new Nota(new Pair<>(idStudent, idTema), data, profesor, valoare);
        return notaRepository.update(nota);
    }

    public void saveToTextFile(Nota nota, String fileName, String feedback){
        try(FileWriter writer = new FileWriter("data/" + fileName + ".txt", true)){
            writer.write("Tema: " + nota.getId().snd);
            writer.write(System.getProperty( "line.separator" ));
            writer.write("Nota: " + nota.getNota());
            writer.write(System.getProperty( "line.separator" ));
            writer.write("Predata in saptamana: " + StructuraAnUniversitar.getInstance().getWeek(nota.getData(), StructuraAnUniversitar.getInstance().getSem1()));
            writer.write(System.getProperty( "line.separator" ));
            Tema t = findOneTema(nota.getId().snd);
            writer.write("Deadline: " + t.getDeadlineWeek());
            writer.write(System.getProperty( "line.separator" ));
            writer.write("Feedback: " + feedback);
            writer.write(System.getProperty( "line.separator" ));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    //filtrare 1: toti studentii unei grupe
    public Iterable<Student> getStudentByGrupa(Integer grupa){
        return StreamSupport.stream(this.studentRepository.findAll().spliterator(), false)
                .filter(x -> x.getGrupa().equals(grupa))
                .collect(Collectors.toList());
    }

    //obtine lista studentilor dintr-o lista de note
    public Iterable<Student> getStudent(Iterable<Nota> lista) {
        List<Student> rezultat = new ArrayList<>();
        lista.forEach(nota -> {
            Student student = studentRepository.findOne(nota.getId().fst);
            rezultat.add(student);
        });
        return rezultat;
    }

    //filtrare 2: toti studentii care au predat o anumita tema
    public Iterable<Nota> getStudentByTema(String id){
        return StreamSupport.stream(this.notaRepository.findAll().spliterator(), false)
                .filter(x -> x.getId().snd.equals(id))
                .collect(Collectors.toList());
    }

    //filtrare 3: toti studentii care au predat o anumita tema unui anumit profesor
    public Iterable<Nota> getStudentByTemaAndProfesor(String idTema, String profesor){
        return StreamSupport.stream(this.notaRepository.findAll().spliterator(), false)
                .filter(x -> x.getId().snd.equals(idTema))
                .filter(x -> x.getProfesor().equals(profesor))
                .collect(Collectors.toList());
    }

    //filtrare 4: toate notele la o anumita tema dintr-o sapt data
    public Iterable<Nota> getNotaByTemaAndData(String idTema, Integer data){
        return StreamSupport.stream(this.notaRepository.findAll().spliterator(), false)
                .filter(x -> x.getId().snd.equals(idTema) && data.equals(StructuraAnUniversitar.getInstance().getWeek(x.getData(), StructuraAnUniversitar.getInstance().getSem1())))
                .collect(Collectors.toList());
    }

}
