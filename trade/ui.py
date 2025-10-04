#!/usr/bin/env python3
"""
Energy Trading Platform - Proof of Concept GUI
Multi-page Streamlit application for blockchain-based energy trading
"""

import streamlit as st
import pandas as pd
import numpy as np
import plotly.graph_objects as go
import plotly.express as px
from datetime import datetime, timedelta
import json
import time
import requests
from pathlib import Path
import random
import networkx as nx

# Page configuration
st.set_page_config(
    page_title="Energy Trading Platform",
    page_icon="⚡",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Custom CSS for better styling
st.markdown("""
<style>
    .stMetric {
        background-color: #f0f2f6;
        padding: 10px;
        border-radius: 5px;
    }
    .success-box {
        padding: 1rem;
        border-radius: 0.5rem;
        background-color: #d4edda;
        border: 1px solid #c3e6cb;
        color: #155724;
    }
    .warning-box {
        padding: 1rem;
        border-radius: 0.5rem;
        background-color: #fff3cd;
        border: 1px solid #ffeaa7;
        color: #856404;
    }
    div[data-testid="stSidebar"] {
        background-color: #1e3a5f;
    }
    div[data-testid="stSidebar"] .stMarkdown {
        color: white;
    }
</style>
""", unsafe_allow_html=True)

# Initialize session state
if 'oracle_client' not in st.session_state:
    st.session_state.oracle_client = None
if 'prosumers' not in st.session_state:
    st.session_state.prosumers = []
if 'consumers' not in st.session_state:
    st.session_state.consumers = []
if 'marketplace_data' not in st.session_state:
    st.session_state.marketplace_data = []
if 'network_status' not in st.session_state:
    st.session_state.network_status = "Not Connected"
if 'billing_data' not in st.session_state:
    st.session_state.billing_data = {}
if 'transaction_history' not in st.session_state:
    st.session_state.transaction_history = []

# Oracle Client Class
class OracleClient:
    def __init__(self, oracle_url="http://localhost:3000"):
        self.oracle_url = oracle_url
        self.session = requests.Session()

    def check_health(self):
        try:
            response = self.session.get(f"{self.oracle_url}/health")
            return response.json()
        except:
            return None

    def submit_generation_event(self, prosumer_id, generated_kwh, meter_id, buyer_id):
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
                return response.json()
        except Exception as e:
            st.error(f"Error: {e}")
        return None

    def register_prosumer(self, prosumer_id, name, location, capacity, msp):
        payload = {
            "prosumerId": prosumer_id,
            "name": name,
            "location": location,
            "solarCapacityKW": capacity,
            "organizationMSP": msp
        }
        try:
            response = self.session.post(
                f"{self.oracle_url}/api/prosumer/register",
                json=payload
            )
            if response.status_code == 200:
                return response.json()
        except:
            pass
        return None

# Sidebar Navigation
with st.sidebar:
    st.image("https://via.placeholder.com/300x100/1e3a5f/ffffff?text=Energy+Trading", use_column_width=True)
    st.markdown("---")

    st.markdown("## 🔌 Navigation")
    page = st.radio("", [
        "🏠 Dashboard",
        "🌐 Network Topology",
        "📄 Bill Processing",
        "💰 Marketplace",
        "📊 Analytics",
        "⚙️ System Config"
    ])

    st.markdown("---")

    # Network Status Indicator
    if st.session_state.network_status == "Connected":
        st.success("🟢 Blockchain Connected")
    else:
        st.error("🔴 Blockchain Disconnected")

    # Quick Stats
    st.markdown("### 📈 Quick Stats")
    col1, col2 = st.columns(2)
    with col1:
        st.metric("Prosumers", len(st.session_state.prosumers))
        st.metric("Active Trades", len(st.session_state.marketplace_data))
    with col2:
        st.metric("Consumers", len(st.session_state.consumers))
        st.metric("Transactions", len(st.session_state.transaction_history))

# Main Content Area
if page == "🏠 Dashboard":
    st.title("⚡ Energy Trading Platform - Dashboard")
    st.markdown("### Real-time Overview of the Energy Trading Ecosystem")

    # KPI Metrics
    col1, col2, col3, col4 = st.columns(4)

    with col1:
        total_generation = random.randint(15000, 25000)
        st.metric(
            "Total Generation",
            f"{total_generation:,} kWh",
            f"+{random.randint(5, 15)}%"
        )

    with col2:
        total_consumption = random.randint(12000, 20000)
        st.metric(
            "Total Consumption",
            f"{total_consumption:,} kWh",
            f"+{random.randint(3, 10)}%"
        )

    with col3:
        avg_price = round(random.uniform(4.0, 5.5), 2)
        st.metric(
            "Avg Token Price",
            f"₹{avg_price}",
            f"{random.choice(['+', '-'])}{random.uniform(0.1, 0.5):.2f}"
        )

    with col4:
        carbon_saved = random.randint(5000, 10000)
        st.metric(
            "Carbon Saved",
            f"{carbon_saved} tons",
            f"+{random.randint(10, 25)}%"
        )

    st.markdown("---")

    # Real-time Generation and Consumption Chart
    col1, col2 = st.columns([2, 1])

    with col1:
        st.markdown("#### 📈 Energy Flow (Last 24 Hours)")

        # Generate sample time series data
        hours = pd.date_range(end=datetime.now(), periods=24, freq='H')
        generation_data = np.random.normal(1000, 200, 24)
        consumption_data = np.random.normal(900, 150, 24)

        fig = go.Figure()
        fig.add_trace(go.Scatter(
            x=hours, y=generation_data,
            mode='lines+markers',
            name='Generation',
            line=dict(color='green', width=2),
            fill='tozeroy'
        ))
        fig.add_trace(go.Scatter(
            x=hours, y=consumption_data,
            mode='lines+markers',
            name='Consumption',
            line=dict(color='orange', width=2),
            fill='tozeroy'
        ))
        fig.update_layout(
            height=400,
            xaxis_title="Time",
            yaxis_title="Energy (kWh)",
            hovermode='x unified'
        )
        st.plotly_chart(fig, use_container_width=True)

    with col2:
        st.markdown("#### 🎯 System Health")

        health_metrics = {
            "Blockchain": random.randint(95, 100),
            "Oracle Service": random.randint(92, 100),
            "Smart Contracts": random.randint(98, 100),
            "Network Latency": random.randint(88, 98)
        }

        for metric, value in health_metrics.items():
            st.progress(value/100)
            st.caption(f"{metric}: {value}%")

    # Recent Transactions
    st.markdown("---")
    st.markdown("#### 📜 Recent Transactions")

    transactions = []
    for i in range(5):
        transactions.append({
            "Time": (datetime.now() - timedelta(minutes=random.randint(1, 60))).strftime("%H:%M:%S"),
            "Type": random.choice(["Token Mint", "Trade", "Settlement"]),
            "From": f"PROSUMER_{random.randint(1, 20):03d}",
            "To": f"CONSUMER_{random.randint(1, 50):03d}",
            "Amount": f"{random.randint(50, 500)} kWh",
            "Status": "✅ Confirmed"
        })

    df_transactions = pd.DataFrame(transactions)
    st.dataframe(df_transactions, use_container_width=True, hide_index=True)

elif page == "🌐 Network Topology":
    st.title("🌐 Blockchain Network Topology")
    st.markdown("### Visual representation of the blockchain network architecture")

    # Network controls
    col1, col2, col3 = st.columns([1, 1, 2])

    with col1:
        if st.button("🔄 Refresh Network", type="primary"):
            st.session_state.network_status = "Connected"
            st.rerun()

    with col2:
        if st.button("📡 Test Connection"):
            with st.spinner("Testing connection..."):
                time.sleep(1)
                st.success("Connection successful!")

    # Create network graph
    st.markdown("---")

    # Create a directed graph
    G = nx.DiGraph()

    # Add nodes
    # Orderers
    orderers = ["Orderer1", "Orderer2", "Orderer3"]
    for orderer in orderers:
        G.add_node(orderer, node_type="orderer")

    # Peer organizations
    orgs = ["Org1-Peer1", "Org1-Peer2", "Org2-Peer1", "Org2-Peer2"]
    for org in orgs:
        G.add_node(org, node_type="peer")

    # Oracle and services
    services = ["Oracle Service", "CA-Org1", "CA-Org2"]
    for service in services:
        G.add_node(service, node_type="service")

    # Add edges (connections)
    # Connect orderers to peers
    for orderer in orderers:
        for org in orgs:
            G.add_edge(orderer, org)

    # Connect peers to each other (gossip)
    for i in range(len(orgs)-1):
        G.add_edge(orgs[i], orgs[i+1])

    # Connect services
    G.add_edge("Oracle Service", "Org1-Peer1")
    G.add_edge("CA-Org1", "Org1-Peer1")
    G.add_edge("CA-Org1", "Org1-Peer2")
    G.add_edge("CA-Org2", "Org2-Peer1")
    G.add_edge("CA-Org2", "Org2-Peer2")

    # Calculate layout
    pos = nx.spring_layout(G, k=2, iterations=50)

    # Create Plotly figure
    edge_trace = []
    for edge in G.edges():
        x0, y0 = pos[edge[0]]
        x1, y1 = pos[edge[1]]
        edge_trace.append(go.Scatter(
            x=[x0, x1, None],
            y=[y0, y1, None],
            mode='lines',
            line=dict(width=1, color='#888'),
            hoverinfo='none'
        ))

    # Node traces by type
    node_traces = {}
    node_types = {"orderer": "#FF6B6B", "peer": "#4ECDC4", "service": "#45B7D1"}

    for node_type, color in node_types.items():
        node_trace = go.Scatter(
            x=[],
            y=[],
            text=[],
            mode='markers+text',
            textposition="top center",
            hoverinfo='text',
            marker=dict(
                showscale=False,
                color=color,
                size=20,
                line_width=2
            )
        )

        for node in G.nodes():
            if G.nodes[node].get('node_type') == node_type:
                x, y = pos[node]
                node_trace['x'] += tuple([x])
                node_trace['y'] += tuple([y])
                node_trace['text'] += tuple([node])

        node_traces[node_type] = node_trace

    # Create figure
    fig = go.Figure(data=edge_trace + list(node_traces.values()))
    fig.update_layout(
        showlegend=False,
        hovermode='closest',
        margin=dict(b=0,l=0,r=0,t=0),
        xaxis=dict(showgrid=False, zeroline=False, showticklabels=False),
        yaxis=dict(showgrid=False, zeroline=False, showticklabels=False),
        height=500
    )

    st.plotly_chart(fig, use_container_width=True)

    # Network Statistics
    st.markdown("---")
    col1, col2, col3 = st.columns(3)

    with col1:
        st.markdown("#### 📦 Blocks")
        st.metric("Total Blocks", random.randint(100, 500))
        st.metric("Block Height", random.randint(50, 100))

    with col2:
        st.markdown("#### ⚙️ Smart Contracts")
        st.metric("Deployed Contracts", 3)
        st.metric("Total Invocations", random.randint(1000, 5000))

    with col3:
        st.markdown("#### 🔗 Channels")
        st.metric("Active Channels", 2)
        st.metric("Organizations", 2)

elif page == "📄 Bill Processing":
    st.title("📄 Electricity Bill Processing & Tokenization")
    st.markdown("### Process monthly bills and mint tokens for excess generation")

    # Bill Upload Section
    tab1, tab2, tab3 = st.tabs(["📤 Upload Bill", "🔍 Parse & Analyze", "🪙 Tokenize"])

    with tab1:
        st.markdown("#### Upload Electricity Bill")

        col1, col2 = st.columns([2, 1])

        with col1:
            # File uploader
            uploaded_file = st.file_uploader(
                "Choose a bill file (PDF, Image, or CSV)",
                type=['pdf', 'png', 'jpg', 'jpeg', 'csv']
            )

            # Manual entry option
            st.markdown("##### Or Enter Bill Details Manually")

            with st.form("manual_bill_entry"):
                col1_form, col2_form = st.columns(2)

                with col1_form:
                    prosumer_id = st.text_input("Prosumer ID", value="PROSUMER_001")
                    meter_id = st.text_input("Meter ID", value="METER_001")
                    billing_month = st.date_input("Billing Month")

                with col2_form:
                    units_generated = st.number_input("Units Generated (kWh)", min_value=0.0, value=1500.0)
                    units_consumed = st.number_input("Units Consumed (kWh)", min_value=0.0, value=800.0)
                    tariff_rate = st.number_input("Tariff Rate (₹/kWh)", min_value=0.0, value=4.5)

                submit_bill = st.form_submit_button("Process Bill", type="primary")

                if submit_bill:
                    excess_units = units_generated - units_consumed
                    st.session_state.billing_data = {
                        "prosumer_id": prosumer_id,
                        "meter_id": meter_id,
                        "billing_month": billing_month.strftime("%Y-%m"),
                        "units_generated": units_generated,
                        "units_consumed": units_consumed,
                        "excess_units": excess_units,
                        "tariff_rate": tariff_rate,
                        "token_value": excess_units * tariff_rate
                    }
                    st.success("✅ Bill processed successfully!")

        with col2:
            # Bill Summary Card
            st.markdown("##### 📊 Quick Summary")
            if st.session_state.billing_data:
                data = st.session_state.billing_data
                st.info(f"""
                **Prosumer:** {data.get('prosumer_id', 'N/A')}
                **Month:** {data.get('billing_month', 'N/A')}
                **Generated:** {data.get('units_generated', 0):.2f} kWh
                **Consumed:** {data.get('units_consumed', 0):.2f} kWh
                **Excess:** {data.get('excess_units', 0):.2f} kWh
                **Token Value:** ₹{data.get('token_value', 0):.2f}
                """)

    with tab2:
        st.markdown("#### Bill Analysis & Verification")

        if st.session_state.billing_data:
            data = st.session_state.billing_data

            # Create visualization
            col1, col2 = st.columns([1, 1])

            with col1:
                # Pie chart of generation vs consumption
                fig = go.Figure(data=[go.Pie(
                    labels=['Consumed', 'Excess (Available for Trading)'],
                    values=[data['units_consumed'], data['excess_units']],
                    hole=.3,
                    marker_colors=['#FF6B6B', '#4ECDC4']
                )])
                fig.update_layout(title="Energy Distribution", height=300)
                st.plotly_chart(fig, use_container_width=True)

            with col2:
                # Bar chart
                categories = ['Generated', 'Consumed', 'Excess']
                values = [data['units_generated'], data['units_consumed'], data['excess_units']]

                fig = go.Figure([go.Bar(
                    x=categories,
                    y=values,
                    marker_color=['green', 'orange', 'blue']
                )])
                fig.update_layout(title="Energy Metrics (kWh)", height=300)
                st.plotly_chart(fig, use_container_width=True)

            # Verification checklist
            st.markdown("##### ✅ Verification Checklist")
            col1, col2, col3 = st.columns(3)

            with col1:
                st.checkbox("Meter reading verified", value=True)
                st.checkbox("Generation data accurate", value=True)
            with col2:
                st.checkbox("Tariff rate confirmed", value=True)
                st.checkbox("Billing period correct", value=True)
            with col3:
                st.checkbox("Prosumer ID validated", value=True)
                st.checkbox("Smart meter synced", value=True)
        else:
            st.warning("⚠️ Please process a bill first in the 'Upload Bill' tab")

    with tab3:
        st.markdown("#### 🪙 Token Minting & Blockchain Registration")

        if st.session_state.billing_data and st.session_state.billing_data['excess_units'] > 0:
            data = st.session_state.billing_data

            col1, col2 = st.columns([2, 1])

            with col1:
                st.markdown("##### Token Details")

                token_amount = data['excess_units']
                token_value = data['token_value']

                st.success(f"""
                **Tokens to Mint:** {token_amount:.2f} ERC (Energy Credits)
                **Token Value:** ₹{token_value:.2f}
                **Exchange Rate:** 1 ERC = 1 kWh
                **Valid Until:** {(datetime.now() + timedelta(days=90)).strftime("%Y-%m-%d")}
                """)

                # Minting options
                st.markdown("##### Minting Options")

                col1_mint, col2_mint = st.columns(2)
                with col1_mint:
                    mint_percentage = st.slider(
                        "Percentage to Tokenize",
                        min_value=0,
                        max_value=100,
                        value=100,
                        step=10
                    )

                with col2_mint:
                    buyer_preference = st.selectbox(
                        "Preferred Buyer Category",
                        ["Any", "Utility Companies", "Commercial", "Residential", "Industrial"]
                    )

                min_price = st.number_input(
                    "Minimum Selling Price (₹/kWh)",
                    min_value=0.0,
                    max_value=10.0,
                    value=data['tariff_rate'],
                    step=0.1
                )

                # Mint button
                if st.button("🚀 Mint Tokens on Blockchain", type="primary", use_container_width=True):
                    with st.spinner("Minting tokens on blockchain..."):
                        # Simulate blockchain interaction
                        time.sleep(2)

                        # Add to marketplace
                        st.session_state.marketplace_data.append({
                            "token_id": f"TOKEN_{random.randint(1000, 9999)}",
                            "prosumer_id": data['prosumer_id'],
                            "amount": token_amount * (mint_percentage/100),
                            "price": min_price,
                            "status": "Available"
                        })

                        st.success("✅ Tokens minted successfully!")
                        st.balloons()

                        # Transaction details
                        st.info(f"""
                        **Transaction Hash:** 0x{random.randbytes(32).hex()}
                        **Block Number:** {random.randint(1000, 5000)}
                        **Gas Used:** {random.randint(50000, 100000)}
                        **Timestamp:** {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
                        """)

            with col2:
                st.markdown("##### 📈 Market Conditions")

                # Current market price
                current_price = random.uniform(4.0, 5.5)
                price_change = random.uniform(-0.5, 0.5)

                st.metric(
                    "Current Market Price",
                    f"₹{current_price:.2f}",
                    f"{price_change:+.2f}"
                )

                st.metric(
                    "24h Volume",
                    f"{random.randint(10000, 50000)} kWh"
                )

                st.metric(
                    "Active Buyers",
                    random.randint(20, 50)
                )
        else:
            st.warning("⚠️ No excess units available for tokenization. Please process a bill with excess generation.")

elif page == "💰 Marketplace":
    st.title("💰 Energy Credit Marketplace")
    st.markdown("### P2P Trading Platform for Energy Credits")

    # Marketplace tabs
    tab1, tab2, tab3, tab4 = st.tabs(["🛒 Buy Credits", "💼 Sell Credits", "📊 Order Book", "📜 Trade History"])

    with tab1:
        st.markdown("#### Available Energy Credits")

        # Filters
        col1, col2, col3, col4 = st.columns(4)
        with col1:
            price_filter = st.slider("Max Price (₹/kWh)", 0.0, 10.0, 5.0)
        with col2:
            amount_filter = st.slider("Min Amount (kWh)", 0, 1000, 100)
        with col3:
            location_filter = st.selectbox("Location", ["All", "Mumbai", "Pune", "Delhi", "Bangalore"])
        with col4:
            source_filter = st.selectbox("Energy Source", ["All", "Solar", "Wind", "Hydro"])

        # Generate sample marketplace data
        available_credits = []
        for i in range(10):
            available_credits.append({
                "Seller": f"PROSUMER_{random.randint(1, 20):03d}",
                "Amount (kWh)": random.randint(100, 1000),
                "Price (₹/kWh)": round(random.uniform(3.5, 5.5), 2),
                "Source": random.choice(["Solar", "Wind", "Hydro"]),
                "Location": random.choice(["Mumbai", "Pune", "Delhi", "Bangalore"]),
                "Rating": f"⭐ {random.uniform(4.0, 5.0):.1f}",
                "Action": "Buy"
            })

        df_credits = pd.DataFrame(available_credits)

        # Display filtered results
        filtered_df = df_credits[df_credits["Price (₹/kWh)"] <= price_filter]
        if amount_filter > 0:
            filtered_df = filtered_df[filtered_df["Amount (kWh)"] >= amount_filter]

        st.dataframe(filtered_df, use_container_width=True, hide_index=True)

        # Quick buy section
        st.markdown("---")
        st.markdown("##### Quick Buy")

        col1, col2, col3 = st.columns(3)
        with col1:
            selected_seller = st.selectbox("Select Seller", filtered_df["Seller"].tolist() if not filtered_df.empty else [])
        with col2:
            buy_amount = st.number_input("Amount (kWh)", min_value=10, max_value=1000, value=100)
        with col3:
            if st.button("Execute Trade", type="primary"):
                st.success(f"✅ Trade executed! Bought {buy_amount} kWh from {selected_seller}")

    with tab2:
        st.markdown("#### List Your Energy Credits")

        with st.form("sell_credits_form"):
            col1, col2 = st.columns(2)

            with col1:
                sell_amount = st.number_input("Amount to Sell (kWh)", min_value=10.0, value=100.0)
                sell_price = st.number_input("Price per kWh (₹)", min_value=0.0, value=4.5)
                energy_source = st.selectbox("Energy Source", ["Solar", "Wind", "Hydro", "Biomass"])

            with col2:
                validity_days = st.number_input("Validity (days)", min_value=1, max_value=90, value=30)
                min_purchase = st.number_input("Minimum Purchase (kWh)", min_value=10, value=50)
                auto_renew = st.checkbox("Auto-renew listing")

            if st.form_submit_button("List Credits", type="primary"):
                st.success(f"✅ Listed {sell_amount} kWh at ₹{sell_price}/kWh")
                st.info(f"Your listing ID: #LST{random.randint(10000, 99999)}")

    with tab3:
        st.markdown("#### Live Order Book")

        col1, col2 = st.columns(2)

        with col1:
            st.markdown("##### 🟢 Buy Orders")
            buy_orders = []
            for i in range(5):
                buy_orders.append({
                    "Price": f"₹{random.uniform(3.5, 4.5):.2f}",
                    "Amount": f"{random.randint(100, 500)} kWh",
                    "Total": f"₹{random.randint(500, 2000)}"
                })
            st.dataframe(pd.DataFrame(buy_orders), use_container_width=True, hide_index=True)

        with col2:
            st.markdown("##### 🔴 Sell Orders")
            sell_orders = []
            for i in range(5):
                sell_orders.append({
                    "Price": f"₹{random.uniform(4.5, 5.5):.2f}",
                    "Amount": f"{random.randint(100, 500)} kWh",
                    "Total": f"₹{random.randint(500, 2000)}"
                })
            st.dataframe(pd.DataFrame(sell_orders), use_container_width=True, hide_index=True)

        # Price chart
        st.markdown("---")
        st.markdown("##### 📈 Price Chart (24h)")

        hours = pd.date_range(end=datetime.now(), periods=24, freq='H')
        prices = np.random.normal(4.5, 0.3, 24)

        fig = go.Figure()
        fig.add_trace(go.Scatter(
            x=hours,
            y=prices,
            mode='lines',
            name='Price',
            line=dict(color='blue', width=2)
        ))
        fig.update_layout(
            height=300,
            xaxis_title="Time",
            yaxis_title="Price (₹/kWh)",
            hovermode='x'
        )
        st.plotly_chart(fig, use_container_width=True)

    with tab4:
        st.markdown("#### Recent Trades")

        # Generate trade history
        trades = []
        for i in range(20):
            trades.append({
                "Time": (datetime.now() - timedelta(minutes=random.randint(1, 1440))).strftime("%Y-%m-%d %H:%M"),
                "Type": random.choice(["Buy", "Sell"]),
                "Amount": f"{random.randint(50, 500)} kWh",
                "Price": f"₹{random.uniform(4.0, 5.0):.2f}",
                "Counterparty": f"USER_{random.randint(1, 100):03d}",
                "Status": random.choice(["✅ Completed", "⏳ Pending", "✅ Completed"])
            })

        df_trades = pd.DataFrame(trades)
        st.dataframe(df_trades, use_container_width=True, hide_index=True)

elif page == "📊 Analytics":
    st.title("📊 Analytics & Insights")
    st.markdown("### Comprehensive analysis of energy trading patterns")

    # Date range selector
    col1, col2, col3 = st.columns([1, 1, 2])
    with col1:
        start_date = st.date_input("Start Date", datetime.now() - timedelta(days=30))
    with col2:
        end_date = st.date_input("End Date", datetime.now())

    st.markdown("---")

    # Analytics tabs
    tab1, tab2, tab3, tab4 = st.tabs(["📈 Market Analytics", "🏆 Leaderboard", "🌍 Environmental Impact", "💹 Financial Reports"])

    with tab1:
        col1, col2 = st.columns([2, 1])

        with col1:
            # Trading volume over time
            st.markdown("#### Trading Volume Trends")

            days = pd.date_range(start=start_date, end=end_date, freq='D')
            volume = np.random.normal(10000, 2000, len(days))

            fig = go.Figure()
            fig.add_trace(go.Bar(x=days, y=volume, name='Daily Volume', marker_color='lightblue'))
            fig.update_layout(xaxis_title="Date", yaxis_title="Volume (kWh)")
            st.plotly_chart(fig, use_container_width=True)

        with col2:
            st.markdown("#### Key Metrics")
            st.metric("Total Volume", f"{random.randint(100000, 500000):,} kWh")
            st.metric("Avg Daily Volume", f"{random.randint(5000, 15000):,} kWh")
            st.metric("Peak Trading Hour", f"{random.randint(10, 16)}:00")
            st.metric("Active Traders", random.randint(50, 150))

    with tab2:
        st.markdown("#### Top Performers")

        # Top prosumers
        col1, col2 = st.columns(2)

        with col1:
            st.markdown("##### 🏅 Top Prosumers (by Generation)")
            top_prosumers = []
            for i in range(5):
                top_prosumers.append({
                    "Rank": i+1,
                    "Prosumer": f"PROSUMER_{random.randint(1, 20):03d}",
                    "Generation": f"{random.randint(5000, 15000):,} kWh",
                    "Tokens": f"{random.randint(1000, 5000):,}"
                })
            st.dataframe(pd.DataFrame(top_prosumers), use_container_width=True, hide_index=True)

        with col2:
            st.markdown("##### 💰 Top Traders (by Volume)")
            top_traders = []
            for i in range(5):
                top_traders.append({
                    "Rank": i+1,
                    "Trader": f"TRADER_{random.randint(1, 50):03d}",
                    "Volume": f"{random.randint(3000, 10000):,} kWh",
                    "Trades": random.randint(50, 200)
                })
            st.dataframe(pd.DataFrame(top_traders), use_container_width=True, hide_index=True)

    with tab3:
        st.markdown("#### Environmental Impact Dashboard")

        col1, col2, col3 = st.columns(3)

        with col1:
            carbon_saved = random.randint(5000, 15000)
            st.metric("CO₂ Reduced", f"{carbon_saved:,} tons", "+12%")

            # Equivalent trees
            trees = carbon_saved * 50
            st.info(f"🌳 Equivalent to planting {trees:,} trees")

        with col2:
            renewable_percentage = random.randint(60, 85)
            st.metric("Renewable Energy %", f"{renewable_percentage}%", "+5%")

            # Progress bar
            st.progress(renewable_percentage/100)
            st.caption("Target: 100% by 2030")

        with col3:
            households_powered = random.randint(1000, 5000)
            st.metric("Households Powered", f"{households_powered:,}", "+8%")

            st.info(f"💡 Clean energy for {households_powered*4:,} people")

    with tab4:
        st.markdown("#### Financial Summary")

        # Revenue metrics
        col1, col2, col3, col4 = st.columns(4)

        with col1:
            st.metric("Total Revenue", f"₹{random.randint(100000, 500000):,}", "+15%")
        with col2:
            st.metric("Avg Token Price", f"₹{random.uniform(4.0, 5.0):.2f}", "+0.3")
        with col3:
            st.metric("Transaction Fees", f"₹{random.randint(5000, 15000):,}", "+10%")
        with col4:
            st.metric("Market Cap", f"₹{random.randint(1000000, 5000000):,}", "+20%")

elif page == "⚙️ System Config":
    st.title("⚙️ System Configuration")
    st.markdown("### Manage blockchain network and system settings")

    tab1, tab2, tab3, tab4 = st.tabs(["🔧 Network Config", "👥 User Management", "📝 Smart Contracts", "🔐 Security"])

    with tab1:
        st.markdown("#### Blockchain Network Configuration")

        col1, col2 = st.columns(2)

        with col1:
            st.markdown("##### Oracle Settings")
            oracle_url = st.text_input("Oracle Service URL", value="http://localhost:3000")

            if st.button("Test Oracle Connection"):
                with st.spinner("Testing connection..."):
                    if not st.session_state.oracle_client:
                        st.session_state.oracle_client = OracleClient(oracle_url)

                    health = st.session_state.oracle_client.check_health()
                    if health:
                        st.success(f"✅ Oracle is healthy: {health}")
                        st.session_state.network_status = "Connected"
                    else:
                        st.error("❌ Cannot connect to Oracle")

            st.markdown("##### Network Parameters")
            block_time = st.number_input("Block Time (seconds)", min_value=1, max_value=60, value=5)
            max_message_count = st.number_input("Max Messages per Block", min_value=10, max_value=1000, value=100)

        with col2:
            st.markdown("##### Channel Configuration")
            channel_name = st.text_input("Channel Name", value="mychannel")
            org_count = st.number_input("Number of Organizations", min_value=2, max_value=10, value=2)
            peer_count = st.number_input("Peers per Organization", min_value=1, max_value=5, value=2)

            if st.button("Apply Configuration", type="primary"):
                st.success("✅ Configuration applied successfully!")

    with tab2:
        st.markdown("#### User Management")

        # Initialize network button
        if st.button("🚀 Initialize Network with Sample Data", type="primary"):
            with st.spinner("Initializing network..."):
                # Generate prosumers
                st.session_state.prosumers = []
                for i in range(20):
                    st.session_state.prosumers.append({
                        "id": f"PROSUMER_{i+1:03d}",
                        "name": f"Solar Farm {i+1}",
                        "location": random.choice(["Mumbai", "Pune", "Delhi", "Bangalore"]),
                        "capacity": random.randint(100, 500)
                    })

                # Generate consumers
                st.session_state.consumers = []
                for i in range(50):
                    consumer_type = random.choice(["Residential", "Commercial", "Industrial"])
                    st.session_state.consumers.append({
                        "id": f"CONSUMER_{i+1:03d}",
                        "type": consumer_type,
                        "location": random.choice(["Mumbai", "Pune", "Delhi", "Bangalore"]),
                        "avg_consumption": random.randint(500, 5000) if consumer_type != "Residential" else random.randint(100, 500)
                    })

                st.success(f"✅ Network initialized with {len(st.session_state.prosumers)} prosumers and {len(st.session_state.consumers)} consumers!")
                time.sleep(1)
                st.rerun()

        # Display users
        col1, col2 = st.columns(2)

        with col1:
            st.markdown("##### Prosumers")
            if st.session_state.prosumers:
                df_prosumers = pd.DataFrame(st.session_state.prosumers)
                st.dataframe(df_prosumers.head(10), use_container_width=True, hide_index=True)
                st.caption(f"Showing 10 of {len(st.session_state.prosumers)} prosumers")

        with col2:
            st.markdown("##### Consumers")
            if st.session_state.consumers:
                df_consumers = pd.DataFrame(st.session_state.consumers)
                st.dataframe(df_consumers.head(10), use_container_width=True, hide_index=True)
                st.caption(f"Showing 10 of {len(st.session_state.consumers)} consumers")

    with tab3:
        st.markdown("#### Smart Contract Management")

        contracts = [
            {"Name": "EnergyTradingContract", "Version": "1.1", "Status": "✅ Deployed", "Invocations": random.randint(1000, 5000)},
            {"Name": "TokenMintContract", "Version": "1.0", "Status": "✅ Deployed", "Invocations": random.randint(500, 2000)},
            {"Name": "SettlementContract", "Version": "1.0", "Status": "✅ Deployed", "Invocations": random.randint(200, 1000)}
        ]

        st.dataframe(pd.DataFrame(contracts), use_container_width=True, hide_index=True)

        # Contract deployment
        st.markdown("---")
        st.markdown("##### Deploy New Contract")

        col1, col2 = st.columns(2)
        with col1:
            contract_file = st.file_uploader("Upload Contract (.java, .go, .js)", type=['java', 'go', 'js'])
        with col2:
            if st.button("Deploy Contract"):
                with st.spinner("Deploying..."):
                    time.sleep(2)
                    st.success("✅ Contract deployed successfully!")

    with tab4:
        st.markdown("#### Security Settings")

        col1, col2 = st.columns(2)

        with col1:
            st.markdown("##### Access Control")
            st.checkbox("Enable TLS", value=True)
            st.checkbox("Mutual TLS Authentication", value=True)
            st.checkbox("Enable Access Control Lists", value=True)

            st.markdown("##### Encryption")
            encryption_type = st.selectbox("Encryption Algorithm", ["AES-256", "RSA-2048", "ECDSA"])

        with col2:
            st.markdown("##### Audit & Compliance")
            st.checkbox("Enable Audit Logging", value=True)
            st.checkbox("GDPR Compliance Mode", value=False)
            st.checkbox("Real-time Monitoring", value=True)

# Footer
st.markdown("---")
st.markdown(
    """
    <div style='text-align: center; color: #888;'>
        <p>Energy Trading Platform v1.0 | Powered by Hyperledger Fabric & Python | © 2025</p>
    </div>
    """,
    unsafe_allow_html=True
)
