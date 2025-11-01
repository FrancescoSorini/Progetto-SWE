package DomainModel.tournament.observer;

import DomainModel.tournament.Tournament;
import java.util.ArrayList;
import java.util.List;

public class TournamentSubject {
    private final List<TournamentObserver> observers = new ArrayList<>();

    public void addObserver(TournamentObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TournamentObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Tournament tournament) {
        for (TournamentObserver observer : observers) {
            observer.update(tournament);
        }
    }
}
