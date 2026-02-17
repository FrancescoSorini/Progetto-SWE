package DomainModel.tournament;
import DomainModel.tournament.observer.TournamentSubject;
import DomainModel.user.User;
import DomainModel.GameType;

import java.time.LocalDate;
import java.util.List;

public class Tournament extends TournamentSubject {
    private int tournamentId;
    private String tournamentName;
    private String description;
    private User organizer;          // molti-a-uno
    private int capacity;
    private LocalDate deadline;
    private LocalDate startDate;
    private TournamentStatus status;
    private List<Registration> registrations;
    private GameType gameType;

    // COSTRUTTORI
    public Tournament() {}

    public Tournament(String name) {
        this.tournamentName = name;
        this.status = TournamentStatus.PENDING;
    }

    public Tournament(String name, GameType gameType) {
        this.tournamentName = name;
        this.gameType = gameType;
        this.status = TournamentStatus.PENDING;
    }

    public Tournament(String name, String description, User organizer, int capacity, LocalDate deadline, LocalDate startDate, GameType gameType) {
        this.tournamentName = name;
        this.description = description;
        this.organizer = organizer;
        this.capacity = capacity;
        this.deadline = deadline;
        this.startDate = startDate;
        this.gameType = gameType;
        this.status = TournamentStatus.PENDING;
    }

    // GETTER
    public int getTournamentId() { return tournamentId; }
    public String getName() { return tournamentName; }
    public String getDescription() { return description; }
    public User getOrganizer() { return organizer; }
    public int getCapacity() { return capacity; }
    public LocalDate getDeadline() { return deadline; }
    public LocalDate getStartDate() { return startDate; }
    public TournamentStatus getStatus() { return status; }
    public List<Registration> getRegistrations() { return registrations; }
    public GameType getGameType() { return gameType; }


    // SETTER
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }
    public void setName(String name) { this.tournamentName = name; }
    public void setDescription(String description) { this.description = description; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setStatus(TournamentStatus newStatus) {
        this.status = newStatus;
        notifyObservers(this); // notifica tutti gli iscritti
    }
    public void setRegistrations(List<Registration> registrations) { this.registrations = registrations; }
    public void setGameType(GameType gameType) { this.gameType = gameType; }


    // Registration handling
    public void addRegistration(Registration r) {
        this.registrations.add(r);
    }

    public void removeRegistration(Registration r) {
        this.registrations.remove(r);
    }

    public boolean isRegistrationOpen() {
        return LocalDate.now().isBefore(deadline);
    }

    public boolean isFull() {
        return registrations.size() >= capacity;
    }


    public boolean isStarted() {
        return LocalDate.now().isAfter(startDate);
    }

    public void printTournamentInfo(List<Tournament> tournaments){
        if (tournaments.isEmpty()) {
            System.out.println("Nessun torneo trovato.");
            return;
        }

        for (Tournament t : tournaments) {
            System.out.println("----------------------");
            System.out.println("ID: " + t.getTournamentId());
            System.out.println("Nome: " + t.getName());
            System.out.println("Descrizione: " + t.getDescription());
            System.out.println("Organizzatore: " + t.getOrganizer().getUsername());
            System.out.println("Capacità: " + t.getCapacity());
            System.out.println("Iscrizioni: " + t.getRegistrations().size());
            System.out.println("Deadline iscrizioni: " + t.getDeadline());
            System.out.println("Data inizio: " + t.getStartDate());
            System.out.println("Stato: " + t.getStatus());
            System.out.println("Gioco: " + t.getGameType());
        }
    }

}
