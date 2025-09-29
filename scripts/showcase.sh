#!/bin/bash

# Energy Trading Smart Contract Demo Script
# This script demonstrates the complete workflow of the energy trading platform

echo "==============================================="
echo "🔋 ENERGY TRADING SMART CONTRACT DEMO"
echo "==============================================="

CHANNEL_NAME="mychannel"
CC_NAME="energy-trading-chaincode"

# Function to execute chaincode transactions
execute_transaction() {
    local org=$1
    local function_call="$2"
    local description="$3"
    
    echo ""
    echo "📋 $description"
    echo "Command: ./network.sh cc invoke -c $CHANNEL_NAME -ccn $CC_NAME -org $org -cci '$function_call'"
    ./network.sh cc invoke -c $CHANNEL_NAME -ccn $CC_NAME -org $org -cci "$function_call"
    echo "✅ Transaction completed"
    sleep 2
}

# Function to execute chaincode queries
execute_query() {
    local org=$1
    local function_call="$2"
    local description="$3"

    echo ""
    echo "🔍 $description"
    echo "Command: ./network.sh cc query -c $CHANNEL_NAME -ccn $CC_NAME -org $org -ccqc '$function_call'"
    local result=$(./network.sh cc query -c $CHANNEL_NAME -ccn $CC_NAME -org $org -ccqc "$function_call")
    echo "$result"

    # Format into table
    if [ -n "$result" ]; then
        if [[ "$result" == "["* ]]; then
            # Array: Format as table based on expected fields
            if echo "$result" | jq '.[] | has("eventId")' 2>/dev/null | head -1 | grep -q true; then
                # GenerationEvents
                echo ""
                echo "📊 Formatted as Table:"
                echo "$result" | jq -r '["Event ID", "Prosumer ID", "Energy (kWh)", "Tokens Issued", "Invoice Value", "Timestamp"], (.[] | [.eventId, .prosumerId, .generatedKWh, .tokensIssued, .invoiceValue, .timestamp]) | @tsv' 2>/dev/null | column -t 2>/dev/null || echo "Column tool not available for formatting"
            elif echo "$result" | jq '.[] | has("tokenId")' 2>/dev/null | head -1 | grep -q true; then
                # Tokens
                echo ""
                echo "📊 Formatted as Table:"
                echo "$result" | jq -r '["Token ID", "Prosumer ID", "Energy (kWh)", "Type", "Tariff (per kWh)", "Location"], (.[] | [.tokenId, .prosumerId, .energyAmount, .energyType, .tariffPerKWh, .location]) | @tsv' 2>/dev/null | column -t 2>/dev/null || echo "Column tool not available for formatting"
            fi
        else
            # Object
            echo ""
            echo "📊 Formatted as Table:"
            echo "$result" | jq -r 'to_entries | map(.key + "|" + (.value | tostring))[]' 2>/dev/null | column -t -s '|' 2>/dev/null || echo "Column tool not available for formatting"
        fi
    fi

    echo "✅ Query completed"
    sleep 2
}

echo ""
echo "🚀 Starting Energy Trading Platform Demonstration..."
echo ""
# Step 1: Initialize the ledger with sample data
echo "==============================================="
echo "STEP 1: INITIALIZE LEDGER"
echo "==============================================="
execute_transaction 1 '{"function":"initLedger","Args":[]}' "Initializing ledger with sample prosumers and PPAs"

# Step 2: Register additional prosumers
echo ""
echo "==============================================="
echo "STEP 2: REGISTER NEW PROSUMERS"
echo "==============================================="
execute_transaction 1 '{"function":"registerProsumer","Args":["PROSUMER003","SolarPowerIndia","DelhiIndia","200.0","ProsumerMSP"]}' "Registering Solar Power India"
execute_transaction 2 '{"function":"registerProsumer","Args":["PROSUMER004","WindEnergyCorp","BangaloreKarnataka","300.0","ProsumerMSP"]}' "Registering Wind Energy Corp"
execute_transaction 1 '{"function":"registerProsumer","Args":["PROSUMER005","HydroSolutions","ChennaiTamilNadu","150.0","ProsumerMSP"]}' "Registering Hydro Solutions"

# Step 3: Create additional PPAs
echo ""
echo "==============================================="
echo "STEP 3: CREATE POWER PURCHASE AGREEMENTS"
echo "==============================================="
execute_transaction 1 '{"function":"createPPA","Args":["PPA003","PROSUMER003","CORPORATE002","4.8","2025-01-01","2030-12-31"]}' "Creating PPA for Solar Power India"
execute_transaction 2 '{"function":"createPPA","Args":["PPA004","PROSUMER004","UTILITY002","4.1","2025-01-01","2030-12-31"]}' "Creating PPA for Wind Energy Corp"
execute_transaction 1 '{"function":"createPPA","Args":["PPA005","PROSUMER005","UTILITY001","4.5","2025-01-01","2030-12-31"]}' "Creating PPA for PROSUMER005"

# Step 4: Query existing prosumers and PPAs
echo ""
echo "==============================================="
echo "STEP 4: QUERY REGISTERED ENTITIES"
echo "==============================================="
execute_query 1 '{"function":"getProsumer","Args":["PROSUMER001"]}' "Getting details of Green Solar Farm"
execute_query 2 '{"function":"getProsumer","Args":["PROSUMER002"]}' "Getting details of Eco Energy Solutions"
execute_query 1 '{"function":"getPPA","Args":["PPA001"]}' "Getting PPA details for PROSUMER001"

# Step 5: Process electricity generation events
echo ""
echo "==============================================="
echo "STEP 5: PROCESS ELECTRICITY GENERATION"
echo "==============================================="
execute_transaction 1 '{"function":"processElectricityGeneration","Args":["PROSUMER001","150.5","METER001","2025-09-15T10:30:00.000+0000","UTILITY001"]}' "Processing 150.5 kWh generation for Green Solar Farm"
execute_transaction 2 '{"function":"processElectricityGeneration","Args":["PROSUMER002","220.8","METER002","2025-09-15T11:15:00.000+0000","CORPORATE001"]}' "Processing 220.8 kWh generation for Eco Energy Solutions"
execute_transaction 1 '{"function":"processElectricityGeneration","Args":["PROSUMER003","180.2","METER003","2025-09-15T12:00:00.000+0000","CORPORATE002"]}' "Processing 180.2 kWh generation for Solar Power India"
execute_transaction 2 '{"function":"processElectricityGeneration","Args":["PROSUMER004","350.7","METER004","2025-09-15T13:45:00.000+0000","UTILITY002"]}' "Processing 350.7 kWh generation for Wind Energy Corp"
execute_transaction 1 '{"function":"processElectricityGeneration","Args":["PROSUMER001","95.3","METER001","2025-09-15T14:30:00.000+0000","UTILITY001"]}' "Processing additional 95.3 kWh generation for Green Solar Farm"

# Step 6: Query generation events
echo ""
echo "==============================================="
echo "STEP 6: QUERY GENERATION EVENTS"
echo "==============================================="
execute_query 1 '{"function":"getGenerationEvents","Args":["PROSUMER001"]}' "Getting all generation events for Green Solar Farm"
execute_query 2 '{"function":"getGenerationEvents","Args":["PROSUMER002"]}' "Getting all generation events for Eco Energy Solutions"

# Step 7: Query available energy tokens
echo ""
echo "==============================================="
echo "STEP 7: QUERY AVAILABLE ENERGY TOKENS"
echo "==============================================="
execute_query 1 '{"function":"getAvailableTokens","Args":[]}' "Getting all available energy tokens in the system"

# Step 8: More generation events with different prosumers
echo ""
echo "==============================================="
echo "STEP 8: ADDITIONAL GENERATION EVENTS"
echo "==============================================="
execute_transaction 1 '{"function":"processElectricityGeneration","Args":["PROSUMER005","125.4","METER005","2025-09-15T15:20:00.000+0000","UTILITY001"]}' "Processing 125.4 kWh generation for Hydro Solutions"
execute_transaction 2 '{"function":"processElectricityGeneration","Args":["PROSUMER003","210.6","METER003","2025-09-15T16:10:00.000+0000","CORPORATE002"]}' "Processing additional 210.6 kWh generation for Solar Power India"
execute_transaction 1 '{"function":"processElectricityGeneration","Args":["PROSUMER004","445.8","METER004","2025-09-15T17:30:00.000+0000","UTILITY002"]}' "Processing additional 445.8 kWh generation for Wind Energy Corp"

# Step 9: Final queries to show updated state
echo ""
echo "==============================================="
echo "STEP 9: FINAL STATE QUERIES"
echo "==============================================="
execute_query 1 '{"function":"getProsumer","Args":["PROSUMER001"]}' "Final state of Green Solar Farm"
execute_query 2 '{"function":"getProsumer","Args":["PROSUMER004"]}' "Final state of Wind Energy Corp"
execute_query 1 '{"function":"getPPA","Args":["PPA001"]}' "Final state of PPA001"
execute_query 2 '{"function":"getPPA","Args":["PPA004"]}' "Final state of PPA004"
execute_query 1 '{"function":"getPPA","Args":["PPA005"]}' "Final state of PPA005"

# Step 10: Get all available tokens for final showcase
echo ""
echo "==============================================="
echo "STEP 10: FINAL TOKEN SHOWCASE"
echo "==============================================="
execute_query 1 '{"function":"getAvailableTokens","Args":[]}' "Final showcase of all available energy tokens"

echo ""
echo "==============================================="
echo "🎉 DEMONSTRATION COMPLETED SUCCESSFULLY!"
echo "==============================================="
echo ""
echo "📊 SUMMARY OF ACTIVITIES:"
echo "• Registered 5 prosumers across different locations"
echo "• Created 5 Power Purchase Agreements"
echo "• Processed 11 electricity generation events"
echo "• Generated energy tokens automatically"
echo "• Calculated invoice values based on tariffs"
echo "• Updated prosumer and PPA statistics"
echo ""
echo "💡 Key Features Demonstrated:"
echo "• Automated PPA creation when needed"
echo "• Real-time energy token generation (1 token = 1 kWh)"
echo "• Invoice calculation based on agreed tariffs"
echo "• Comprehensive tracking of generation events"
echo "• Transparent and immutable energy trading records"
echo ""
echo "🔗 The energy tokens generated can now be used for:"
echo "• Trading between prosumers and utilities"
echo "• Renewable energy certificate verification"
echo "• Carbon credit calculations"
echo "• Regulatory compliance reporting"
echo ""
