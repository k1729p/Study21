@echo off
set EXEC_PULSAR_ADMIN=docker exec -it pulsar bin/pulsar-admin 
set TENANT=kp-tenant
set NAMESPACE=kp-namespace
set TOPICS=orig-1, orig-2, dest-1, dest-2, select-dest, select-orig

set /P KEY="Delete previous topics, namespace and tenant? Y [N]"
if /i "%KEY:~0,1%"=="Y" (
  for %%T in (%TOPICS%) do (
    echo delete topic %%T
    %EXEC_PULSAR_ADMIN% topics delete persistent://%TENANT%/%NAMESPACE%/%%T > nul 2>&1
  )
  echo delete namespace %NAMESPACE%
  %EXEC_PULSAR_ADMIN% namespaces delete %TENANT%/%NAMESPACE% > nul 2>&1
  echo delete tenant %TENANT%
  %EXEC_PULSAR_ADMIN% tenants delete %TENANT% > nul 2>&1
)

echo ------------------------------------------------------------------------------------------
echo create tenant %TENANT%
%EXEC_PULSAR_ADMIN% tenants create %TENANT%
echo create namespace %NAMESPACE%
%EXEC_PULSAR_ADMIN% namespaces create %TENANT%/%NAMESPACE%
pause