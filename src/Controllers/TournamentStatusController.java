package Controllers;

import Services.tournament.TournamentService;

import java.sql.SQLException;

public class TournamentStatusController {

    private final TournamentService tournamentService;

    public TournamentStatusController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    public void syncTournamentStatuses() throws SQLException {
        tournamentService.updateTournamentStatusesAutomatically();
    }
}
