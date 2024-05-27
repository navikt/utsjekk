#!/bin/zsh

secret_name=$(kubectl -n helved get secrets | grep -v oppdrag | grep -v simulering | grep -v prosessering | grep azure-utsjekk | awk '{print ($1)}' )
secret_value=$(kubectl -n helved get secret "${secret_name}" -o json | jq '.data | map_values(@base64d)')

client_id=$(echo "${secret_value}" | jq -r '.AZURE_APP_CLIENT_ID')
printf "AZURE_APP_CLIENT_ID:\t %s\n" "${client_id}"

client_secret=$(echo "${secret_value}" | jq -r '.AZURE_APP_CLIENT_SECRET')
printf "AZURE_APP_CLIENT_SECRET: %s\n" "${client_secret}"

if [[ $(uname) == "Darwin" ]]; then
    printf "AZURE_APP_CLIENT_ID=%s\nAZURE_APP_CLIENT_SECRET=%s" "${client_id}" "${client_secret}" | pbcopy
fi
