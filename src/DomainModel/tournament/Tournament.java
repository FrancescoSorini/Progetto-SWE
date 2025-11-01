package DomainModel.tournament;
import DomainModel.tournament.observer.TournamentSubject;
import DomainModel.user.User;

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

    public Tournament(String name) {
        this.tournamentName = name;
        this.status = TournamentStatus.PENDING;
    }

    public String getName() {
        return tournamentName;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus newStatus) {
        this.status = newStatus;
        notifyObservers(this); // notifica tutti gli iscritti
    }

}
