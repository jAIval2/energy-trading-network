#!/usr/bin/env bash
# Helper: invoke registerProsumer and then query getProsumer via docker exec into peer0.org1
# Usage: ./scripts/invokeRegisterProsumer.sh PROSUMER_ID NAME LOCATION SOLAR_KW ORG_MSP

set -euo pipefail
if [ "$#" -lt 5 ]; then
  echo "Usage: $0 PROSUMER_ID NAME LOCATION SOLAR_KW ORG_MSP"
  exit 2
fi

PROSUMER_ID="$1"
NAME="$2"
LOCATION="$3"
SOLAR_KW="$4"
ORG_MSP="$5"

PEER_CONTAINER=peer0.org1.example.com
ORDERER_CA_HOST=organizations/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem
ADMIN_MSP_HOST=organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
PEER0_ORG2_CA_HOST=organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/msp/tlscacerts/tlsca.org2.example.com-cert.pem

# copy necessary files into container (idempotent)
docker cp "$ORDERER_CA_HOST" "$PEER_CONTAINER":/tmp/orderer-tlsca.pem >/dev/null 2>&1 || true
docker cp "$ADMIN_MSP_HOST" "$PEER_CONTAINER":/tmp/adminmsp >/dev/null 2>&1 || true
docker cp "$PEER0_ORG2_CA_HOST" "$PEER_CONTAINER":/tmp/peer0-org2-tlsca.pem >/dev/null 2>&1 || true

INVOKE_PAYLOAD=$(printf '{"function":"registerProsumer","Args":["%s","%s","%s","%s","%s"]}' "$PROSUMER_ID" "$NAME" "$LOCATION" "$SOLAR_KW" "$ORG_MSP")

echo "Invoking registerProsumer for $PROSUMER_ID ..."
docker exec "$PEER_CONTAINER" bash -lc \
  "CORE_PEER_LOCALMSPID='Org1MSP' CORE_PEER_MSPCONFIGPATH=/tmp/adminmsp CORE_PEER_ADDRESS=localhost:7051 CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/tls/ca.crt peer chaincode invoke -o orderer.example.com:7050 --waitForEvent --ordererTLSHostnameOverride orderer.example.com --tls --cafile /tmp/orderer-tlsca.pem -C mychannel -n energy-trading-chaincode --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles /etc/hyperledger/fabric/tls/ca.crt --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles /tmp/peer0-org2-tlsca.pem -c '$INVOKE_PAYLOAD'"

echo "Waiting a few seconds for commit..."
sleep 3

QUERY_PAYLOAD=$(printf '{"function":"getProsumer","Args":["%s"]}' "$PROSUMER_ID")

echo "Querying getProsumer for $PROSUMER_ID ..."
docker exec "$PEER_CONTAINER" bash -lc \
  "CORE_PEER_LOCALMSPID='Org1MSP' CORE_PEER_MSPCONFIGPATH=/tmp/adminmsp CORE_PEER_ADDRESS=localhost:7051 CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/tls/ca.crt peer chaincode query -C mychannel -n energy-trading-chaincode -c '$QUERY_PAYLOAD'"

exit 0
