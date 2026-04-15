package Controllers;

import Controllers.session.UserSession;
import Services.tournament.TournamentService;
import Services.tournament.TournamentStatusChangeEvent;

import java.sql.SQLException;
import java.util.List;

public class TournamentStatusController {

    private final TournamentService tournamentService;

    public TournamentStatusController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    public void syncTournamentStatuses() throws SQLException {
        List<TournamentStatusChangeEvent> events = tournamentService.updateTournamentStatusesAutomatically();
        for (TournamentStatusChangeEvent e : events) {
            if (e.getRegisteredUserIds().isEmpty()) {
                continue;
            }

            String message = "Il torneo ID " + e.getTournamentId()
                    + " - Nome: " + e.getTournamentName()
                    + " ha cambiato stato da " + e.getOldStatus() + " a " + e.getNewStatus() + ".";

            for (int userId : e.getRegisteredUserIds()) {
                UserSession.addNotificationForUser(userId, message, e.getGameType());
            }
        }
    }
}
