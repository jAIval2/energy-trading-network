#!/bin/bash

# Energy Trading Network Script
# Based on Hyperledger Fabric test-network

export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=$PWD/../config/

# Network configuration
CHANNEL_NAME="energy-trading-channel"
CC_NAME="energy-trading-chaincode"
CC_SRC_PATH="../chaincode/energy-trading-java"
CC_RUNTIME_LANGUAGE="java"
CC_VERSION="1.0"
CC_SEQUENCE="1"

# Organizations
ORG1_NAME="EnergyAggregatorMSP"
ORG2_NAME="UtilityCompanyMSP"
ORG3_NAME="RegulatoryBodyMSP"

# Print the usage message
function printHelp() {
  echo "Usage: "
  echo "  energy-network.sh <Mode> [Flags]"
  echo "    <Mode>"
  echo "      - 'up' - Bring up the energy trading network"
  echo "      - 'down' - Clear the network and remove artifacts"
  echo "      - 'restart' - Restart the network"
  echo "      - 'deployCC' - Deploy the energy trading chaincode"
  echo "      - 'createChannel' - Create the energy trading channel"
  echo
  echo "    Flags:"
  echo "    -c <channel name> - Channel name to use (default: \"energy-trading-channel\")"
  echo "    -ccn <name> - Chaincode name to use (default: \"energy-trading-chaincode\")"
  echo "    -ccl <language> - Programming language (default: \"java\")"
  echo "    -ccv <version>  - Chaincode version (default: \"1.0\")"
  echo
  echo " Examples:"
  echo "   energy-network.sh up createChannel"
  echo "   energy-network.sh deployCC -ccn energy-trading-chaincode -ccl java"
}

# Import from test-network functions
. scripts/envVar.sh
. scripts/deployCC.sh
. scripts/createChannel.sh

# Network Up
function networkUp() {
    echo "Starting Energy Trading Network..."
    
    if [ ! -d "organizations/peerOrganizations" ]; then
        echo "Generating crypto material..."
        ./scripts/generate.sh
    fi

    COMPOSE_FILES="-f docker/docker-compose-test-net.yaml"
    COMPOSE_FILES="${COMPOSE_FILES} -f docker/docker-compose-ca.yaml"

    IMAGE_TAG=$IMAGETAG docker-compose ${COMPOSE_FILES} up -d 2>&1

    echo "Waiting for network to start..."
    sleep 10
    
    docker ps -a
    if [ $? -ne 0 ]; then
        echo "ERROR: Failed to start network"
        exit 1
    fi
    
    echo "Energy Trading Network is ready!"
}

# Network Down
function networkDown() {
    echo "Stopping Energy Trading Network..."
    
    COMPOSE_FILES="-f docker/docker-compose-test-net.yaml"
    COMPOSE_FILES="${COMPOSE_FILES} -f docker/docker-compose-ca.yaml"

    IMAGE_TAG=$IMAGETAG docker-compose ${COMPOSE_FILES} down --volumes --remove-orphans

    # Clean up chaincode containers and images
    docker rm -f $(docker ps -aq --filter label=service=hyperledger-fabric) 2>/dev/null || true
    docker rmi -f $(docker images -aq --filter reference='dev-*') 2>/dev/null || true

    echo "Network stopped and cleaned up"
}

# Parse commandline args
while [[ $# -ge 1 ]] ; do
  key="$1"
  case $key in
  -h )
    printHelp
    exit 0
    ;;
  -c )
    CHANNEL_NAME="$2"
    shift
    ;;
  -ccn )
    CC_NAME="$2"
    shift
    ;;
  -ccl )
    CC_RUNTIME_LANGUAGE="$2"
    shift
    ;;
  -ccv )
    CC_VERSION="$2"
    shift
    ;;
  up )
    NETWORK_UP=true
    ;;
  down )
    NETWORK_DOWN=true
    ;;
  restart )
    NETWORK_DOWN=true
    NETWORK_UP=true
    ;;
  deployCC )
    DEPLOY_CC=true
    ;;
  createChannel )
    CREATE_CHANNEL=true
    ;;
  * )
    echo "Unknown flag: $key"
    printHelp
    exit 1
    ;;
  esac
  shift
done

# Execute based on mode
if [ "${NETWORK_DOWN}" == "true" ]; then
    networkDown
fi

if [ "${NETWORK_UP}" == "true" ]; then
    networkUp
fi

if [ "${CREATE_CHANNEL}" == "true" ]; then
    echo "Creating channel ${CHANNEL_NAME}..."
    createChannel $CHANNEL_NAME
fi

if [ "${DEPLOY_CC}" == "true" ]; then
    echo "Deploying chaincode ${CC_NAME}..."
    deployCC $CHANNEL_NAME $CC_NAME $CC_SRC_PATH $CC_RUNTIME_LANGUAGE $CC_VERSION $CC_SEQUENCE
fi

echo "Energy Trading Network operations completed!"
