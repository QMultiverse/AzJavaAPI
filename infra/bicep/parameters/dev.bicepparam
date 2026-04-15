// infra/bicep/parameters/dev.bicepparam
// Development environment parameter values.

using '../main.bicep'

param env                = 'dev'
param workload           = 'jsapi'
param location           = 'westeurope'
param corporateIpAddress = '103.214.46.138'   // Update to your IP
param notificationEmail  = 'techukjobs@gmail.com'

// SSH public key authorized on the Jump Box.
// Generate with: ssh-keygen -t rsa -b 4096 -C "your-email" -f ~/.ssh/azure_key
param sshPublicKey = 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCZYLDhlJotZDk1B21SORINApGsyakE+65bA2xqT+YttMj0zJpMe3Rl0gD079JddVZHwWjxVka6NMovlg25+WrjEZU+UWdv1saq6shzFeRN0jhHX+3bu+44X59NkIGiMWwxUfalFemLzgWqXoF4crWP+xQPPPJeSvoQ7MpW+9yg1DC/iHc4atBgkNa+4pfqonkDjzEhHmxMWqC+d0Y5GIslvPMiFqIBxdIJ0cJmOg/xoC7BvNck7jaeumj0+3eG768ezMiu5o1EFFMpRAMOtUYBZL3iNsFD28ECVhYYyP2HECF4BNB9VwwjI33BiG31iFAJaPgQOrUXy8gLTM80foJCV2mGhXZWznH/IgyHlqfbu6skMKywpX7y52NCyJ2sgnZByriOCaBZBR35hhzAuXJM2G2rvq39a49u4T4pvryOSirMFmEZhhUTta+wE9cX76uSHcgiCrsOgWZhkMAlJn7WFh0XkUqRP/598wuwGfLB9w4NP43yl2aLLKpRJXOKF69N4p4Uurv/oA4GTghhyE+3rkAOyhVk+5Uk/ZKvgIJKEOlwIPgzZQIwAPI6fEFiXJn5QiWM4xW0iMbj9HMM9Y6qa1Zqh4MPX4BrrKQfQZ7JHHa35HQd3LcX1VFB7fj341Ge4y4Id64wqqRs7k+1IVyB2Yyv1R5QCl9ftTIIs/Egbw== TechMeetsFinance@outlook.com'
