package DomainModel.tournament;

public enum TournamentStatus {
    PENDING,
    APPROVED,
    REJECTED,
    READY,
    CLOSED;

    public boolean isFinalState() {
        return this == REJECTED || this == CLOSED;
    }
}
