### Organizzazione del codice
I quattro meccanismi si riflettono direttamente nella struttura a package presentata nell'architettura:
`domain` contiene i dati puri e le definizioni dichiarative
(Pokédex, mosse, abilità, oggetti, tabella dei tipi, spesso espressi tramite piccoli DSL interni);
`logics` contiene lo stato immutabile con i suoi moduli e combinatori, il team building e l'orchestrazione
dei turni; `app` contiene il game loop monadico e la porta `GameView`;
`view` contiene l'adapter Swing; `util` la State monad, priva di dipendenze.
Le dipendenze fluiscono in un'unica direzione, verso il dominio, e l'unico attraversamento in senso opposto
è l'implementazione della porta da parte della vista.
