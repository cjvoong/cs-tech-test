# ClearScore - Backend Technical Test

The credit cards service has been implemented in Scala using the [Play Framework](https://www.playframework.com/).

## Building

The project can be built (including test) using [sbt](http://www.scala-sbt.org/).  
```bash
sbt test
```

To run the app simply run the `start.sh` script. Make sure both environment variables `CSCARDS_ENDPOINT` and `SCOREDCARDS_ENDPOINT` are set

## App design

### Controller

- CreditCardsController.scala:

  Handles incoming HTTP requests and delegates them to its service component.

### Service

- CreditCardsService.scala:

  Responsible for collating credit card information from partner APIs and calculating a score for each card.

### Model

  Contains the different model classes.

## Production deployment
  
  Running `sbt dist` builds a binary version of the app that can be deployed to a server without any external dependencies. This produces a ZIP file in the `target\universal` directory. The file contains an executable script which can be used to start the app.  
