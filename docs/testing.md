## Testing

Per la fase di testing il team ha utilizzato **ScalaTest**, che nel progetto si è dimostrato particolarmente adatto grazie alla sintassi espressiva e alla possibilità di scrivere test leggibili e molto vicini alla specifica del comportamento atteso. In particolare, abbiamo privilegiato uno stile descrittivo basato su `AnyFunSuite` e sui `Matchers`
(come ad esempio shouldBe, shouldEqual, ...), così da rendere la suite di test anche una forma di documentazione eseguibile del dominio e delle sue regole.

Lo sviluppo è stato condotto con approccio **Test Driven Development (TDD)**: come imparato durante il corso, per molte componenti abbiamo prima definito i casi di verifica e poi implementato la logica corrispondente. Questo ha favorito una progettazione modulare che ha reso più semplice testare in modo isolato le singole parti del sistema.

La **Test Suite** copre in modo esteso i principali elementi del dominio, tra cui:
- la correttezza delle statistiche proprie di ogni Pokémon;
- la correttezza della modellazione di mosse, effetti e stati alterati;
- il comportamento delle abilità e la verifica dei loro trigger nei momenti specifici della battaglia;
- l’evoluzione dello stato di battaglia in risposta all’uso di mosse, strumenti e condizioni ambientali.

Un aspetto rilevante è che i test non si limitano ai soli casi base, ma includono anche **vincoli, eccezioni e scenari limite**. Ad esempio, vengono verificati i controlli sui valori non validi, il corretto consumo dei PP dopo l’uso di una mossa, l’applicazione condizionata di effetti come guarigione, contraccolpo o alterazioni di stato, e il comportamento in presenza di condizioni come nebbia o pioggia. Questo ci ha permesso di intercettare rapidamente regressioni e incoerenze nella logica di gioco, che sono poi state risolte di conseguenza.

Per quanto riguarda la **copertura** invece, la strategia adottata mira a testare la **logica core** del progetto, cioè tutto ciò che riguarda dominio, calcolo degli effetti e transizioni di stato. Per monitorare questo aspetto abbiamo utilizzato lo strumento **scoverage**, in modo da misurare la copertura del codice e verificare che le componenti fondamentali fossero effettivamente esercitate dalla suite di test (tenendoci come minimo una percentuale del 50%).

La parte grafica Swing è stata invece verificata soprattutto tramite integrazione e testing manuale, in quanto fortemente legata all’interazione utente e quindi meno adatta a test unitari puri.

