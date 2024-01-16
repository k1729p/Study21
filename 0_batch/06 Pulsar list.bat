@echo off
set TENANT=kp-tenant
set NAMESPACE=kp-namespace
set TOPICS=orig-1, orig-2, dest-1, dest-2, select-dest, select-orig
set EXEC_PULSAR_ADMIN=docker exec -it pulsar bin/pulsar-admin 

echo Pulsar tenants:
%EXEC_PULSAR_ADMIN% tenants list
echo - - - - -
echo Pulsar namespaces in '%TENANT%':
%EXEC_PULSAR_ADMIN% namespaces list %TENANT%
echo - - - - -
echo Pulsar topics in '%NAMESPACE%':
%EXEC_PULSAR_ADMIN% topics list %TENANT%/%NAMESPACE%
echo - - - - -
for %%T in (%TOPICS%) do (
	echo Pulsar subscriptions for topic '%%T':
	%EXEC_PULSAR_ADMIN% topics subscriptions persistent://%TENANT%/%NAMESPACE%/%%T
)
:: %EXEC_PULSAR_ADMIN% topics stats persistent://%TENANT%/%NAMESPACE%/select-dest
pause