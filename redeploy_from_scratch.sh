 #!/usr/bin/env bash
set -euo pipefail

# Move to script dir root (energy-trading-network/)
cd "$(dirname "$0")"

# Config (adjust as needed)
CHANNEL_NAME="${CHANNEL_NAME:-mychannel}"
CC_NAME="${CC_NAME:-energy-trading-chaincode}"
CC_SRC_PATH="${CC_SRC_PATH:-chaincode/energy-trading-java}"
CC_LANG="${CC_LANG:-java}"
NEW_CC_VERSION="${NEW_CC_VERSION:-1.1}"
NEW_CC_SEQUENCE="${NEW_CC_SEQUENCE:-2}"

# Retry/delay
export CLI_DELAY="${CLI_DELAY:-3}"
export MAX_RETRY="${MAX_RETRY:-5}"
export VERBOSE="${VERBOSE:-false}"

echo "==> Cleaning any previous network"
./network.sh down || true

echo "==> Starting network"
./network.sh up

echo "==> Creating channel: ${CHANNEL_NAME}"
./network.sh createChannel -c "${CHANNEL_NAME}"

echo "==> Packaging chaincode ${CC_NAME} ${NEW_CC_VERSION}"
./network.sh cc package \
  -c "${CHANNEL_NAME}" \
  -ccn "${CC_NAME}" \
  -ccp "${CC_SRC_PATH}" \
  -ccl "${CC_LANG}" \
  -ccv "${NEW_CC_VERSION}"

echo "==> Deploying chaincode ${CC_NAME} v${NEW_CC_VERSION} seq ${NEW_CC_SEQUENCE}"
./network.sh deployCC \
  -c "${CHANNEL_NAME}" \
  -ccn "${CC_NAME}" \
  -ccp "${CC_SRC_PATH}" \
  -ccl "${CC_LANG}" \
  -ccv "${NEW_CC_VERSION}" \
  -ccs "${NEW_CC_SEQUENCE}" \
  -cci NA

echo "==> Verifying installed/committed chaincode"
./network.sh cc list -c "${CHANNEL_NAME}"

# Optional sanity invocations (uncomment to run after deployment)

# echo "==> Registering a prosumer"
# ./network.sh cc invoke \
#   -org 1 \
#   -c "${CHANNEL_NAME}" \
#   -ccn "${CC_NAME}" \
#   -ccic '{"function":"registerProsumer","Args":["PROSUMER001","Green Solar Farm","Mumbai, Maharashtra","100.0","ProsumerMSP"]}'

# echo "==> Processing generation event"
# TS=$(date -u +"%Y-%m-%dT%H:%M:%S.000+0000")
# ./network.sh cc invoke \
#   -org 1 \
#   -c "${CHANNEL_NAME}" \
#   -ccn "${CC_NAME}" \
#   -ccic "{\"function\":\"processElectricityGeneration\",\"Args\":[\"PROSUMER001\",\"50.0\",\"METER001\",\"$TS\",\"UTILITY001\"]}"

# echo "==> Query available tokens"
# ./network.sh cc query \
#   -org 1 \
#   -c "${CHANNEL_NAME}" \
#   -ccn "${CC_NAME}" \
#   -ccqc '{"function":"getAvailableTokens","Args":[]}'

echo "==> Done."