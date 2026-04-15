// infra/bicep/modules/acr.bicep

param env string
param workload string
param location string

@allowed(['Basic', 'Classic', 'Standard', 'Premium'])
param sku string = 'Basic'

// ACR names are globally unique. uniqueString(rg id) keeps sibling projects apart.
var acrName = 'acr${workload}${env}${take(uniqueString(resourceGroup().id), 6)}'

resource acr 'Microsoft.ContainerRegistry/registries@2025-11-01' = {
  name: acrName
  location: location
  sku: {
    name: sku
  }
  properties: {
    adminUserEnabled: true
  }
}

output loginServer string = acr.properties.loginServer
output name        string = acr.name
output acrPassword string = acr.listCredentials().passwords[0].value
