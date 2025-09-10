./network.sh down

sleep 5

docker volume prune -f

docker volume rm $(docker volume ls -q | grep -E "(orderer|peer0)") 2>/dev/null || true

docker rm -f $(docker ps -aq --filter label=service=hyperledger-fabric) 2>/dev/null || true
docker rm -f $(docker ps -aq --filter name='dev-peer*') 2>/dev/null || true
docker kill $(docker ps -q --filter name=ccaas) 2>/dev/null || true

docker image rm -f $(docker images -aq --filter reference='dev-peer*') 2>/dev/null || true

docker run --rm -v "$(pwd):/data" busybox sh -c 'cd /data && rm -rf system-genesis-block/*.block organizations/peerOrganizations organizations/ordererOrganizations channel-artifacts log.txt *.tar.gz packagedChaincode/*.tar.gz'

docker run --rm -v "$(pwd):/data" busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/org1/msp organizations/fabric-ca/org1/tls-cert.pem organizations/fabric-ca/org1/ca-cert.pem organizations/fabric-ca/org1/IssuerPublicKey organizations/fabric-ca/org1/IssuerRevocationPublicKey organizations/fabric-ca/org1/fabric-ca-server.db'
docker run --rm -v "$(pwd):/data" busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/org2/msp organizations/fabric-ca/org2/tls-cert.pem organizations/fabric-ca/org2/ca-cert.pem organizations/fabric-ca/org2/IssuerPublicKey organizations/fabric-ca/org2/IssuerRevocationPublicKey organizations/fabric-ca/org2/fabric-ca-server.db'
docker run --rm -v "$(pwd):/data" busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/ordererOrg/msp organizations/fabric-ca/ordererOrg/tls-cert.pem organizations/fabric-ca/ordererOrg/ca-cert.pem organizations/fabric-ca/ordererOrg/IssuerPublicKey organizations/fabric-ca/ordererOrg/IssuerRevocationPublicKey organizations/fabric-ca/ordererOrg/fabric-ca-server.db'
