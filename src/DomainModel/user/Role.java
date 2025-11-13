package DomainModel.user;

public enum Role {
    PLAYER,
    ADMIN,
    ORGANIZER;

    public int getRoleId() {
        switch (this) {
            case ADMIN:
                return 1;
            case ORGANIZER:
                return 2;
            case PLAYER:
                return 3;
            default:
                return 0;
        }
    }
}

