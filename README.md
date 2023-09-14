# RET Plugins

This repository contains plugins for the [RET-Engineering-Tools](https://github.com/rabobank/ret-engineering-tools). Currently, we have the following plugins:

* [Git Plugin](#git-plugin): Interact with your Git provider
* [Splunk Plugin](#splunk-plugin): Directly execute queries in Splunk

## Getting Started

First, make sure you have [installed ret](https://github.com/rabobank/ret-engineering-tools#getting-started).
Then, to install a plugin, do the following:

* Navigate to the latest release
* Download the zip file for the plugin and operating system you want
* Unzip and copy/move the file to your `~/.ret/plugins` folder (create the folder if it doesn't exist yet)
  * If installing on Mac, it is possible that you need to authorize the usage of the dylib file. To do this: right-click on the file and choose open. You will now get a pop-up to confirm opening.
Do so, and you will be able to initialize the plugin:
* Run:

```shell
ret plugin initialize /path/to/.ret/plugins/<plugin file> # make sure you enter the absolute path
```

* If the plugin requires you to configure certain properties, you will now be prompted for it.
  * If you want to re-configure the plugin later, you can simply run the initialize command again
* To make sure the autocompletion is picked up correctly, restart your terminal (e.g. run `zsh` as a command)

## Git Plugin

The Git plugin allows you to interact with your Git provider, by navigating to your repositories,
or creating PRs directly from the terminal. The following Git providers are currently supported:

* Azure Devops

### Configuration

The Git plugin requires you to configure the following properties:

| Property     | Description                                                              |
|--------------|--------------------------------------------------------------------------|
| email        | Your email address. Is used for checking which pr's you already reviewed |
| pat          | A personal access token                                                  |
| projectId    | Name or id of the project your repositories are under                    |
| organization | Your Azure Devops organization                                           |

More info on personal access tokens [can be found here](https://learn.microsoft.com/en-us/azure/devops/organizations/accounts/use-personal-access-tokens-to-authenticate).
Make sure you give it at least "Read" scopes for Code and for Build.

### Pipelines
Usage: `ret git pipeline open [<pipeline_id> [<pipeline_run_id>]]`
Open the pipeline dashboard, or a specific pipeline or run

Subcommands:
- `open` Opens the pipeline dashboard

Arguments:
- `pipeline_id` Pipeline id or `<folder>\<pipeline-name>`.
    - Autocompletion on pipeline name or pipeline folder name.
- `pipeline_run_id` Pipeline run to open in Azure DevOps.
    - Autocompletion on the id, name, state (IN_PROGRESS, ...) or result (COMPLETED, ...)

Examples:
```
$ ret git pipeline open
(opens the pipeline dashboard of your project in your Git provider)

$ ret git pipeline open 123456
(opens the pipeline dashboard of a specific pipeline)

$ ret git pipeline open 123456 234567
(opens a specific pipeline run of a pipeline)
```

### Pull requests
`ret git pr [COMMAND]`

Open or create a pull request

Subcommands:
- `open`
- `create`

Flags:
- `-ica, --ignore-context-aware` Ignores context awareness.
- `-r, --repository` Filters the branches on repository.
    - Autocompletion on the repository name.
    - Context-awareness based on the name of the Git repository (last part of the Git remote URL).

#### Create
Usage: `ret git pr create [-ica] [-r=<filterRepository>] [<branch>]`

Arguments:
- `branch`
    - Autocompletion on the branch name.
    - Context-awareness based on the name of the Git branch.

Flags:
- `--no-prompt` Does not open the browser, but creates pull request directly.

Examples:
```
$ ret git pr create
(Opens the "create PR" page in Azure DevOps for the current Git repository and branch)

$ ret git pr create -r=admin-service feature/new-shiny-feature
(Opens the "create PR" page in Azure DevOps for the repository `admin-service` and for branch `feature/new-shiny-feature)`

$ ret git pr create --no-prompt
(Creates the PR directly and outputs the URL to the PR in the stdout. Tip: Pipe it into pbcopy to directly put it into your clipboard.`
```

#### Open
Usage: `ret git pr open [-ica] [-r=<filterRepository>] <pullRequestId>`

Arguments:
- `pullRequestId`
    - Autocompletion on the PR title and the Git repository name.

Examples:
```
$ ret git pr open 123456
(Opens the pull request with id 123456 in Azure DevOps)
```

### Repository
`ret git repository [COMMAND]`

This allows you to open a Git repository directly in your browser.

Subcommands:
- `open`

#### Open
Usage: `ret git repository open [-ica] [<repository>]`

Flags:
- `-ica, --ignore-context-aware` Ignores context awareness.

Arguments:
- `repository` The repository to open.
    - Autocompletion on the repository name.
    - Context-awareness based on the name of the Git repository (last part of the Git remote URL).

## Splunk Plugin

### Configuration

Upon installation, RET prompts you for the following config properties:

| Property        | Description                                                     |
|-----------------|-----------------------------------------------------------------|
| splunk_base_url | The base url where your Splunk is deployed                      |
| splunk_app      | The app name of your Splunk instance that should be searched on |

The final url that will be used by the Splunk plugin has this format:
```
"${splunkBaseUrl}/en-GB/app/${splunkApp}/search"
```

### Search on Splunk
You can search on Splunk and already type your query in the commandline, before opening the Splunk web page. Optionally,
RET allows you to specify some properties (Cloud Foundry application name and index).

Usage: `ret splunk [-ica] [-p=projectName] [-i=index] [<queryParts>...]`

Flags:
- `-p, --project=projectName` Provide the project (git repo) name to query on.
    - Context-awareness: will fill in the current Git repository name.
- `-i, --index=index` Provide the index to query on.
- `-ica, --ignore-context-aware` Ignore context awareness

Arguments:
- `queryParts` Optional free text field to add to the Splunk query

Examples:
```
$ ret splunk
(Opens Splunk with query "cf_app_name=<Your Git repo name>", or navigate to the Splunk search page without context-awareness)

$ ret splunk -i=my-index -p=admin-service loglevel != INFO
(Opens Splunk with query "index=my-index cf_app_name=admin-service loglevel != INFO")
```

