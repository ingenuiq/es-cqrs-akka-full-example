# Event sourcing / CQRS Note example Service

Application is built in CQRS (Command Query Responsibility Segregation) fashion:
- Command side
    - Akka persistence
    - Cassandra as event store
- Query side
    - Akka persistence query for event store to view store transfer
    - Slick
    - Postgres as a view store

Application will constantly read from the event store and keep query side in sync. This model has Eventual consistency which means once the command is executed the view will eventually be updated. 

## How to build and run

Dependencies: Java 8 & sbt

### Running with full authorization service (being able to login as different user)

- Run `docker-compose -f docker-compose-essential.yml up`
- From the root directory run `sbt run`
- Go to http://localhost:9001/ to test the application

### Example of API usage

##### Creation of new note
Request:

    curl -X POST \
      http://localhost:9001/api/note \
      -H 'Content-Type: application/json' \
      -d '{
    	"title": "Some title",
    	"published": false,
    	"subTitle": "Some subtitle"
    }'

Response:

    {
        "noteId": "af8a7f3b-67ca-45cd-87d1-44438f1d3f58",
        "correlationId": "6238f89e05c205be"
    }

#### Fetch individual note
Request:

    curl -X GET http://localhost:9001/api/query/note/af8a7f3b-67ca-45cd-87d1-44438f1d3f58

Response:

    {
        "note": {
            "id": "af8a7f3b-67ca-45cd-87d1-44438f1d3f58",
            "title": "Some title",
            "subTitle": "Some subtitle",
            "published": false
        },
        "correlationId": "aa1c3e6e633ee8a5"
    }


### Schema evolution!!!

All Akka persistence events are generated from .avsc files located in `src/main/resources/avro/avsc`. 

All events that you see changed in `avsc` folder needs to be versioned properly if we want to avoid breaking changes in environments.

Tricky thing to do here is to take the version that was there before and copy it to `avsc-history` folder.

##### Example

You need to make a change in Note object which is within NoteCreated event. 

1. Go to `src/main/resources/avro` folder and find in which .avdl file is this event modelled 
2. Change the record by adding the new field `WITH default value`. If default value isn't added old events won't be able to reply. There is no changing of current fields or types. Only addition.
3. Execute command `sbt generateAvsc`
4. Notice that in `src/main/resources/avro/avsc` there are multiple files changed NoteAvro, NoteCreatedAvro and NoteUpdatedAvro. NoteAvro.avsc isn't really used as standalone object so we leave it as is. NoteCreatedAvro and NoteUpdatedAvro both use it internally so we need to version these events.
5. We need to copy previous versions (ones before you ran command in step 3, just use history to fetch it) and copy it into `src/main/resources/avro/avsc-history` in a folder with JIRA number ie EP-1234. Just copy the previous versions there. Nothing else.
6. Execute command `sbt compile` and boom, new version of Avro events is generated.
