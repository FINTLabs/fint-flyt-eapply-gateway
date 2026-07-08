# fint-flyt-eapply-gateway

Gateway for mottak av eApply metadata og instanser til Flyt.

Applikasjonen er en Spring Boot/Kotlin-tjeneste som eksponerer API-er for kildeapplikasjoner, validerer og mapper eApply-data til Flyt sine interne metadata- og instansmodeller, og publiserer videre via `flyt-gateway-starter`.

## Funksjonalitet

- Mottar eApply skjemadefinisjoner som integrasjonsmetadata.
- Mottar eApply instanser som JSON eller multipart.
- Laster opp filer referert fra multipart-instansen til Flyt file service.
- Mapper felter, grupper og repeterende grupper til Flyt `InstanceObject`.
- Tilbyr statusoppslag for en kildeapplikasjonsinstans med tilhørende `archiveCaseId`.

## Teknologi

- Kotlin
- Spring Boot
- Gradle
- Java 25
- `no.novari:flyt-gateway-starter`
- Kafka

## API

Alle eksterne endepunkter ligger under `/api`.

### Metadata

```http
POST /api/eapply/metadata
Content-Type: application/json
```

Eksempel:

```json
{
  "metadata": {
    "formId": "eapply-case",
    "formDisplayName": "Soknad",
    "version": 1
  },
  "elements": [
    {
      "id": "Case",
      "displayName": "Sak",
      "type": "Group",
      "elements": [
        {
          "id": "Title",
          "displayName": "Tittel",
          "type": "String"
        }
      ]
    }
  ]
}
```

Metadata mappes slik:

- `metadata.formId` blir `sourceApplicationIntegrationId`.
- `metadata.formDisplayName` blir integrasjonens visningsnavn.
- `metadata.version` blir integrasjonens versjon.
- Elementer av type `Group` blir kategorier.
- Elementer av type `Group` med `multiple=true` blir objektkolleksjoner.
- Elementer av type `file` blir filverdier.
- Elementer av type `bool` eller `boolean` blir boolean-verdier.
- Andre elementtyper blir string-verdier.

### Instans

```http
POST /api/eapply/instances
Content-Type: application/json
```

Eksempel:

```json
{
  "metadata": {
    "formId": "eapply-case",
    "instanceId": "instance-123"
  },
  "elements": [
    {
      "id": "Case.Title",
      "value": "Soknad om redusert foreldrebetaling"
    }
  ]
}
```

`metadata.formId` brukes som kildeapplikasjonens integrasjons-ID. `metadata.instanceId` brukes som kildeapplikasjonens instans-ID.

### Multipart-instans

```http
POST /api/eapply/instances
Content-Type: multipart/form-data
```

Multipart-kallet må inneholde en part med navn `instance`, som er samme JSON-struktur som vanlig instansmottak. Filreferanser i instansen kobles mot multipart-filer via `partName`.

Eksempel på filreferanse i `elements.value`:

```json
{
  "fileName": "soknad.pdf",
  "mediaType": "application/pdf",
  "partName": "mainDocument",
  "originalFilename": "soknad.pdf",
  "encoding": "binary"
}
```

Ved mapping blir filen persistert, og feltet får en fil-ID fra Flyt file service.

### Status

```http
GET /api/eapply/instances/{sourceApplicationInstanceId}/status
```

Respons ved funnet sak:

```json
{
  "archiveCaseId": "2026/456"
}
```

Returnerer `404 Not Found` hvis `archiveCaseId` ikke finnes for angitt `sourceApplicationInstanceId`.

## Validering

Metadata:

- `metadata.formId` og `metadata.formDisplayName` må være satt.
- `elements` må inneholde minst ett element.
- Elementer må ha `displayName` og `type`.
- Element-ID-er må være unike innenfor samme nivå.
- Elementer med `multiple=true` må være av type `Group`.
- Kun gruppeelementer kan ha underelementer.

Instanser:

- `metadata.formId` og `metadata.instanceId` må være satt.
- `elements` må inneholde minst ett element.
- Hvert element må ha `id`.

## Lokal kjøring

Applikasjonen krever Java 25.

```bash
./gradlew bootRun
```

For lokal staging-konfigurasjon:

```bash
./gradlew bootRun --args='--spring.profiles.active=local-staging'
```

`local-staging` setter blant annet:

- `server.port=8110`
- `spring.kafka.bootstrap-servers=localhost:9092`
- `novari.flyt.file-service-url=http://localhost:8091`
- `novari.kafka.topic.org-id=novari-no`

## Bygg og test

Kjør full sjekk før endringer merges:

```bash
./gradlew check
```

`check` kjører også ktlint. Bruk denne fremfor bare `test` når du skal validere endringer.

Nyttige kommandoer:

```bash
./gradlew test
./gradlew ktlintCheck
./gradlew ktlintFormat
./gradlew clean build
```

## Konfigurasjon

Standardprofiler inkluderes fra `application.yaml`:

- `flyt-kafka`
- `flyt-logging`
- `flyt-web-resource-server`
- `flyt-file-client`

Viktige konfigurasjonsområder:

- `novari.kafka.*` for Kafka topics og consumer group.
- `novari.flyt.file-service-url` for Flyt file service.
- `novari.flyt.web-resource-server.security.api.external.*` for ekstern API-sikkerhet.
- `fint.sso.client-id` og `fint.sso.client-secret` for OAuth2-klient mot file service.

## Deployment

Applikasjonen pakkes som Docker-image med `gcr.io/distroless/java25:nonroot`.

Kubernetes/Flais-konfigurasjon ligger under `kustomize/`:

- `kustomize/base` inneholder felles Application-konfigurasjon.
- `kustomize/overlays/<org>/<cluster>` inneholder org- og miljøspesifikke patcher.

GitHub Actions:

- `CI.yaml` bygger og tester pull requests og `main`.
- `CD.yaml` deployer etter fullført CI på `main`.
- `MD.yaml` kan brukes til manuell deploy.

