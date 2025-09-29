import requests
import json
from datetime import datetime
import time

class OracleClient:
    """Client for sending data to Oracle Service"""
    
    def __init__(self, oracle_url="http://localhost:3000"):
        self.oracle_url = oracle_url
        self.session = requests.Session()
    
    def check_health(self):
        """Check if oracle service is running"""
        try:
            response = self.session.get(f"{self.oracle_url}/health")
            return response.json()
        except Exception as e:
            print(f"Error checking oracle health: {e}")
            return None
    
    def submit_generation_event(self, prosumer_id, generated_kwh, meter_id, buyer_id):
        """Submit energy generation event to blockchain via oracle"""
        timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.000+0000")
        
        payload = {
            "prosumerId": prosumer_id,
            "generatedKWh": generated_kwh,
            "meterId": meter_id,
            "timestamp": timestamp,
            "buyerId": buyer_id
        }
        
        try:
            response = self.session.post(
                f"{self.oracle_url}/api/generation",
                json=payload,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                result = response.json()
                print(f"✓ Generation event submitted successfully")
                print(f"  Event ID: {result['result']['eventId']}")
                print(f"  Tokens Issued: {result['result']['tokensIssued']}")
                print(f"  Invoice Value: {result['result']['invoiceValue']}")
                return result
            else:
                print(f"✗ Failed to submit: {response.text}")
                return None
                
        except Exception as e:
            print(f"✗ Error submitting generation event: {e}")
            return None
    
    def register_prosumer(self, prosumer_id, name, location, solar_capacity_kw, organization_msp):
        """Register new prosumer on blockchain"""
        payload = {
            "prosumerId": prosumer_id,
            "name": name,
            "location": location,
            "solarCapacityKW": solar_capacity_kw,
            "organizationMSP": organization_msp
        }
        
        try:
            response = self.session.post(
                f"{self.oracle_url}/api/prosumer/register",
                json=payload,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                result = response.json()
                print(f"✓ Prosumer registered successfully: {prosumer_id}")
                return result
            else:
                print(f"✗ Failed to register: {response.text}")
                return None
                
        except Exception as e:
            print(f"✗ Error registering prosumer: {e}")
            return None
    
    def query_prosumer(self, prosumer_id):
        """Query prosumer data from blockchain"""
        try:
            response = self.session.get(f"{self.oracle_url}/api/prosumer/{prosumer_id}")
            
            if response.status_code == 200:
                return response.json()['data']
            else:
                print(f"✗ Failed to query: {response.text}")
                return None
                
        except Exception as e:
            print(f"✗ Error querying prosumer: {e}")
            return None
    
    def query_generation_events(self, prosumer_id):
        """Query generation events for a prosumer"""
        try:
            response = self.session.get(f"{self.oracle_url}/api/generation/{prosumer_id}")

            if response.status_code == 200:
                return response.json()['data']
            else:
                print(f"✗ Failed to query: {response.text}")
                return None

        except Exception as e:
            print(f"✗ Error querying generation events: {e}")
            return None

    def create_ppa(self, agreement_id, prosumer_id, buyer_id, tariff_per_kwh, start_date, end_date):
        """Create PPA on blockchain"""
        payload = {
            "agreementId": agreement_id,
            "prosumerId": prosumer_id,
            "buyerId": buyer_id,
            "tariffPerKWh": tariff_per_kwh,
            "startDate": start_date,
            "endDate": end_date
        }

        try:
            response = self.session.post(
                f"{self.oracle_url}/api/ppa/create",
                json=payload,
                headers={"Content-Type": "application/json"}
            )

            if response.status_code == 200:
                result = response.json()
                print(f"✓ PPA created successfully: {agreement_id}")
                return result
            else:
                print(f"✗ Failed to create PPA: {response.text}")
                return None

        except Exception as e:
            print(f"✗ Error creating PPA: {e}")
            return None
