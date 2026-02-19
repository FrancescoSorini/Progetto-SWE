# Progetto-SWE

Abbiamo strutturato il progetto in Domain Model, DAO e Business Logic.
## Domain Model
Il Domain Model rappresenta le entità principali del sistema, come 
Utente, Carta, Deck, Torneo, Registrazione e GameType. Ogni entità è definita in una classe separata all'interno della cartella `domain_model`.
Per l'oggetto carta abbiamo usato il design pattern Factory Method per creare diverse tipologie di carte in modo flessibile.
Per il torneo abbiamo implementato il design pattern Observer per notificare gli utenti iscritti sugli aggiornamenti del torneo.

## DAO
La cartella `dao` contiene le classi responsabili dell'accesso ai dati.
Abbiamo collegato il progetto ad un database PostgreSQL usando pgAdmin4.
Ogni classe DAO fornisce metodi per eseguire operazioni CRUD (Create, Read, Update, Delete) sulle entità del Domain Model.
Abbiamo poi fatto dei test per verificare il corretto funzionamento delle operazioni di accesso ai dati utilizando JUnit.

## Business Logic
La cartella `business_logic` è destinata a contenere la logica di business del sistema.
Si effettuano i vari controlli per interagire con gli oggetti del Domain Model e le operazioni sui dati tramite le classi DAO.
Ad esempio, le regole per la registrazione ai tornei, la gestione dei deck e l'autenticazione degli utenti saranno implementate qui.
Abbiamo preferito creare una classe apposita per gestire la sessione di un utente autenticato, 
in modo da mantenere traccia dello stato dell'utente e delle sue caratteristiche (se è Admin, Organizer o Player)
durante l'interazione con il sistema.

## Testing
Stiamo sviluppando l'interfaccia CLI per far funzionare l'applicazione.
Dobbiamo rifare i test, una volta che l'interfaccia sarà pronta, per verificare che tutte le funzionalità siano correttamente implementate e funzionanti.
Questo perchè durante lo sviluppo della CLI potrebbero venirci in mente nuove funzionilità da aggiungere o modificare, 
e quindi è importante assicurarsi che tutto funzioni come previsto dopo ogni modifica.
Specialmente per i test della Services.

## TODO CODICE
- Errore conteggio iscrizioni nella lista dentro "I miei tornei"
- Controllare che utente non veda tornei "scaduti" nella lista "Check Tornei"
- Automatizzare il cambio di status tornei quando diventano full o si supera la deadline / start date con una classe controller apposita
- Rimuovi scelta gametype da creazione torneo, in quanto si sceglie il game type al login
- Formattare data iscrizione tornei in modo più leggibile
-----------------------------------------------------------------------------------
- Correggere comportamenti errati nella CLI con messaggi di errore più chiari.
-----------------------------------------------------------------------------------
- Testare DomainModel, Services e Controllers
- Controlla anche test DAO

## TODO DOCUMENTAZIONE
- DatabaseConnection: esplicitare l'uso del singleton per gestire la connessione al database in modo efficiente e sicuro.
- Modifica Use Case Diagram con le nuove funzioni aggiunte nel main