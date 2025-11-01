package DomainModel.card;

public enum CardType {
    YUGIOH("Yu-Gi-Oh"),
    MAGIC("Magic the Gathering"),
    POKEMON("Pokémon");

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
