package DomainModel.tournament.observer;

import DomainModel.tournament.Tournament;
import DomainModel.user.User;

public class UserObserver implements TournamentObserver {
    private User user;

    public UserObserver(User user) {
        this.user = user;
    }

    @Override
    public void update(Tournament tournament) {
        System.out.println("Notifica a " + user.getUsername() +
                ": Il torneo '" + tournament.getName() +
                "' ha cambiato stato in " + tournament.getStatus());
    }
}
