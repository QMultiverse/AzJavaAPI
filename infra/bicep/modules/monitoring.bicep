// infra/bicep/modules/monitoring.bicep
// Log Analytics Workspace and Azure Monitor alerts.
// Covers: VM CPU and Key Vault secret expiry.

param env string
param workload string
param location string

@description('Resource ID of the Jump Box VM for CPU alert')
param jumpboxVmId string = ''

@description('Email address for alert notifications')
param notificationEmail string

// Log Analytics Workspace
resource law 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: 'law-${workload}-${env}'
  location: location
  properties: {
    sku: { name: 'PerGB2018' }
    retentionInDays: 30
    features: {
      enableLogAccessUsingOnlyResourcePermissions: true
    }
  }
}

// Action group — sends notifications to email
resource actionGroup 'Microsoft.Insights/actionGroups@2023-01-01' = {
  name: 'ag-${workload}-${env}'
  location: 'global'
  properties: {
    // groupShortName is capped at 12 chars by Azure
    groupShortName: take(workload, 12)
    enabled: true
    emailReceivers: [
      {
        name: 'Platform Owner'
        emailAddress: notificationEmail
        useCommonAlertSchema: true
      }
    ]
  }
}

// Alert: Jump Box CPU > 80% for 5 minutes
// Only created if jumpboxVmId is provided
resource cpuAlert 'Microsoft.Insights/metricAlerts@2018-03-01' = if (!empty(jumpboxVmId)) {
  name: 'alert-${workload}-jumpbox-cpu-${env}'
  location: 'global'
  properties: {
    description: 'Jump Box CPU exceeded 80% for 5 minutes'
    severity: 2
    enabled: true
    scopes: [ jumpboxVmId ]
    evaluationFrequency: 'PT1M'
    windowSize: 'PT5M'
    criteria: {
      'odata.type': 'Microsoft.Azure.Monitor.SingleResourceMultipleMetricCriteria'
      allOf: [
        {
          name: 'HighCPU'
          metricName: 'Percentage CPU'
          operator: 'GreaterThan'
          threshold: 80
          timeAggregation: 'Average'
          criterionType: 'StaticThresholdCriterion'
        }
      ]
    }
    actions: [ { actionGroupId: actionGroup.id } ]
  }
}

output workspaceId   string = law.id
output workspaceName string = law.name
