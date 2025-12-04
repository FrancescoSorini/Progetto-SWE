package DomainModel;

public enum GameType {
    MAGIC(1),
    POKEMON(2),
    YUGIOH(3);

    private final int id;

    GameType(int id) { this.id = id; }

    public int getGameId() { return id; }

    public static GameType fromId(int id) {
        return switch (id) {
            case 1 -> MAGIC;
            case 2 -> POKEMON;
            case 3 -> YUGIOH;
            default -> throw new IllegalArgumentException("Invalid game type id: " + id);
        };
    }
}

