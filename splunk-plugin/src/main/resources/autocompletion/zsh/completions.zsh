function _autocomplete_splunk_app() {
    # Get the possible prefix for the values. For example "-a=" or "" when -a is followed by a space rather than "="
    local flag_word_prefix=$([[ ${words[$CURRENT]} =~ ^.*= ]] && echo -n "$MATCH")
    # Get the value from the option -a or else --app
    local app_flag_value=${RET_COMBINED_OPT_ARGS[-a]-${RET_COMBINED_OPT_ARGS[--app]}}
    local index_flag_value=${RET_COMBINED_OPT_ARGS[-i]-${RET_COMBINED_OPT_ARGS[--index]}}
    desc=("${(@f)$(RET_ENV=ZSH_AUTOCOMPLETE ret splunk autocomplete splunk-app-name --index="${index_flag_value}" --word="${app_flag_value}")}")
    vals=( ${desc%%:*} )
    compadd -p "$flag_word_prefix" -d desc -aQU vals
    compstate[insert]=menu # no expand
}

function _autocomplete_splunk_index() {
    # Get the possible prefix for the values. For example "-i=" or "" when -i is followed by a space rather than "="
    local flag_word_prefix=$([[ ${words[$CURRENT]} =~ ^.*= ]] && echo -n "$MATCH")
    # Get the value from the option -i or else --index
    local index_flag_value=${RET_COMBINED_OPT_ARGS[-i]-${RET_COMBINED_OPT_ARGS[--index]}}
    local app_flag_value=${RET_COMBINED_OPT_ARGS[-a]-${RET_COMBINED_OPT_ARGS[--app]}}
    desc=("${(@f)$(RET_ENV=ZSH_AUTOCOMPLETE ret splunk autocomplete splunk-index --app="${app_flag_value}" --word="${index_flag_value}")}")
    vals=( ${desc%%:*} )
    compadd -p "$flag_word_prefix" -d desc -aQU vals
    compstate[insert]=menu # no expand
}
