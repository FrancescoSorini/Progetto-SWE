package DomainModel.tournament;

import DomainModel.user.User;

import java.time.LocalDate;
import java.util.List;

public class Tournament {
    private int tournamentId;
    private String tournamentName;
    private String description;
    private User organizer;          // molti-a-uno
    private int capacity;
    private LocalDate deadline;
    private LocalDate startDate;
    private TournamentStatus status;
    private List<Registration> registrations;
}
