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

## Services

## Controllers

## Testing 

