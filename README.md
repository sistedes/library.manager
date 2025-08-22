# Sistedes Digital Library Manager

The _Sistedes Digital Library Manager_ is a utility program that (semi-)automates the process of producing the proceedings of the Sistedes conferences. To do so, it can import an Excel (XSLX) dump of the conference data from EasyChair.

The process must be manually monitored and inspected. Manual modifications in the intermediate files are required as well.

The process to produce a new set of proceedings is typically as follows:

0. **Import the conference data** from EasyChair using the `init` command. A set of JSON metadata files will be created with the conference data.

1. **Manually adjust** the generated JSON files to complete the information required for the proceedings. Some actions that must be taken are:

    * Adjust the edition information in the edition file.

    * Add the needed _Preliminaries_ for the conference proceedings (typically _Preface_, _Commitees_, and _Invited talk_). Pay special attention to the `id` and `filename` properties to avoid overwriting files on each execution.

    * Use the `list` subcommand to generate listings and detect issues that must be manually fixed. E.g., remove clarifications between parentheses about the type of paper --e.g., abstract, summary, etc.--, detect authors with inconsistent e-mails, ORCIDs or names, etc.

    * NOTE: the `submissions` property inside the authors' signature in submission files is not used to produce the proceedings, and it is included only for informative purposes while manually editing the files.

2. **Synchronize the authors' list** with the _Sistedes Digital Library_ using the `sync-authors` command. This process will try to identify which authors already exist in the database. If so, the match will be stored locally using the `sistedesUuid` property. Non-existing authors will be created, and the `sistedesUuid` of the newly created author will be stored too. New authors can be created either "public" or "private" (i.e., visible only by administrators). 

    The synchronization can be aborted at any time. Authors that already have a `sistedesUuid` in its metadata file will be considered as already synchronized and will be skipped.

    Also note that, to produce the final proceedings, it is necessary to run the `sync-authors` command with the `--curate` option (or run the `curate-authors` command separately, or run the needed curation tasks manually from the DSpace UI).

3. **Publish the proceedings**. Once the authors have been matched/created, the proceedings can be produced using the `publish` command. The documents are published one at a time, and the process can take long time. The proceedings can be created either "public" or "private".

    The process can be stopped at any time without loosing its progress. Tracks and documents with a `sistedesUuid` will be considered as already uploaded and won't be recreated. Nevertheless, the documents will be inspected to check that bundles have been correctly uploaded and authorships have been correctly registered.

    To produce the final proceedings, it is necessary to run the `publish` command with the `--curate` option (or run the needed curation tasks manually from the DSpace UI).

4. **Make authors public (if they were created as private)**. If authors are created as private, they must be published using the _Make public_ ("Hacer público") curation task on the `Archivo documental de Sistedes > Autores` collection.

5. **Make the proceedings public (if they were created as private)**. If the proceedings are created as private, they must be published using the _Make public_ ("Hacer público") curation task on the corresponding communities:

* `Jornadas de Ciencia e Ingeniería de Servicios (JCIS) > JCIS <YEAR> (<Location>)`
* `Jornadas de Ingeniería del Software y Bases de Datos (JISBD) > JISBD <YEAR> (<Location>)`
* `Jornadas sobre Programación y Lenguajes (PROLE) > PROLE <YEAR> (<Location>)`
* `Archivo documental de Sistedes > Jornadas Sistedes > Sistedes <YEAR> (<Location>)` if there are documents that are shared among them (as is typically the case in CEDI editions).

6. **List the new proceedings in the home page**. In order to make the new proceedings visible in the _Highlighted proceedings_ ("Jornadas destacadas") section in the home page, the `/opt/dspace.ui/config/config.prod.yml` must be updated accordingly.


## Requirements

This program requires **Java 17** or higher to be executed.

This program requires **maven** and **Java 17** to be built.

## Building

To build the _Sistedes Digital Library Manager_, simply run `mvn package` inside the `es.sistedes.library.manager` directory:

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

3. Run `execute.bat` using any of the following subcommands: `init`, `list`, `sync-authors`, `publish`, `validate`.

## Command line interface

All the different commands can be executed from a single tool: the `library.manager-<VERSION>.jar`.

Below you can find the main usage options:

```
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
                        be skipped when running in normal mode.
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
  curate-authors      Launches all the curation tasks that may be applicable to
                        newly created authors in the Sistedes Digital Library.
                        Since the process is executed asynchonously by DSpace,
                        no feedback about the execution result is given. The
                        DSpace UI can be used to get more feedback.
```

Next, we describe the CLI options for each subcommand.

### Initiliaze metadata (`init`)

As aforementioned, this command takes an EasyChair dump file, and initializes a set of JSON files (together with the corresponding submission files) in the specified directory. These JSON files can be later refined to produce the full proceedings. It is necessary to specify which types of submissions (based on EasyChair form fields) are papers and which ones are abstracts using the `--abstracts` and `--papers` options.

```
Usage: java -jar <this-file.jar> init -a=ACRONYM [-i=DIR] [-o=DIR] [-p=PATTERN]
                                      -P=PREFIX -x=FILE -y=YEAR -A=KEY-VALUE
                                      [-A=KEY-VALUE]... -R=KEY-VALUE
                                      [-R=KEY-VALUE]...
Initializes the JSON files required to generate the proceedings of a Sistedes
conference from EasyChair data.
  -a, --acronym=ACRONYM    Acronym of the conference to be prepared.
  -A, --abstracts=KEY-VALUE
                           Form fields (in the form of 'key=value' with NO
                             SPACES around =) which denote that a given
                             submission is an abstract. E.g.
                             'Category=Published'. This parameter may be used
                             as many times as needed.
  -i, --input=DIR          Input directory where the source PDF files must be
                             looked for.
  -o, --output=DIR         Ouput directory where the generated conference files
                             should be placed. The directory MUST be empty.
  -p, --pattern=PATTERN    Pattern describing the names of the submission
                             files. {acronym} {year} and {id} will be
                             substituted by the corresponding values. Default
                             value is {acronym}_{year}_paper_{id}.pdf.
  -P, --prefix=PREFIX      Handle prefix.
  -R, --papers=KEY-VALUE   Form fields (in the form of 'key=value' with NO
                             SPACES around =) which denote that a given
                             submission is an abstract. E.g. 'Category=Full
                             Paper'.This parameter may be used as many times as
                             needed.
  -x, --xslx=FILE          XSLX file as downloaded from the EasyChair
                             'Conference data download' menu.
  -y, --year=YEAR          Year of the edition to be prepared.
```


### Synchronize authors (`sync-authors`)

Synchronizes the authors information between the local submissions and the _Sistedes Digital Library_, trying to match existing authors in the library with local authors. In case the authors do not exist in the library, it creates them. Identifiers of the authors in the _Sistedes Digital Library_ (whether they are newly created or already existing) will be saved locally for a later use during the publication of the proceedings. In case of doubt, and when running in `interactive` mode, the user will be asked whether found authors are a match or not. This command may take some time.

```
Usage: java -jar <this-file.jar> sync-authors [-acir] -e=E-MAIL -f=DIR
       -p=PASSWORD -u=URI
Synchronizes the authors information between the local submissions and the
Sistedes Digital Library, trying to match existing authors in the library with
local authors. In case the authors do not exist in the library, creates them.
Already identified authors will be skipped when running in normal mode.
  -a, --admin-only          Create new authors with administrator-only
                              permissions (i.e., hidden to the general public).
  -c, --curate              Also launch curation tasks that may be applicable
                              to the newly created Authors (i.e.,
                              refreshsistedesauthortitle)
  -e, --email=E-MAIL        E-mail of the account required to log in the
                              Sistedes Digital Library to create the authors.
  -f, --edition-file=DIR    JSON file including the conference edition metadata.
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

Publishes the conference proceedings in the _Sistedes Digital Library_, publishing one document at a time. This may take some time.

```
Usage: java -jar <this-file.jar> publish [-ac] -e=E-MAIL -f=FILE -p=PASSWORD
       -u=URI
Publishes the specified edition in the Sistedes Digital Library. Published
elements will be recorded locally to avoid recreating them.
  -a, --admin-only          Publish with administrator-only permissions (i.e.,
                              hidden to the general public).
  -c, --curate              Also launch curation tasks that may be applicable
                              to the newly created communities, collections and
                              items (i.e., registerexternalhandle, filtermedia,
                              generatecitation, generatebibcitation).
  -e, --email=E-MAIL        E-mail of the account required to log in the
                              Sistedes Digital Library to create the authors.
  -f, --edition-file=FILE   JSON file including the conference edition metadata.
  -p, --password=PASSWORD   Password of the account in the Sistedes Digital
                              Library.
  -u, --uri=URI             URI of the root endpoint of the DSpace API.
```

### Produce different listings (`list`)

Produce some listings that may be useful to detect inconsistencies and errors in the metadata files. If multiple listings are specified, they will be shown in a rown.

```
Usage: java -jar <this-file.jar> list [-enot] -f=FILE
Generates different listings of the conference data.
  -e, --authors-with-different-emails
                            List the authors that have more than one different
                              e-mail in his/her signature.
  -f, --edition-file=FILE   JSON file including the conference edition metadata.
  -n, --authors-with-different-names
                            List the authors that have more than one different
                              name in his/her signature.
  -o, --authors-with-different-orcids
                            List the authors that have more than one different
                              ORCID in his/her signature.
  -t, --paper-titles        List all the titles of the papers.
```

### EXPERIMENTAL: Split PDF file (`split`)

Splits a single PDF file containing the full proceedings of a conference and sets up the JSON files required to generate the proceedings in the new Digital Library.

```
Usage: java -jar <this-file.jar> split -a=ACRONYM [-c=PAGES] [-f=PAGES] -i=FILE
                                       [-o=DIR] -P=PREFIX [-u=URL] -y=YEAR
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
  -i, --input=FILE        Input PDF file with the full proceedings.
  -o, --output=DIR        Ouput directory where the generated conference files
                            should be placed.
  -P, --prefix=PREFIX     Handle prefix.
  -u, --grobid-url=URL    Grobid service URL.
  -y, --year=YEAR         Year of the edition to be prepared.
```

## License

This program is licensed under the _Eclipse Public License v2.0_.

## Authorship

Sistedes Digital Library (https://biblioteca.sistedes.es/about).