// infra/bicep/modules/network.bicep
// Creates VNet + subnet for the Jump Box.

param env string
param workload string
param location string

resource vnet 'Microsoft.Network/virtualNetworks@2023-09-01' = {
  name: 'vnet-${workload}-${env}'
  location: location
  properties: {
    addressSpace: { addressPrefixes: ['10.0.0.0/16'] }
    subnets: [
      {
        name: 'subnet-jumpbox'
        properties: { addressPrefix: '10.0.1.0/24' }
      }
    ]
  }
}

output jumpboxSubnetId string = vnet.properties.subnets[0].id
