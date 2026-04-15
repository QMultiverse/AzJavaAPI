// infra/bicep/main.bicep
// Orchestrator — deploys all modules for one environment.
// Parameters come from the environment-specific .bicepparam file.

targetScope = 'resourceGroup'

@description('Environment name: dev, uat, or prod')
param env string

@description('Workload / project short name — used as prefix for every resource')
param workload string = 'jsapi'

@description('Azure region for all resources')
param location string = resourceGroup().location

@description('Your corporate IP address for Jump Box NSG rule')
param corporateIpAddress string

@description('SSH public key authorized to log into the Jump Box')
param sshPublicKey string

@description('Email address for monitoring alert notifications')
param notificationEmail string

// Deploy networking first (other modules depend on it)
module network 'modules/network.bicep' = {
  name: 'network'
  params: {
    env: env
    workload: workload
    location: location
  }
}

// Jump Box depends on the VNet being created
module jumpbox 'modules/jumpbox.bicep' = {
  name: 'jumpbox'
  params: {
    env: env
    workload: workload
    location: location
    subnetId: network.outputs.jumpboxSubnetId
    corporateIpAddress: corporateIpAddress
    sshPublicKey: sshPublicKey
  }
}

module acr 'modules/acr.bicep' = {
  name: 'acr'
  params: {
    env: env
    workload: workload
    location: location
  }
}

module keyvault 'modules/keyvault.bicep' = {
  name: 'keyvault'
  params: {
    env: env
    workload: workload
    location: location
    // Wire ACR password directly — never touches terminal
    acrPassword: acr.outputs.acrPassword
  }
}

module storage 'modules/storage.bicep' = {
  name: 'storage'
  params: {
    env: env
    workload: workload
    location: location
  }
}

module monitoring 'modules/monitoring.bicep' = {
  name: 'monitoring'
  params: {
    env: env
    workload: workload
    location: location
    jumpboxVmId: jumpbox.outputs.vmId
    notificationEmail: notificationEmail
  }
}

// Outputs used by pipeline variable groups
output acrLoginServer string = acr.outputs.loginServer
output reportUrl       string = storage.outputs.staticWebUrl
output keyVaultName    string = keyvault.outputs.name
