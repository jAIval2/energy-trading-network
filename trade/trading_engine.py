#!/usr/bin/env python3
"""
Simple Python Trading Engine - Generates energy trading data
"""

import random
import time
from datetime import datetime
from oracle_client import OracleClient

def main():
    print("=" * 60)
    print("PYTHON ENERGY TRADING ENGINE")
    print("=" * 60)

    # Initialize oracle client
    oracle = OracleClient("http://localhost:3000")

    # Initialize global prosumer_id
    prosumer_id = None

    # Check oracle health
    print("\n1. Checking Oracle Service Status...")
    health = oracle.check_health()
    if health:
        print(f"✓ Oracle Service is healthy")
        print(f"  Connected to blockchain: {health.get('connected', False)}")
    else:
        print("✗ Cannot connect to Oracle Service")
        print("  Make sure oracle service is running on port 3000")
        return

    # Register a new prosumer
    print("\n2. Registering New Prosumer...")
    prosumer_id = f"PYTHON_PROSUMER_{int(time.time())}"
    oracle.register_prosumer(
        prosumer_id=prosumer_id,
        name="Python Test Solar Farm",
        location="Automated Test Location",
        solar_capacity_kw=250.5,
        organization_msp="ProsumerMSP"
    )

    time.sleep(2)  # Give blockchain time to process

    # CREATE PPA FIRST - This fixes the findOrCreatePPA issue!
    print("\n3. Creating PPA...")
    ppa_id = f"PPA_{prosumer_id}_UTILITY001_PYTHON"  # Predictable, consistent ID
    oracle.create_ppa(
        agreement_id=ppa_id,
        prosumer_id=prosumer_id,
        buyer_id="UTILITY001",
        tariff_per_kwh=4.5,
        start_date="2025-01-01",
        end_date="2030-12-31"
    )
    time.sleep(3)  # Give blockchain time to process

    # Simulate energy generation events - now PPA exists!
    print("\n4. Simulating Energy Generation Events...")
    for i in range(3):  # Reduced to 3 events for cleaner output
        generated_kwh = round(random.uniform(50.0, 300.0), 2)
        meter_id = f"METER_{prosumer_id}"
        buyer_id = "UTILITY001"

        print(f"\nEvent {i+1}/3: Generating {generated_kwh} kWh")
        oracle.submit_generation_event(
            prosumer_id=prosumer_id,
            generated_kwh=generated_kwh,
            meter_id=meter_id,
            buyer_id=buyer_id
        )

        time.sleep(3)  # Wait between submissions

    # Query prosumer data
    print("\n5. Querying Prosumer Data...")
    prosumer_data = oracle.query_prosumer(prosumer_id)
    if prosumer_data:
        print(f"✓ Prosumer Data Retrieved:")
        print(f"  Name: {prosumer_data.get('name')}")
        print(f"  Location: {prosumer_data.get('location')}")
        print(f"  Total Energy Generated: {prosumer_data.get('totalEnergyGenerated')} kWh")

    # Query generation events
    print("\n6. Querying Generation Events...")
    events = oracle.query_generation_events(prosumer_id)
    if events:
        print(f"✓ Found {len(events)} generation events")
        for event in events:
            print(f"  - {event.get('eventId')}: {event.get('generatedKWh')} kWh")

    print("\n" + "=" * 60)
    print("TRADING ENGINE COMPLETED SUCCESSFULLY")
    print("=" * 60)

if __name__ == "__main__":
    main()
