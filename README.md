# Sistedes Digital Library Manager

The _Sistedes Digital Library Manager_ is a utility program that (semi-)automates the process of producing the proceedings of the Sistedes conferences. To do so, it can import an Excel (XSLX) dump of the conference data from EasyChair.

The process must be manually monitored and inspected. Manual modifications in the intermediate files are required as well.

The process to produce a new set of proceedings is typically as follows:

0. **Import the conference data** from EasyChair using the `init` command. A set of JSON metadata files will be created with the conference data.

1. **Manually adjust** the generated JSON files to complete the information required by the proceedings. Some actions that must be taken are:

    * Adjust the edition information in the edition file.

    * Add the needed _Preliminaries_ for the conference proceedings (typically _Preface_, _Commitees_, and _Invited talk_). Pay special attention to the `id` and `filename` properties to avoid overwriting files on each execution.

    * Double check the titles of the submissions (remove clarifications between parentheses about the type of paper --e.g., abstract, summary, etc.--).

    * NOTE: the `submissions` property inside the authors' signature in submission files is not used to produce the proceedings, and it is included only for informative purposes while manually editing the files.

2. **Synchronize the authors' list** with the Sistedes Digital Library using the `sync-authors` command. This process will try to identify which authors already exist in the database. If so, the match will be stored locally using the `sistedesUuid` property. Non-existing authors will be created, and the `sistedesUuid` of the newly created author will be stored too. New authors can be created either "public" or "private" (i.e., visible only by administrators). 

3. **Publish the proceedings**. Once the authors have been matched/created, the proceedings can be produced using the `publish` command. The documents are published one at a time, and the process can take a few minutes. The proceedings can be created either "public" or "private".

4. **Create the Sistedes Handles in the Handle server**. Run the _Register external Handle_ ("Registrar Handle externo") curation task in the needed communities (i.e., those listed in Step 5).

5. **Create thumbnails for the published documents (optional)**. Run the _Filter media_ ("Filtrar medios") curation task in the needed communities (i.e., those listed in Step 7). If the task is not manually executed, it will be eventually executed automatically as part of the maintenance tasks.

6. **Make authors public (if they were created as private)**. If authors are created as private, they must be published using the _Make public_ ("Hacer público") curation task on the `Archivo documental de Sistedes > Autores` collection.

7. **Make the proceedings public (if they were created as private)**. If the proceedings are created as private, they must be published using the _Make public_ ("Hacer público") curation task on the corresponding communities:

* `Jornadas de Ciencia e Ingeniería de Servicios (JCIS) > JCIS <YEAR> (<Location>)`
* `Jornadas de Ingeniería del Software y Bases de Datos (JISBD) > JISBD <YEAR> (<Location>)`
* `Jornadas sobre Programación y Lenguajes (PROLE) > PROLE <YEAR> (<Location>)`
* `Archivo documental de Sistedes > Jornadas Sistedes > Sistedes <YEAR> (<Location>)` if there are documents that are shared among them (as is typically the case in CEDI editions).

8. **List the new proceedings in the home page**. In order to make the new proceedings visible in the _Highlighted proceedings_ ("Jornadas destacadas") section in the home page, the `/opt/dspace.ui/config/config.prod.yml` must be updated accordingly.


## Requirements

This program requires **Java 17** or higher to be executed.

This program requires **maven** and **Java 17** to be built.

## Building

To build the Sistedes Library Manager, simply run `mvn package` inside the `es.sistedes.library.manager` directory:

```
cd es.sistedes.library.manager
mvn package
```

The executable files will be placed inside the `target` folder.

**Important:** the `lib` directory with the dependencies must be distributed _as is_ (i.e., without renaming it or any files inside) together with the `library.manager-<VERSION>.jar` file.

## Running the tool in batch mode

It is possible to run the aforementioned commands in batch mode for all the Sistedes conferences using the `execute.bat` script. To do so:

1. Make a copy of `execute.env.bat.TEMPLATE` and name it `execute.env.bat`.

2. Adjust all the environment variables to the conferences and editions to be published.

3. Run `execute.bat` using any of the following subcommands: `init`, `sync-authors`, `publish`, `validate`.

## Command line interface

All the different commands can be executed from a single tool: the `library.manager-<VERSION>.jar`.

Below you can find the main usage options:

```
Usage: java -jar <this-file.jar> [-hV] [COMMAND]
Missing required subcommand
Usage: java -jar <this-file.jar> [-hV] [COMMAND]
Manage the Sistedes Digital Library.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  init                Initializes the JSON files required to generate the
                        proceedings of a Sistedes conference from EasyChair
                        data.
  sync-authors        Synchronizes the authors information between the local
                        submissions and the Sistedes Digital Library, trying to
                        match existing authors in the library with local
                        authors. In case the authors do not exist in the
                        library, creates them. Already identified authors will
                        be skipped when running in normal mode. In forced mode,
                        information about  already identified authors will be
                        discarded and a new match will be attempted.
  validate            Validates that the conference data is ready for
                        submission without performing any modification.
  publish             Publishes the specified edition in the Sistedes Digital
                        Library. Published elements will be recorded locally to
                        avoid recreating them.
  list                Generates different listings of the conference data.
  split               (EXPERIMENTAL) Splits a single PDF file containing the
                        full proceedings of a conference and sets up the JSON
                        files required to generate the proceedings in the new
                        Digital Library.
  register-handles    (DEPRECATED) Registers the Sistedes Handles such that
                        they point to the Digital Library internal Handles.
  dump-handles-batch  (DEPRECATED) Dumps a set of Handle commands that can be
                        run as a batch file to (optionally delete) and create
                        all the Handles in the specified edition.
```

Next, we describe the CLI options for each subcommand.

### Initiliaze metadata (`init`)

As aforementioned, this command takes an EasyChair dump file, and initializes a set of JSON files (together with the corresponding submission files) in the specified directory. These JSON files can be later refined to produce the full proceedings.

```
Usage: java -jar <this-file.jar> init [-F] -a=ACRONYM [-i=DIR] [-o=DIR]
                                      [-p=PATTERN] -P=PREFIX -x=FILE -y=YEAR
Initializes the JSON files required to generate the proceedings of a Sistedes
conference from EasyChair data.
  -a, --acronym=ACRONYM   Acronym of the conference to be prepared.
  -F, --force             Force execution, even if submission files are
                            overwritten.
  -i, --input=DIR         Input directory where the source PDF files must be
                            looked for.
  -o, --output=DIR        Ouput directory where the generated conference files
                            should be placed.
  -p, --pattern=PATTERN   Pattern describing the names of the submission files.
                            {acronym} {year} and {id} will be substituted by
                            the corresponding values. Default value is {acronym}
                            _{year}_paper_{id}.pdf.
  -P, --prefix=PREFIX     Handle prefix.
  -x, --xslx=FILE         XSLX file as downloaded from the EasyChair
                            'Conference data download' menu.
  -y, --year=YEAR         Year of the edition to be prepared.
```


### Synchronize authors (`sync-authors`)

Synchronizes the authors information between the local submissions and the Sistedes Digital Library, trying to match existing authors in the library with local authors. In case the authors do not exist in the library, it creates them. Identifiers of the authors in the _Sistedes Digital Library_ (whether they are newly created or already existing) will be saved locally for a later use during the publication of the proceedings. In case of doubt, and when running in `interactive` the user will be asked whether found authors are a match or not. This command may take some time.

```
Usage: java -jar <this-file.jar> sync-authors [-aFir] -e=E-MAIL -f=DIR
       -p=PASSWORD -u=URI
Synchronizes the authors information between the local submissions and the
Sistedes Digital Library, trying to match existing authors in the library with
local authors. In case the authors do not exist in the library, creates them.
Already identified authors will be skipped when running in normal mode. In
forced mode, information about  already identified authors will be discarded
and a new match will be attempted.
  -a, --admin-only          Create new authors with administrator-only
                              permissions (i.e., hidden to the general public).
  -e, --email=E-MAIL        E-mail of the account required to log in the
                              Sistedes Digital Library to create the authors.
  -f, --edition-file=DIR    JSON file including the conference edition metadata.
  -F, --force               Force execution, discarding existing information
                              about identified authors already existing in the
                              Sistedes Digital Library.
  -i, --interactive         Ask interactively whether the found element (when
                              in doubt) is a match or not.
  -p, --password=PASSWORD   Password of the account in the Sistedes Digital
                              Library.
  -r, --dry-run             Do not perform any modifications.
  -u, --uri=URI             URI of the root endpoint of the DSpace API.
```

### Validate conference data (`validate`)

Perform some basic validations of the conference data, specially checking that critical information required during the publication phase is not missing.

```
Usage: java -jar <this-file.jar> validate -f=FILE
Validates that the conference data is ready for submission without performing
any modification.
  -f, --edition-file=FILE   JSON file including the conference edition metadata.
```

### Publish proceedings (`publish`)

Publishes the conference proceedings in the Sistedes Digital Library, publishing one document at a time. This may take some time.

```
Usage: java -jar <this-file.jar> publish [-aF] -e=E-MAIL -f=FILE -p=PASSWORD
       -u=URI
Publishes the specified edition in the Sistedes Digital Library. Published
elements will be recorded locally to avoid recreating them.
  -a, --admin-only          Publish with administrator-only permissions (i.e.,
                              hidden to the general public).
  -e, --email=E-MAIL        E-mail of the account required to log in the
                              Sistedes Digital Library to create the authors.
  -f, --edition-file=FILE   JSON file including the conference edition metadata.
  -F, --force               Force execution, even if elements have been already
                              created or if validation errors exist.
  -p, --password=PASSWORD   Password of the account in the Sistedes Digital
                              Library.
  -u, --uri=URI             URI of the root endpoint of the DSpace API.
```

### Produce different listings (`list`)

Produce some listings that may be useful to detect inconsistencies and errors in the metadata files. If multiple listings are specified, they will be shown in a rown.

```
Usage: java -jar <this-file.jar> list [-ent] -f=FILE
Generates different listings of the conference data.
  -e, --authors-with-different-emails
                            List the authors that have more than one different
                              e-mail in his/her signature.
  -f, --edition-file=FILE   JSON file including the conference edition metadata.
  -n, --authors-with-different-names
                            List the authors that have more than one different
                              name in his/her signature.
  -t, --paper-titles        List all the titles of the papers.
```

### EXPERIMENTAL: Split PDF file (`split`)

Splits a single PDF file containing the full proceedings of a conference and sets up the JSON files required to generate the proceedings in the new Digital Library.

```
Usage: java -jar <this-file.jar> split [-F] -a=ACRONYM [-c=PAGES] [-f=PAGES]
                                       -i=FILE [-o=DIR] -P=PREFIX [-u=URL]
                                       -y=YEAR
(EXPERIMENTAL) Splits a single PDF file containing the full proceedings of a
conference and sets up the JSON files required to generate the proceedings in
the new Digital Library.
  -a, --acronym=ACRONYM   Acronym of the conference to be prepared.
  -c, --contributions-pages=PAGES
                          List of the pages where each contribution starts and,
                            optionally, ends, if a range (inclusive) is
                            specified. Pages (or ranges) separated by comma
                            denote contributions in the same session/track.
                            Pages (or ranges) separated by semicolons denote
                            papers in different sessions/tracks. The last
                            element of the list must always be a range. E.g:
                            10,15,20;25-26,27-30;31,35-40
  -f, --frontmatter-pages=PAGES
                          Comma-separated list of pages where each frontmatter
                            section starts and optionally, ends, if a range
                            (inclusive) is specified. The last element of the
                            list must always be a range. E.g.: 1,3,4-5
  -F, --force             Force execution, even if submission files are
                            overwritten.
  -i, --input=FILE        Input PDF file with the full proceedings.
  -o, --output=DIR        Ouput directory where the generated conference files
                            should be placed.
  -P, --prefix=PREFIX     Handle prefix.
  -u, --grobid-url=URL    Grobid service URL.
  -y, --year=YEAR         Year of the edition to be prepared.
```

### DEPRECATED: ~~Register Handles (`register-handles`)~~

**No longer needed, since Handle registration can be directly done in DSpace as a Curation Task.**

Registers the Handles of the newly published documents in the Sistedes Handle server one at a time. This may take some time.

```
Usage: java -jar <this-file.jar> register-handles -f=FILE -k=FILE [-p=PASSWORD]
       -P=PREFIX
(DEPRECATED) Registers the Sistedes Handles such that they point to the Digital
Library internal Handles.
  -f, --edition-file=FILE   JSON file including the conference edition metadata.
  -k, --key-file=FILE       File with the secure key to authenticate in the
                              Handle system.
  -p, --password=PASSWORD   Password to unlock the key file.
  -P, --prefix=PREFIX       Handle prefix.
```

### DEPRECATED: ~~Dump Handle commands in a batch file (`dump-handles-batch`)~~

**No longer needed, since Handle registration can be directly done in DSpace as a Curation Task.**

Dump a batch text file with all the commands to (optionally delete) and create the Handles. This batch file can be later used in `hdl-admintool` to update all the Handles at the same time.

```
Usage: java -jar <this-file.jar> dump-handles-batch [-d] -f=FILE [-o=FILE]
       -P=PREFIX
(DEPRECATED) Dumps a set of Handle commands that can be run as a batch file to
(optionally delete) and create all the Handles in the specified edition.
  -d, --delete              Also issue an initial DELETE command in order to
                              CREATE the Handles from scratch.
  -f, --edition-file=FILE   JSON file including the conference edition metadata.
  -o, --output=FILE         If specified, the Handle commands will be saved in
                              FILE rather than shown in stdout.
  -P, --prefix=PREFIX       Handle prefix.
```

## License

This program is licensed under the _Eclipse Public License v2.0_.

## Authorship

Sistedes Digital Library (https://biblioteca.sistedes.es/about).