
./network.sh up
./network.sh createChannel -c mychannel
./network.sh cc package -c mychannel -ccn energy-trading-chaincode -ccp chaincode/energy-trading-java -ccl java -ccv 1.1
./network.sh deployCC -c mychannel -ccn energy-trading-chaincode -ccp chaincode/energy-trading-java -ccl java -ccv 1.1 -ccs 1 -cci NA
./network.sh cc list -c mychannel
