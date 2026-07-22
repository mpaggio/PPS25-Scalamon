## Testing

Per la fase di testing il team ha utilizzato **ScalaTest**, che nel progetto si è dimostrato particolarmente adatto grazie alla sintassi espressiva e alla possibilità di scrivere test leggibili e molto vicini alla specifica del comportamento atteso. In particolare, abbiamo privilegiato uno stile descrittivo basato su `AnyFunSuite` e sui `Matchers`
(come ad esempio shouldBe, shouldEqual, ...), così da rendere la suite di test anche una forma di documentazione eseguibile del dominio e delle sue regole.

Lo sviluppo non ha seguito in maniera rigorosa un approccio **Test Driven Development (TDD)**, sebbene ne abbia ripreso alcuni principi. In particolare, il progetto è stato sviluppato in modo incrementale: a ogni nuova funzionalità o componente implementata veniva affiancata una corrispondente suite di test, con l'obiettivo di verificarne il comportamento prima di procedere con ulteriori estensioni. Ciò ha permesso di validare progressivamente le diverse parti del sistema e di individuare tempestivamente eventuali regressioni. Per ciascun modulo sono stati realizzati sia test relativi ai casi d'uso principali, sia test dedicati a scenari limite e condizioni eccezionali. Questo approccio ha favorito una progettazione più modulare e ha reso più semplice verificare in isolamento le singole componenti, mantenendo nel tempo un buon livello di affidabilità del codice.

La **Test Suite** copre in modo esteso i principali elementi del dominio, tra cui:
- la correttezza delle statistiche proprie di ogni Pokémon;
- la correttezza della modellazione di mosse, effetti e stati alterati;
- il comportamento delle abilità e la verifica dei loro trigger nei momenti specifici della battaglia;
- l’evoluzione dello stato di battaglia in risposta all’uso di mosse, strumenti e condizioni ambientali.

Un aspetto rilevante è che i test non si limitano ai soli casi base, ma includono anche **vincoli, eccezioni e scenari limite**. Ad esempio, vengono verificati i controlli sui valori non validi, il corretto consumo dei PP dopo l’uso di una mossa, l’applicazione condizionata di effetti come guarigione, contraccolpo o alterazioni di stato, e il comportamento in presenza di condizioni come nebbia o pioggia. Questo ci ha permesso di intercettare rapidamente regressioni e incoerenze nella logica di gioco, che sono poi state risolte di conseguenza.

Per quanto riguarda la **copertura** invece, la strategia adottata mira a testare la **logica core** del progetto, cioè tutto ciò che riguarda dominio, calcolo degli effetti e transizioni di stato. Per monitorare questo aspetto abbiamo utilizzato lo strumento **scoverage**, in modo da misurare la copertura del codice e verificare che le componenti fondamentali fossero effettivamente esercitate dalla suite di test (tenendoci come minimo una percentuale del 50%).

La parte grafica Swing è stata invece verificata soprattutto tramite integrazione e testing manuale, in quanto fortemente legata all’interazione utente e quindi meno adatta a test unitari puri.

