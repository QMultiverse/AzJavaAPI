// infra/bicep/modules/keyvault.bicep

param env string
param workload string
param location string

@secure()
param acrPassword string

// utcNow() is only valid as a parameter default — this is the correct pattern
param currentTime string = utcNow()

// KV names are globally unique and linger after soft-delete — use uniqueString suffix.
var kvName = 'kv-${workload}-${env}-${take(uniqueString(resourceGroup().id), 6)}'

resource kv 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: kvName
  location: location
  properties: {
    sku: { family: 'A', name: 'standard' }
    tenantId: subscription().tenantId
    enableSoftDelete: true
    softDeleteRetentionInDays: 90
    enableRbacAuthorization: true
  }
}

// Store ACR password — wired directly from acr.bicep output
resource acrPasswordSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: kv
  name: 'acr-password'
  properties: {
    value: acrPassword
    attributes: {
      // Alert fires 30 days before expiry (configured in monitoring.bicep)
      exp: dateTimeToEpoch(dateTimeAdd(currentTime, 'P90D'))
    }
  }
}

output name string = kv.name
output id   string = kv.id
