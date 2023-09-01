# RET Splunk Plugin

To interact with Splunk and query on project (names), for now a manual action is needed.

Locate `$HOME/.ret/plugins/splunk.json` and add the following:

```json
{
    ...
    "projects": [
        {
            "name": "project-one"
        },
        {
            "name": "project-two"
        },
        {
            "name": "project-x"
        }
    ]
}
```
