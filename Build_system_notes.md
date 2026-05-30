# APPUNTI SUL SETTING DI SBT

## Architettura:
Il progetto prevede una struttura organizzata in:
- File `build.sbt`: contiene la logica del progetto (dipendenze e settings).
- Cartella `project`: contiene i plugin SBT (*scoverage*, *wartremover* e *scalafix*).
- Cartella `target`: contiene l'output di compilazione generato.

## File di configurazione:
- `build.sbt` definisce: 
  - La versione di scala (`scalaVersion := "3.4.2"`).
  - Il nome e la versione del progetto (`name := "PPS25-Scalamon`, `version := "0.1.0`).
  - Le dipendenze (`libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test`)
  - SemanticDB (analisi del codice e refactor, `semanticdbEnabled := true`, `semanticdbVersion := scalafixSemanticdb.revision`).
  - ScalacOptions (`scalacOptions ++= Seq("-Wunused:all")`, attiva warning per variabili inutilizzate, import inutilizzati e parametri inutilizzati).
- `project/plugins.sbt` definisce i plugin SBT esterni:
  - Scoverage (`addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")`, misura quanto il codice è testato).
  - Wartremover (`addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.1.6")`, trova *code smell*).
  - Scalafix (`addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.0")`, refactoring automatico e regole).

## Flusso di lavoro:
1) Scrivere codice scala.
2) Compilare tramite `sbt compile` (controlla errori, applica scalacOptions e geneera bytecode).
3) Eseguire l'applicativo tramite `sbt run`.
4) Lanciare i test tramite `sbt test`.
5) Generare il report di coverage tramite `sbt coverage test coverageReport` (genera report HTML).
6) Quality check tramite `sbt scalafix` e `sbt wartremover`.