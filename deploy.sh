export AWS_PROFILE=nif-dev
export SLS_DEBUG=*
mvn clean install -Denv=dev
sls deploy
