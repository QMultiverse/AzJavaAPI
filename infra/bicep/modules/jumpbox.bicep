// infra/bicep/modules/jumpbox.bicep
// Creates the Jump Box VM — the only resource with a public IP.
// SSH is locked to your corporate IP via NSG.

param env string
param workload string
param location string
param subnetId string
param corporateIpAddress string

@description('Admin username for the VM')
param adminUsername string = 'azureuser'

@description('SSH public key authorized to log in as adminUsername')
param sshPublicKey string

@allowed(['Basic', 'Standard', 'Premium'])
param sku string = 'Standard'

resource sshKey 'Microsoft.Compute/sshPublicKeys@2025-04-01' = {
  name: 'sshkey-${workload}-${env}'
  location: location
  properties: {
    publicKey: sshPublicKey
  }
}

// Public IP — static so it never changes between restarts
resource publicIp 'Microsoft.Network/publicIPAddresses@2025-05-01' = {
  name: 'pip-${workload}-${env}'
  location: location
  sku: { name: sku }
  properties: { publicIPAllocationMethod: 'Static' }
}

// NSG — port 22 only from your IP
resource nsg 'Microsoft.Network/networkSecurityGroups@2025-05-01' = {
  name: 'nsg-${workload}-${env}'
  location: location
  properties: {
    securityRules: [
      {
        name: 'AllowSSHFromCorporateIP'
        properties: {
          priority: 100
          protocol: 'Tcp'
          access: 'Allow'
          direction: 'Inbound'
          sourceAddressPrefix: corporateIpAddress
          sourcePortRange: '*'
          destinationAddressPrefix: '*'
          destinationPortRange: '22'
        }
      }
      {
        name: 'DenySSHAll'
        properties: {
          priority: 200
          protocol: 'Tcp'
          access: 'Deny'
          direction: 'Inbound'
          sourceAddressPrefix: '*'
          sourcePortRange: '*'
          destinationAddressPrefix: '*'
          destinationPortRange: '22'
        }
      }
    ]
  }
}

// Network interface
resource nic 'Microsoft.Network/networkInterfaces@2025-05-01' = {
  name: 'nic-${workload}-${env}'
  location: location
  properties: {
    networkSecurityGroup: { id: nsg.id }
    ipConfigurations: [
      {
        name: 'ipconfig1'
        properties: {
          subnet: { id: subnetId }
          publicIPAddress: { id: publicIp.id }
          privateIPAllocationMethod: 'Dynamic'
        }
      }
    ]
  }
}

// The Jump Box VM
resource jumpbox 'Microsoft.Compute/virtualMachines@2025-04-01' = {
  name: 'vm-${workload}-${env}'
  location: location
  properties: {
    hardwareProfile: { vmSize: 'Standard_D2s_v3' }
    osProfile: {
      computerName: 'vm-${workload}-${env}'
      adminUsername: adminUsername
      linuxConfiguration: {
        disablePasswordAuthentication: true
        ssh: {
          publicKeys: [
            {
              path: '/home/${adminUsername}/.ssh/authorized_keys'
              keyData: sshKey.properties.publicKey
            }
          ]
        }
      }
    }
    storageProfile: {
      imageReference: {
        publisher: 'canonical'
        offer: 'ubuntu-24_04-lts'
        sku: 'server-gen1'
        version: 'latest'
      }
      osDisk: {
        createOption: 'FromImage'
        managedDisk: { storageAccountType: 'Standard_LRS' }
        diskSizeGB: 32
      }
    }
    networkProfile: {
      networkInterfaces: [ { id: nic.id } ]
    }
  }
}

// Auto-shutdown at 22:00 to save cost overnight.
// Name must be literally 'shutdown-computevm-<vmName>' for Azure to bind it.
resource autoShutdown 'Microsoft.DevTestLab/schedules@2018-09-15' = {
  name: 'shutdown-computevm-vm-${workload}-${env}'
  location: location
  properties: {
    status: 'Enabled'
    taskType: 'ComputeVmShutdownTask'
    dailyRecurrence: { time: '2200' }
    timeZoneId: 'UTC'
    targetResourceId: jumpbox.id
  }
}

output publicIpAddress string = publicIp.properties.ipAddress
output vmName          string = jumpbox.name
output vmId            string = jumpbox.id
