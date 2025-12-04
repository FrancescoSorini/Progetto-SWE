# Progetto-SWE

Dopo le varie modifiche al progetto, faccio commit con l'interfaccia di IntelliJ e poi
Opzione 1: faccio da terminale
            git push origin main
per caricare le modifiche sul repo GitHub
oppure
Opzione 2: uso l'interfaccia di IntelliJ per fare il Commit and Push sul repo GitHub collegando
            l'account GitHub ad IntelliJ

# TODO: Aggiungere attributi alla classe Torneo
Aggiungere attributo "TournamentGameType" alla classe Torneo per specificare il tipo di gioco del torneo (es. Magic, Pokemon, Yu-Gi-Oh!).
Aggiornare di conseguenza i metodi costruttori, getter e setter della classe Torneo.
Aggiornare la documentazione della classe Torneo per includere il nuovo attributo.
Aggiornare i test unitari relativi alla classe Torneo per includere casi di test che verificano il corretto funzionamento del nuovo attributo "TournamentGameType".
Controllare se c'è bisogno di aggiornare le registrazioni:
- Se il torneo è di tipo "Magic", assicurarsi che gli utenti si registrino con un mazzo contenente solo carte di Magic.
- Aggiornare Schema ER per riflettere il nuovo attributo (relazione rombo con cards_type)

# TODO: AGGIORNARE IL CARD FACTORY

# TODO: Controllare con Kilo Code le modifiche fatte per GameType e Registrations
Verificare con Kilo Code che le modifiche apportate alla gestione del tipo di gioco (GameType) e alle registrazioni (Registrations) 
siano corrette e funzionino come previsto. In particolare controllare se la logica dell'aggiunta dell'id del mazzo alla registrazione 
è stata implementata correttamente e se è anche corretta logicamente.

# TODO: Implementare i cambiamenti nella business logic
Aggiornare la logica di business per gestire il nuovo attributo "TournamentGameType" nella creazione e gestione dei tornei.
Assicurarsi che le funzionalità esistenti funzionino correttamente con il nuovo attributo.
Aggiornare eventuali servizi o controller che interagiscono con la classe Torneo per includere il nuovo attributo.

# TODO: Rifare i test del DAO