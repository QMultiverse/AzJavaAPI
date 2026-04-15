# CI & Infrastructure — Refactor Notes

This document captures the plan and the changes applied while adapting the
Azure DevOps pipelines and the Bicep infra that were copied into this repo
from a sibling UI-automation project. It is scoped to `azure-pipelines/`
and `infra/` — the Serenity test code itself was not touched.

For the general project README (what the tests do, how to run them locally),
see [`readme.adoc`](./readme.adoc).

---

## 1. Starting point

The following assets were copied verbatim from another project and needed
review before they could run here:

```
azure-pipelines/
  ci-build.yml            # main CI pipeline
  pr-validation.yml       # PR gate
infra/
  bicep/
    main.bicep
    modules/
      acr.bicep
      jumpbox.bicep
      keyvault.bicep
      monitoring.bicep
      network.bicep
      storage.bicep
    parameters/
      dev.bicepparam
  grid/
    grid-deploy.sh        # Selenium Grid on ACI
    grid-teardown.sh
```

Problems observed on review:

- **UI-test assumptions baked in.** The pipelines assumed a Selenium Grid
  deployed to Azure Container Instances, split tests into Chrome and Edge
  jobs, and passed `webdriver.remote.url` / `BROWSER` / `WEBDRIVER_BASE_URL`.
  This is an API-only project — nothing needs a browser.
- **Broken Docker build stage.** `ci-build.yml` built and pushed a
  `serenity-runner` image from `Dockerfile`, but that Dockerfile references
  non-existent `bddtrader-*` modules. It would fail on first run.
- **Undefined Maven profile.** Tests were invoked with `-Pazure-ci,cucumber`,
  but neither profile exists in this repo's `pom.xml`.
- **Globally-unique resource name collisions.** `acr.bicep`, `storage.bicep`,
  and `keyvault.bicep` used hard-coded `serenity`-prefixed names. Azure
  requires these to be globally unique, so a second deployment of this
  repo — or a parallel deployment of the sibling project — would collide.
  Key Vault soft-delete makes the collision linger for 90 days even after
  the other vault is removed.
- **Dead infra bits left behind.** `subnet-aci` (delegated to ACI for the
  grid) and the `selenium-grid-url` Key Vault secret had no consumer once
  the grid was removed. `subnet-jenkins` had a misleading name — the
  project doesn't use Jenkins; it just needed a subnet for the Jump Box.
- **Copy-paste hazards.** Jumpbox SSH public key was hard-coded inside
  `jumpbox.bicep` with an identifying email comment. Monitoring had a
  placeholder `platform-owner@your-company.com` email.

---

## 2. Goals of the refactor

1. Remove all Selenium Grid / browser-specific scaffolding from the
   pipelines; keep a simple parallel-execution mechanism appropriate for
   API tests.
2. Make the pipelines runnable as-is on a Microsoft-hosted Azure DevOps
   agent, with no container orchestration.
3. Make every Bicep resource name collision-free against the sibling
   project (and against future clones of this repo into new RGs).
4. Strip selenium-specific leftovers from the infra modules but keep the
   operational building blocks (jumpbox, network, ACR, Key Vault,
   storage, monitoring) since they are still useful for API work —
   e.g. jumpbox for ad-hoc queries against private resources.
5. Parameterize anything project-identifying (`workload`,
   `notificationEmail`, `sshPublicKey`) so the next copy doesn't bake in
   this project's values.

---

## 3. Pipeline changes (`azure-pipelines/`)

### `ci-build.yml`

| Removed | Reason |
|---|---|
| `BuildImage` stage (Docker@2 build + push) | Dockerfile is broken for this repo, and no grid means no runner image to push. |
| `SetupGrid` stage (ACI deploy) | No Selenium Grid. |
| `TeardownGrid` stage (ACI delete) | No Selenium Grid. |
| `ChromeTests` and `EdgeTests` jobs | API tests don't care about browsers. |
| `-Pazure-ci,cucumber` profile flags | Profiles don't exist in `pom.xml`. |
| `-Dwebdriver.remote.url`, `-DBROWSER`, `WEBDRIVER_BASE_URL` | UI-only. |

| Added / kept | Detail |
|---|---|
| `Build` stage | JDK 17, Maven cache, `mvn process-test-resources` (Gherkin syntax check) + `mvn compile test-compile`. |
| `FullRegression` stage with a single `ApiTests` job | Runs `mvn -B verify -Dfailsafe.forkCount=2C -Dfailsafe.reuseForks=true`. Parallel execution at three layers — see §5. |
| Single artifact `serenity-report-api` | Replaces the per-browser `serenity-report-chrome` / `serenity-report-edge` pair. |
| `PublishReports` stage | Downloads the one artifact and uploads to `$web/reports/{branch}/build-{id}/api` and `.../latest/api` in the Storage Account. Emits the URL as a warning log for quick discovery. |

### `pr-validation.yml`

| Removed | Reason |
|---|---|
| Inline `selenium-hub` + `chrome` Docker services | No browsers. |
| Grid health-check loop + stray `Running Vinayak.exe` echo | Dead code from copy-source. |
| `webdriver.remote.url` and `WEBDRIVER_BASE_URL` | UI-only. |

| Added / kept | Detail |
|---|---|
| `Validate` stage | Gherkin validation + compile, same as CI. |
| `SmokeTest` stage, single `SmokeApi` job | Runs `mvn -B verify -Dfailsafe.forkCount=2C -Dfailsafe.reuseForks=true -Dcucumber.filter.tags='@smoke'`. |
| Results + artifact publishing | JUnit results to Azure Test Plans, Serenity report archived as `serenity-smoke-pr-$(Build.BuildNumber)`. |

### Files removed under `infra/grid/`

- `grid-deploy.sh`
- `grid-teardown.sh`
- the `infra/grid/` directory itself

Neither pipeline references these anymore.

---

## 4. Infra changes (`infra/bicep/`)

### Naming strategy

Every module now takes a `workload` parameter (default `jsapi` — short for
*JavaSerenityAPI*). Resource-group-scoped resources are named
`<prefix>-${workload}-${env}`. Globally-unique resources append
`take(uniqueString(resourceGroup().id), N)` so a deployment into any new
RG produces a different name automatically.

| Resource | New name pattern | Example (`workload=jsapi`, `env=dev`) |
|---|---|---|
| Resource Group-scoped VNet | `vnet-${workload}-${env}` | `vnet-jsapi-dev` |
| Jump Box VM | `vm-${workload}-${env}` | `vm-jsapi-dev` |
| Public IP | `pip-${workload}-${env}` | `pip-jsapi-dev` |
| NSG | `nsg-${workload}-${env}` | `nsg-jsapi-dev` |
| NIC | `nic-${workload}-${env}` | `nic-jsapi-dev` |
| SSH key | `sshkey-${workload}-${env}` | `sshkey-jsapi-dev` |
| Log Analytics | `law-${workload}-${env}` | `law-jsapi-dev` |
| Action Group | `ag-${workload}-${env}` | `ag-jsapi-dev` |
| CPU Alert | `alert-${workload}-jumpbox-cpu-${env}` | `alert-jsapi-jumpbox-cpu-dev` |
| **ACR** (global) | `acr${workload}${env}${take(uniqueString(rg.id),6)}` | `acrjsapidevabcdef` |
| **Storage** (global) | `st${workload}${env}${take(uniqueString(rg.id),8)}` | `stjsapidevabcdef12` |
| **Key Vault** (global) | `kv-${workload}-${env}-${take(uniqueString(rg.id),6)}` | `kv-jsapi-dev-abcdef` |

### Per-module edits

**`main.bicep`** — added `workload`, `sshPublicKey`, `notificationEmail`
params. Threads `workload` into every module call. Jump Box now reads
`network.outputs.jumpboxSubnetId` (renamed).

**`network.bicep`** — dropped `subnet-aci` and its ACI delegation (no
grid). Renamed `subnet-jenkins` to `subnet-jumpbox` so the name matches
reality. Output renamed `jumpboxSubnetId`.

**`jumpbox.bicep`** — added `sshPublicKey` parameter; the hard-coded key
with an identifying email comment is gone from the module and lives in
`dev.bicepparam` instead. All resource names use the `${workload}` prefix.
Auto-shutdown name preserved as `shutdown-computevm-<vmName>` (Azure
requires that literal form).

**`acr.bicep`** — globally-unique name. No functional change.

**`keyvault.bicep`** — globally-unique name. **Removed the
`selenium-grid-url` secret.** Kept `acr-password` wiring.

**`storage.bicep`** — globally-unique name. Added a `name` output for
convenience. Static-website hosting and 90-day blob lifecycle on the
`reports/` prefix kept.

**`monitoring.bicep`** — added `notificationEmail` param (replaces the
`platform-owner@your-company.com` placeholder). `groupShortName` uses
`take(workload, 12)` to respect Azure's 12-char cap. Jump Box CPU alert
retained since the jumpbox stays.

**`parameters/dev.bicepparam`** — added `workload = 'jsapi'`,
`notificationEmail = 'techukjobs@gmail.com'`, `sshPublicKey = '<key>'`.
Kept `env`, `location`, `corporateIpAddress`.

### Modules retained (previously proposed for deletion)

`network.bicep`, `jumpbox.bicep`, `acr.bicep`, `keyvault.bicep` are all
kept. Rationale:

- **Jumpbox + network** — needed for ad-hoc SSH into Azure to query
  private resources during test triage.
- **ACR** — will be used when we containerize test runs in the future.
- **Key Vault** — the natural place for API credentials, bearer tokens,
  and test-data fixtures as the suite grows.

---

## 5. Parallel test execution mechanism

No Selenium Grid — parallelism is achieved entirely at the JVM/Maven
layer, in three stacked layers that are already configured in the repo:

1. **Cucumber JVM parallel execution** —
   `src/test/resources/junit-platform.properties`:
   ```
   cucumber.execution.parallel.enabled=true
   cucumber.execution.parallel.config.strategy=dynamic
   cucumber.plugin=io.cucumber.core.plugin.SerenityReporterParallel
   ```
   Scenarios within a feature run concurrently on multiple threads.

2. **Maven Failsafe parallel methods** — `pom.xml`:
   ```xml
   <parallel>methods</parallel>
   <useUnlimitedThreads>true</useUnlimitedThreads>
   ```
   JUnit test methods run in parallel inside a single JVM fork.

3. **Failsafe fork-level parallelism** — set from the pipeline:
   ```
   -Dfailsafe.forkCount=2C -Dfailsafe.reuseForks=true
   ```
   Two JVM forks per CPU core on the hosted agent, reused across test
   classes to keep startup cost low.

If the suite grows large enough that agent-level fan-out becomes
worthwhile, the next step is to add a matrix strategy to `ApiTests` that
splits by Cucumber tag group (e.g. `@users`, `@products`, `@auth`) so
Azure DevOps runs each subset on its own agent.

---

## 6. Before you deploy

1. **Create the Azure DevOps variable groups** referenced by the
   pipelines:
   - `exp-config` — needs `STORAGE_ACCOUNT` (the Storage account name
     output by `storage.bicep` after deployment).
   - `exp-secrets` — any secrets your tests need at runtime.
   The old `RESOURCE_GROUP`, `LOCATION`, `CHROME_SESSIONS` variables can
   be removed; they were grid-only.
2. **Create the service connection** `azure-exp-connection` with rights
   to the target subscription / resource group.
3. **Generate your own SSH key pair** and paste the public key into
   `infra/bicep/parameters/dev.bicepparam`. The key currently in that
   file was carried over from the copy-source and its private half
   likely isn't available to you:
   ```
   ssh-keygen -t rsa -b 4096 -C "you@example.com" -f ~/.ssh/azure_jsapi
   ```
4. **Update `corporateIpAddress`** in `dev.bicepparam` to your current
   egress IP so the Jump Box NSG actually lets you in.
5. **Deploy the infra** into a fresh resource group:
   ```
   az group create -n rg-jsapi-dev -l westeurope
   az deployment group create \
     -g rg-jsapi-dev \
     -f infra/bicep/main.bicep \
     -p infra/bicep/parameters/dev.bicepparam
   ```
6. **Copy the storage account name** from the deployment output into the
   `STORAGE_ACCOUNT` variable in the `exp-config` variable group.

---

## 7. Known loose ends (not changed)

- **`Dockerfile` and `Dockerfile.tests`** at the repo root still
  reference non-existent `bddtrader-*` modules. They are no longer
  referenced by any pipeline, so they are harmless but dead. Delete
  them if you don't plan to containerize the test runner.
- **`circle.yml`, `docker-compose.yml`, `manifest.yml`** also appear to
  be copy-source leftovers. Out of scope for this refactor — flag and
  review separately.
- **`pom.xml` webdriver dependencies** (`serenity-screenplay-webdriver`,
  etc.) are present but unused in an API-only project. Leaving them
  alone for now to avoid destabilizing the test code.
