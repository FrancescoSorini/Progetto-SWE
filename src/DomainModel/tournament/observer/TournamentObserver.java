package DomainModel.tournament.observer;

import DomainModel.tournament.Tournament;

public interface TournamentObserver {
    void update(Tournament tournament);
}
