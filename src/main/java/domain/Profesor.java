package domain;

public class Profesor extends Entity<String> {
    private String nume;
    private String prenume;
    private String email;

    public Profesor(String s, String nume, String prenume, String email) {
        super(s);
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return getId() +
                "; " + nume +
                "; " + prenume +
                "; " + email;
    }
}
