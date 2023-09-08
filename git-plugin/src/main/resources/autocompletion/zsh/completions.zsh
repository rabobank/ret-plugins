function _autocomplete_branch() {
    local word=${words[$CURRENT]}
    if _matches_flag_syntax "$word"; then return; fi
    _contains_any_flag '-ica' '--ignore-context-aware' && local ignore_context_aware_flag='-ica'
    local repository_flag_value=${RET_COMBINED_OPT_ARGS[-r]-${RET_COMBINED_OPT_ARGS[--repository]}}
    desc=("${(@f)$(RET_ENV=ZSH_AUTOCOMPLETE ret git autocomplete branch --word="$word" --repository="${repository_flag_value}" ${ignore_context_aware_flag})}")
    vals=( ${desc%%:*} )
    compadd -d desc -aQU vals
    compstate[insert]=menu # no expand
}

function _autocomplete_repository() {
    local word=${words[$CURRENT]}
    if _matches_flag_syntax "$word"; then return; fi
    desc=("${(@f)$(RET_ENV=ZSH_AUTOCOMPLETE ret git autocomplete repository --word="$word")}")
    vals=( ${desc%%:*} )
    compadd -d desc -aQU vals
    compstate[insert]=menu # no expand
}

function _autocomplete_repository_flag() {
    local word=${words[$CURRENT]}
    local flag_value=${RET_COMBINED_OPT_ARGS[-r]-${RET_COMBINED_OPT_ARGS[--repository]}}

    # Get the possible prefix for the values. For example "-r=" or "" when -r is followed by a space rather than "="
    local flag_word_prefix=$([[ $word =~ ^.*= ]] && echo -n "$MATCH")
    # Get the value from the option -r or else --repository
    local repository_flag_value=$flag_value
    desc=("${(@f)$(RET_ENV=ZSH_AUTOCOMPLETE ret git autocomplete repository --word="${repository_flag_value}")}")
    vals=( ${desc%%:*} )
    compadd -p "$flag_word_prefix" -d desc -aQU vals
    compstate[insert]=menu # no expand
}

function _autocomplete_pipeline() {
    local word=${(Q)words[$CURRENT]} # (Q) unescapes things like \(, \) etc
    if _matches_flag_syntax "$word"; then return; fi
    desc=("${(@f)$(RET_ENV=ZSH_AUTOCOMPLETE ret git autocomplete pipeline --word="${word}")}")
    vals=( ${desc%%:*} )
    compadd -V "no-sort-group" -d desc -aU vals # not sure why "-o nosort" does not work and why -V is...
    compstate[list_max]="300"
    compstate[insert]=menu
}

function _autocomplete_pipeline_run() {
    local word=${words[$CURRENT]}
    local pipeline_id=${(Q)words[$CURRENT-1]} # (Q) unescapes things like \(, \) etc
    if _matches_flag_syntax "$word"; then return; fi
    desc=("${(@f)$(RET_ENV=ZSH_AUTOCOMPLETE ret git autocomplete pipeline-run --pipeline-id="${pipeline_id}" --word="$word")}")
    vals=( ${desc%%:*} )
    compadd -V "no-sort-group" -d desc -aQU vals # not sure why "-o nosort" does not work and why -V is...
    compstate[insert]=menu # no expand
}

function _autocomplete_pullrequest() {
    local word=${words[$CURRENT]}
    if _matches_flag_syntax "$word"; then return; fi
    _contains_any_flag '-ica' '--ignore-context-aware' && local ignore_context_aware_flag='-ica'
    local repository_flag_value=${RET_COMBINED_OPT_ARGS[-r]-${RET_COMBINED_OPT_ARGS[--repository]}}
    candidates=("${(@f)$(RET_ENV=ZSH_AUTOCOMPLETE ret git autocomplete pullrequest --word="$word" --repository="${repository_flag_value}" ${ignore_context_aware_flag})}")
    desc=( ${candidates#*:} )
    vals=( ${candidates%%:*} )
    compadd -d desc -aQU vals
    compstate[insert]=menu # no expand
}
