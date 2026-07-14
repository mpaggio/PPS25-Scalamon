# Specifica dei requisiti

## Requisiti di business:
(Devono chiarire come si può giudicare se il progetto ha avuto successo)

### BR-01: Simulazione di combattimenti tattici:
Il sistema deve permettere a due giocatori di affrontarsi in combattimenti Pokémon 1v1 a turni, mettendo a disposizione meccaniche strategiche basate su:
- Composizione della squadra.
- Scelta delle mosse.
- Gestione delle risorse.
- Vantaggi e svantaggi dei tipi.
- Condizioni ambientali.
- Stati alterati.

### BR-02: Personalizzazione della squadra:
Il sistema deve consentire ai giocatori di costruire la propria squadra composta da 6 Pokémon unici. La creazione deve poter avvenire attraverso:
- Generazione casuale automatizzata.
- Configurazione manuale tramite selezione dei Pokémon e delle mosse.

### BR-03: Esperienza hot-seat:
Il sistema deve supportare una modalità multiplayer locale nella quale due utenti utilizzano lo stesso dispositivo. Durante la fase di selezione delle azioni:
- Il giocatore corrente deve poter effetturare la propria scelta.
- Il giocatore avversario deve essere temporaneamente escluso dalla visualizzazione delle informazioni strategiche.

### BR-04: Riproduzione coerente delle regole di combattimento:
Il simulatore deve garantire una risoluzione delle battaglie coerente con le regole definite dal dominio Pokémon implementato. Ogni turno deve essere risolto in modo deterministico secondo:
- Priorità delle azioni.
- Velocità dei Pokémon.
- Effetti ambientali.
- Stati alterati.
- Condizioni di KO.

## Requisiti funzionali:
(Descrivono le operazioni che il sistema deve essere in grado di eseguire).

### Gestione del dominio

#### FR-01: Gestione Pokémon
Il sistema deve rappresentare ogni Pokémon attraverso:
- Informazioni statiche:
  - Nome.
  - Tipo/i.
  - Statistiche di base (HP, attacco, difesa, attacco speciale, difesa speciale, velocità).
- Informazioni dinamiche:
  - Punti salute correnti.
  - Stati alterati attivi.
  - Modificatori delle statistiche.
  - Mosse disponibili e PP rimanenti per ogni mossa.

#### FR-02: Gestione delle mosse
Ogni Pokémon deve poter possedere quattro mosse. Le mosse devono essere suddivise in:
- Mosse dannose, caratterizzate da:
  - Potenza base.
  - Categoria fisica o speciale.
  - Precisione.
  - Tipo.
  - Il danno deve essere calcolato considerando:
    - Le statistiche dell'attaccante e del difensore.
    - STAB.
    - Efficacia del tipo.
    - Modificatori esterni.
- Mosse status, che devono poter:
  - Modificare gli stage delle statistiche.
  - Applicare stati alterati.
  - Modificare l'ambiente.

#### FR-03: Gestione degli strumenti consumabili
Il sistema deve fornire ad ogni giocatore un kit iniziale di strumenti consumabili. Gli strumenti devono:
- Essere generati casualmente all'inizio della partita.
- Poter essere utilizzati manualmente durante il turno.
- Consumarsi dopo l'utilizzo.

#### FR-04: Gestione dell'ambiente
All'avvio della partita il sistema deve generare una condizione ambientale casuale. L'ambiente deve:
- Influenzare il calcolo del danno.
- Poter esseree modificato da specifiche abilità.
- Essere condiviso da entrambi i giocatori.

### Gestione della partita:

#### FR-05: Creazione della squadra
Il sistema deve permettere la creazione di due squadre composte da:
- Esattamente 6 Pokémon.
- Pokémon non duplicati all'interno della stessa squadra.
- 4 mosse per ogni Pokémon.

#### FR-06: Generazione automatica del team
La generazione automatica del team deve poter prevedere due possibili logiche:
- Generazione completamente casuale, che seleziona randomicamente 6 Pokémon dal database e assegna ad ognuno 4 mosse random, senza tenere conto di alcun fattore.
- Generazione affine, che seleziona randomicamente 6 Pokémon dal database e ad ognuna assegna 2 mosse dello stesso tipo e 2 mosse che gli permettono di avere una copertura di attacco su tipi su cui (con le prime 2 mosse) non avrebbe molto effetto.

#### FR-07: Creazione manuale del team
La creazione manuale del team deve consentire:
- Visualizzazione dei Pokémon disponibili.
- Scelta dei Pokémon (uno alla volta) tra quelli disponibili.
- Consultazione delle statistiche.
- Visualizzazione delle mosse disponibili.
- Scelta delle 4 mosse (una alla volta) per ciascun Pokémon tra quelli scelti (preso uno alla volta).

#### FR-08: Gestione del turno
Durante ogni turno, ogni giocatore deve poter scegliere una sola azione tra:
- Utilizzo di una mossa del Pokémon in campo.
- Cambio del Pokémon corrente con uno in panchina.
- Utilizzo di uno strumento.

#### FR-09: Risoluzione automatica del turno
Il motore di combattimento deve risolvere le azioni secondo il seguente ordine:
1) Effetti iniziali.
2) Strumenti / Sostituzioni.
3) Mosse.
4) Effetti di fine turno.
In caso di conflitto, se si tratta di due mosse deve utilizare la velocità del Pokémon come criterio di spareggio, mentre se si tratta di strumento/sostituzione allora li esegue nell'ordine di scelta (prima il giocatore 1 e poi il giocatore 2).

#### FR-10: Gestione degli stati alterati
Il sistema deve supportare i seguenti stati alterati:
- Bruciatura.
- Veleno.
- Paralisi.
- Sonno.
- Congelamento.
Ogni stato può prevedere:
- Modifiche statistiche.
- Limitazioni sulle azioni.
- Danni periodici.

#### FR-11: Gestione KO e fine partita
Il sistema deve rilevare automaticamente il KO di un Pokémon e chiederne l'immediata sostituzione. Il sistema deve anche dichiarare il vincitore quando una squadra perde tutti i Pokémon.

## Requisiti non funzionali:

### NFR-01: Usabilità
L'interfaccia deve permettere agli utenti di comprendere facilmente lo stato della battaglia, selezionare rapidamente le azioni e visualizzare informazioni rilevanti sullo svolgimento della battaglia.

### NFR-02: Affidabilità
Il sistema deve garantire la coerenza dello stato della partita, l'assenza di transizioni impossibili e il corretto aggiornamento dei dati.

### NFR-03: Manutenibilità
Il codice deve essere organizzato in componenti e le logiche principali devono essere separate tra dominio, gestione della logica, motore di combattimento e interfaccia grafica.

### NFR-04: Estendibilità
Il sistema deve permettere future estensioni quali:
- Nuove mosse.
- Nuovi Pokémon.
- Nuovi ambienti.
- Nuovi strumenti.
L'aggiunta di nuovi elementi non deve richiedere modifiche significative al motore principale.

### NFR-05: Performance
Le operazioni durante il combattimento devono essere eseguite in tempo reale. Il sistema deve garantire tempi di risposta ridotti durante la selezione delle azioni, il calcolo del danno e l'aggiornamento dello stato.

## Requisiti di implementazione:

### IR-01: Linguaggio di sviluppo
Il sistema deve essere sviluppato utilizzando Scala 3 come linguaggio principale.

### IR-02: Paradigma architetturale
L'implementazione deve seguire un approccio funzionale e deve prevedere l'utilizzo di elementi funzionali avanzati.