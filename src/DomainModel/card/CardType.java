package DomainModel.card;

public enum CardType {
    MAGIC("Magic the Gathering"),
    POKEMON("Pokémon"),
    YUGIOH("Yu-Gi-Oh")
    ;

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getId() {
        return this.ordinal() +1; // Ritorna 1 per MAGIC, 2 per POKEMON, 3 per YUGIOH
    }
}
