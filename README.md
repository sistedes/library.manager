# Sistedes Digital Library Manager

The _Sistedes Digital Library Manager_ is a utility program that (semi-)automates the process of producing the proceedings of the Sistedes conferences. To do so, it can import an Excel (XSLX) dump of the conference data from EasyChair.

The process must be manually monitored and inspected. Manual modifications in the intermediate files are required as well.

The process to produce a new set of proceedings is typically as follows:

0. **Import the conference data** from EasyChair using the `init` command. A set of JSON metadata files will be created with the conference data.

    **IMPORTANT:** The EasyChair tags that determine the type os submission (full paper, short paper, relevant paper, tool paper, tutorial, etc.) vary between Sistedes conferences (JISBD, JCIS, and PROLE) and year. Thus, the code that automatically detects the type of submission from the dump file must be checked and possibly changed every year. Look for the `FIXME` comments in the source code.

1. **Manually adjust** the generated JSON files to complete the information required by the proceedings. Some actions that must be taken are:

    * Adjust the edition information in the edition file.

    * Add the needed _Preliminaries_ for the conference proceedings (typically _Preface_, _Commitees_, and _Invited talk_). Pay special attention to the `id` and `filename` properties to avoid overwriting files on each execution.

    * Double check the titles of the submissions (remove clarifications between parentheses about the type of paper --e.g., abstract, summary, etc.-- and make sure that words in titles are separated by a single space).

    * NOTE: the `submissions` property inside the authors' signature in submission files is not used to produce the proceedings, and it is included only for informative purposes while manually editing the files.

2. **Synchronize the authors' list** with the Sistedes Digital Library using the `sync-authors` command. This process will try to identify which authors already exist in the database. If so, the match will be stored locally using the `sistedesUuid` property. Non-existing authors will be created, and the `sistedesUuid` of the newly created author will be stored too.

3. **Publish the proceedings**. Once the authors have been matched/created, the proceedings can be produced using the `publish` command. The documents are published one at a time, and the process can take a fez minutes. All document will be public immediately.

4. **Register the Handles** of the newly published documents. This can be done in two ways:

    * Using the `register-handles` command. This will automatically create or update the Handles in the specified Handle server one at a time. This process can take several minutes.

    * Using the `dump-handles-batch` command. This will produce a batch text file with all the commands to (optionally delete) and create the Handles. This batch file can be later used in `hdl-admintool` to update all the Handles at the same time. This procedure is faster at the cost of needing an external tool.

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

3. Run `execute.bat` using any of the following subcommands: `init`, `sync-authors`, `publish`, `register-handles`, `dump-handles-batch`, `validate`.

## Command line interface

All the above commands can be executed from a single tool: the `library.manager-<VERSION>.jar`.

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
                        be skipped when running in normal mode. In forced mode,
                        information about  already identified authors will be
                        discarded and a new match will be attempted.
  validate            Validates that the conference data is ready for
                        submission without performing any modification.
  publish             Publishes the specified edition in the Sistedes Digital
                        Library. Published elements will be recorded locally to
                        avoid recreating them.
  register-handles    Registers the Sistedes Handles such that they point to
                        the Digital Library internal Handles.
  dump-handles-batch  Dumps a set of Handle commands that can be run as a batch
                        file to (optionally delete) and create all the Handles
                        in the specified edition.
  list                Generates different listings of the conference data.
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
Usage: java -jar <this-file.jar> sync-authors [-Fir] -e=E-MAIL -f=DIR
       -p=PASSWORD -u=URI
Synchronizes the authors information between the local submissions and the
Sistedes Digital Library, trying to match existing authors in the library with
local authors. In case the authors do not exist in the library, creates them.
Already identified authors will be skipped when running in normal mode. In
forced mode, information about  already identified authors will be discarded
and a new match will be attempted.
  -F, --force               Force execution, discarding existing information
                              about identified authors already existing in the
                              Sistedes Digital Library.
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

Publishes the conference proceedings in the Sistedes Digital Library, publishing one document at a time. This may take some time.

```
Usage: java -jar <this-file.jar> publish [-F] -e=E-MAIL -f=FILE -p=PASSWORD
       -u=URI
Publishes the specified edition in the Sistedes Digital Library. Published
elements will be recorded locally to avoid recreating them.
  -F, --force               Force execution, even if elements have been already
                              created or if validation errors exist.
  -e, --email=E-MAIL        E-mail of the account required to log in the
                              Sistedes Digital Library to create the authors.
  -f, --edition-file=FILE   JSON file including the conference edition metadata.
  -p, --password=PASSWORD   Password of the account in the Sistedes Digital
                              Library.
  -u, --uri=URI             URI of the root endpoint of the DSpace API.
```

### Register Handles (`register-handles`)

Registers the Handles of the newly published documents in the Sistedes Handle server one at a time. This may take some time.

```
Usage: java -jar <this-file.jar> register-handles -f=FILE -k=FILE [-p=PASSWORD]
       -P=PREFIX
Registers the Sistedes Handles such that they point to the Digital Library
internal Handles.
  -f, --edition-file=FILE   JSON file including the conference edition metadata.
  -k, --key-file=FILE       File with the secure key to authenticate in the
                              Handle system.
  -p, --password=PASSWORD   Password to unlock the key file.
  -P, --prefix=PREFIX       Handle prefix.
  ```

### Dump Handle commands in a batch file (`dump-handles-batch`)

Dump a batch text file with all the commands to (optionally delete) and create the Handles. This batch file can be later used in `hdl-admintool` to update all the Handles at the same time.

```
Usage: java -jar <this-file.jar> dump-handles-batch [-d] -f=FILE [-o=FILE]
       -P=PREFIX
Dumps a set of Handle commands that can be run as a batch file to (optionally
delete) and create all the Handles in the specified edition.
  -d, --delete              Also issue an initial DELETE command in order to
                              CREATE the Handles from scratch.
  -f, --edition-file=FILE   JSON file including the conference edition metadata.
  -o, --output=FILE         If specified, the Handle commands will be saved in
                              FILE rather than shown in stdout.
  -P, --prefix=PREFIX       Handle prefix.
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

## License

  This program is licensed under the _Eclipse Public License v2.0_.

## Authorship

Sistedes Digital Library (https://biblioteca.sistedes.es/about).