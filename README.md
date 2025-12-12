# Progetto-SWE

Abbiamo strutturato il progetto in Domain Model, DAO e Business Logic (ancora in fase di sviluppo).
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
Ci manca ancora da fare i test di questa parte (sempre con JUnit).