const express = require('express');
const cors = require('cors');
const { Gateway, Wallets } = require('fabric-network');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

const logger = require('./logger');

class EnergyOracleService {
    constructor() {
        this.app = express();
        this.gateway = null;
        this.contract = null;
        
        // Middleware
        this.app.use(cors());
        this.app.use(express.json());
        
        // Setup routes
        this.setupRoutes();
    }
    
    // Initialize connection to Fabric network
    async initialize() {
        try {
            logger.info('Initializing oracle service...');
            
            // Load connection profile
            const ccpPath = path.resolve(__dirname, process.env.CONNECTION_PROFILE);
            const ccp = JSON.parse(fs.readFileSync(ccpPath, 'utf8'));
            logger.info('Connection profile loaded', { network: ccp.name });
            
            // Create wallet and load identity
            const walletPath = path.resolve(__dirname, process.env.WALLET_PATH);
            const wallet = await Wallets.newFileSystemWallet(walletPath);
            
            // Check if oracle identity exists, if not create from existing admin
            const identity = await wallet.get(process.env.USER_ID);
            if (!identity) {
                logger.info('Creating oracle identity from existing admin certificates...');
                await this.enrollOracleIdentity(wallet);
            }
            
            // Connect to gateway with retries and graceful fallback if discovery fails
            this.gateway = new Gateway();

            const maxConnectAttempts = 6;
            const baseDelay = 2000; // ms
            let connected = false;
            let lastError = null;

            for (let attempt = 1; attempt <= maxConnectAttempts; attempt++) {
                try {
                    logger.info(`Attempting gateway.connect (attempt ${attempt}/${maxConnectAttempts})...`);
                    await this.gateway.connect(ccp, {
                        wallet,
                        identity: process.env.USER_ID,
                        discovery: { enabled: true, asLocalhost: true }
                    });
                    connected = true;
                    logger.info('Connected to Fabric gateway (with discovery enabled)');
                    break;
                } catch (err) {
                    lastError = err;
                    logger.error(`gateway.connect attempt ${attempt} failed`, err);
                    // exponential backoff
                    const wait = baseDelay * Math.pow(2, attempt - 1);
                    logger.info(`Waiting ${wait}ms before next attempt...`);
                    await new Promise(r => setTimeout(r, wait));
                }
            }

            // If discovery-enabled connection failed, attempt a fallback without discovery
            if (!connected) {
                logger.info('All discovery-enabled connect attempts failed, trying fallback with discovery disabled...');
                try {
                    await this.gateway.connect(ccp, {
                        wallet,
                        identity: process.env.USER_ID,
                        discovery: { enabled: false, asLocalhost: true }
                    });
                    connected = true;
                    logger.info('Connected to Fabric gateway (with discovery disabled)');
                } catch (err) {
                    logger.error('Fallback gateway.connect (discovery disabled) failed', err);
                    throw lastError || err;
                }
            }
            
            // Get network and contract
            let network;
            try {
                network = await this.gateway.getNetwork(process.env.CHANNEL_NAME);
            } catch (err) {
                logger.error('Failed to get network using discovery-enabled gateway', err);
                // If discovery failed, attempt a reconnect with discovery disabled and retry once
                try {
                    logger.info('Reconnecting gateway with discovery disabled as fallback...');
                    // disconnect previous gateway
                    try { await this.gateway.disconnect(); } catch (e) { /* ignore */ }
                    this.gateway = new Gateway();
                    await this.gateway.connect(ccp, { wallet, identity: process.env.USER_ID, discovery: { enabled: false, asLocalhost: true } });
                    logger.info('Reconnected gateway (discovery disabled)');
                    network = await this.gateway.getNetwork(process.env.CHANNEL_NAME);
                } catch (err2) {
                    logger.error('Fallback to discovery-disabled gateway failed', err2);
                    throw err; // throw original discovery error
                }
            }

            this.contract = network.getContract(process.env.CHAINCODE_NAME);
            
            logger.success('Oracle service initialized successfully');
            
        } catch (error) {
            logger.error('Failed to initialize oracle service', error);
            throw error;
        }
    }
    
    // Create oracle identity from existing admin certificates
    async enrollOracleIdentity(wallet) {
        try {
            // Use absolute path
            const basePath = path.resolve(__dirname, '../..');
            const orgPath = path.join(basePath, 'organizations/peerOrganizations/org1.example.com');
            const certPath = path.join(orgPath, 'users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem');
            const keyPath = path.join(orgPath, 'users/Admin@org1.example.com/msp/keystore/priv_sk');

            // Verify files exist
            if (!fs.existsSync(certPath)) {
                throw new Error(`Certificate not found at: ${certPath}`);
            }
            if (!fs.existsSync(keyPath)) {
                throw new Error(`Keystore not found at: ${keyPath}`);
            }

            logger.info(`Loading certificate from: ${certPath}`);
            logger.info(`Loading private key from: ${keyPath}`);

            // Read certificate
            const cert = fs.readFileSync(certPath, 'utf8');

            // Read private key
            const privateKey = fs.readFileSync(keyPath, 'utf8');

            logger.info(`Certificate loaded: ${cert.substring(0, 50)}...`);
            logger.info(`Private key loaded: ${privateKey.substring(0, 50)}...`);

            const identity = {
                credentials: {
                    certificate: cert,
                    privateKey: privateKey,
                },
                mspId: process.env.ORG_MSP_ID,
                type: 'X.509',
            };

            await wallet.put(process.env.USER_ID, identity);
            logger.success('Oracle identity created successfully');

        } catch (error) {
            logger.error('Failed to create oracle identity', error);
            throw error;
        }
    }

    // Test connection to blockchain
    async testConnection() {
        try {
            logger.info('Testing blockchain connection...');

            // Try a simple query - wrap in try-catch since PROSUMER001 may not exist initially
            try {
                const result = await this.contract.evaluateTransaction('getProsumer', 'PROSUMER001');
                logger.success('Connection test successful - can query blockchain');
                return true;
            } catch (error) {
                if (error.message.includes('does not exist')) {
                    // This is expected for initial tests, prosumer doesn't exist yet
                    logger.success('Connection test successful - blockchain reachable (query returned expected not found)');
                    return true;
                }
                throw error; // Re-throw other errors
            }
        } catch (error) {
            logger.error('Connection test failed', error);
            return false;
        }
    }
    
    // Setup REST API routes
    setupRoutes() {
        // Health check
        this.app.get('/health', (req, res) => {
            res.json({ 
                status: 'healthy', 
                connected: this.gateway !== null,
                timestamp: new Date().toISOString()
            });
        });
        
        // Submit energy generation event
        this.app.post('/api/generation', async (req, res) => {
            try {
                logger.info('Received generation event request', req.body);
                
                // Validate request
                const { prosumerId, generatedKWh, meterId, timestamp, buyerId } = req.body;
                
                if (!prosumerId || !generatedKWh || !meterId || !timestamp || !buyerId) {
                    return res.status(400).json({ 
                        error: 'Missing required fields',
                        required: ['prosumerId', 'generatedKWh', 'meterId', 'timestamp', 'buyerId']
                    });
                }
                
                // Submit to blockchain
                const result = await this.submitGenerationEvent({
                    prosumerId,
                    generatedKWh: parseFloat(generatedKWh),
                    meterId,
                    timestamp,
                    buyerId
                });
                
                logger.success('Generation event submitted to blockchain', result);
                res.json({ success: true, result });
                
            } catch (error) {
                logger.error('Error submitting generation event', error);
                res.status(500).json({ 
                    error: 'Failed to submit generation event',
                    message: error.message 
                });
            }
        });
        
        // Register prosumer
        this.app.post('/api/prosumer/register', async (req, res) => {
            try {
                logger.info('Received prosumer registration request', req.body);
                
                const { prosumerId, name, location, solarCapacityKW, organizationMSP } = req.body;
                
                if (!prosumerId || !name || !location || !solarCapacityKW || !organizationMSP) {
                    return res.status(400).json({ 
                        error: 'Missing required fields',
                        required: ['prosumerId', 'name', 'location', 'solarCapacityKW', 'organizationMSP']
                    });
                }
                
                const result = await this.registerProsumer({
                    prosumerId,
                    name,
                    location,
                    solarCapacityKW: parseFloat(solarCapacityKW),
                    organizationMSP
                });
                
                logger.success('Prosumer registered on blockchain', result);
                res.json({ success: true, result });
                
            } catch (error) {
                logger.error('Error registering prosumer', error);
                res.status(500).json({ 
                    error: 'Failed to register prosumer',
                    message: error.message 
                });
            }
        });

        // Create PPA
        this.app.post('/api/ppa/create', async (req, res) => {
            try {
                logger.info('Received PPA creation request', req.body);

                const { agreementId, prosumerId, buyerId, tariffPerKWh, startDate, endDate } = req.body;

                if (!agreementId || !prosumerId || !buyerId || !tariffPerKWh || !startDate || !endDate) {
                    return res.status(400).json({
                        error: 'Missing required fields',
                        required: ['agreementId', 'prosumerId', 'buyerId', 'tariffPerKWh', 'startDate', 'endDate']
                    });
                }

                const result = await this.contract.submitTransaction(
                    'createPPA',
                    agreementId,
                    prosumerId,
                    buyerId,
                    tariffPerKWh.toString(),
                    startDate,
                    endDate
                );

                logger.success('PPA created on blockchain', JSON.parse(result.toString()));
                res.json({ success: true, result: JSON.parse(result.toString()) });

            } catch (error) {
                logger.error('Error creating PPA', error);
                res.status(500).json({
                    error: 'Failed to create PPA',
                    message: error.message
                });
            }
        });

        // Query prosumer
        this.app.get('/api/prosumer/:id', async (req, res) => {
            try {
                const prosumerId = req.params.id;
                logger.info(`Querying prosumer: ${prosumerId}`);
                
                const result = await this.queryProsumer(prosumerId);
                
                logger.success('Prosumer data retrieved', result);
                res.json({ success: true, data: result });
                
            } catch (error) {
                logger.error('Error querying prosumer', error);
                res.status(500).json({ 
                    error: 'Failed to query prosumer',
                    message: error.message 
                });
            }
        });
        
        // Query generation events
        this.app.get('/api/generation/:prosumerId', async (req, res) => {
            try {
                const prosumerId = req.params.prosumerId;
                logger.info(`Querying generation events for: ${prosumerId}`);
                
                const result = await this.queryGenerationEvents(prosumerId);
                
                logger.success('Generation events retrieved', { count: result.length });
                res.json({ success: true, data: result });
                
            } catch (error) {
                logger.error('Error querying generation events', error);
                res.status(500).json({ 
                    error: 'Failed to query generation events',
                    message: error.message 
                });
            }
        });
    }
    
    // Blockchain interaction methods
    async submitGenerationEvent(eventData) {
        const result = await this.contract.submitTransaction(
            'processElectricityGeneration',
            eventData.prosumerId,
            eventData.generatedKWh.toString(),
            eventData.meterId,
            eventData.timestamp,
            eventData.buyerId
        );
        
        return JSON.parse(result.toString());
    }
    
    async registerProsumer(prosumerData) {
        const result = await this.contract.submitTransaction(
            'registerProsumer',
            prosumerData.prosumerId,
            prosumerData.name,
            prosumerData.location,
            prosumerData.solarCapacityKW.toString(),
            prosumerData.organizationMSP
        );
        
        return JSON.parse(result.toString());
    }
    
    async queryProsumer(prosumerId) {
        const result = await this.contract.evaluateTransaction('getProsumer', prosumerId);
        return JSON.parse(result.toString());
    }
    
    async queryGenerationEvents(prosumerId) {
        const result = await this.contract.evaluateTransaction('getGenerationEvents', prosumerId);
        return JSON.parse(result.toString());
    }
    
    // Start the oracle service
    async start() {
        try {
            await this.initialize();

            // Test connection (optional, continues even if fails)
            try {
                await this.testConnection();
            } catch (error) {
                logger.info('Connection test failed, but service continues - start network with: cd ../ && ./network.sh up');
            }

            const port = process.env.PORT || 3000;
            this.app.listen(port, () => {
                logger.success(`Oracle service running on port ${port}`);
                logger.info('Ready to receive data from Python trading engine');
            });

        } catch (error) {
            logger.error('Failed to start oracle service', error);
            process.exit(1);
        }
    }
    
    // Graceful shutdown
    async shutdown() {
        logger.info('Shutting down oracle service...');
        if (this.gateway) {
            await this.gateway.disconnect();
        }
        process.exit(0);
    }
}

// Handle shutdown signals
const oracle = new EnergyOracleService();

process.on('SIGINT', () => oracle.shutdown());
process.on('SIGTERM', () => oracle.shutdown());

// Start the service
oracle.start();
