#!/bin/bash

# Path to network directory (adjust if needed)
NETWORK_DIR="../"
OUTPUT_FILE="config/connection-org1.json"

echo "Generating connection profile with embedded certificates..."

# Read certificates
PEER_CA_CERT=$(cat ${NETWORK_DIR}organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt | sed 's/$/\\n/' | tr -d '\n')
ORDERER_CA_CERT=$(cat ${NETWORK_DIR}organizations/ordererOrganizations/example.com/orderers/orderer.example.com/tls/ca.crt | sed 's/$/\\n/' | tr -d '\n')

# Create connection profile
cat > ${OUTPUT_FILE} << EOF
{
    "name": "energy-trading-network",
    "version": "1.0.0",
    "client": {
        "organization": "Org1",
        "connection": {
            "timeout": {
                "peer": {
                    "endorser": "300"
                },
                "orderer": "300"
            }
        }
    },
    "organizations": {
        "Org1": {
            "mspid": "Org1MSP",
            "peers": ["peer0.org1.example.com"]
        }
    },
    "peers": {
        "peer0.org1.example.com": {
            "url": "grpcs://localhost:7051",
            "tlsCACerts": {
                "pem": "${PEER_CA_CERT}"
            },
            "grpcOptions": {
                "ssl-target-name-override": "peer0.org1.example.com",
                "hostnameOverride": "peer0.org1.example.com"
            }
        }
    },
    "orderers": {
        "orderer.example.com": {
            "url": "grpcs://localhost:7050",
            "tlsCACerts": {
                "pem": "${ORDERER_CA_CERT}"
            },
            "grpcOptions": {
                "ssl-target-name-override": "orderer.example.com",
                "hostnameOverride": "orderer.example.com"
            }
        }
    }
}
EOF

echo "Connection profile generated at ${OUTPUT_FILE}"
