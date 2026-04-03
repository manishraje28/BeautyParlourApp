const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { v4: uuidv4 } = require('uuid');
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = 4444;

app.use(cors());
app.use(bodyParser.json());

// ══════════════════════════════════════════════════════════════════
// FIREBASE INITIALIZATION
// ══════════════════════════════════════════════════════════════════

let firebaseDb = null;
try {
    const paths = [
        path.join(__dirname, 'serviceAccountKey.json'),
        path.join(__dirname, 'app', 'serviceAccountKey.json'),
        path.join(__dirname, 'app', 'google-services.json')
    ];

    let serviceAccount = null;
    let foundPath = null;

    for (let p of paths) {
        if (fs.existsSync(p)) {
            serviceAccount = require(p);
            foundPath = p;
            break;
        }
    }

    if (serviceAccount) {
        admin.initializeApp({
            credential: admin.credential.cert(serviceAccount),
            databaseURL: "https://beautyparlourapp-cdd35.firebaseio.com"
        });
        firebaseDb = admin.firestore();
        console.log('✅ Firebase initialized successfully from:', foundPath);
        initializeDefaultData(); // Seed default data if collections are empty
    } else {
        console.log('⚠️  Service account key not found');
        console.log('ℹ️  Expected location: d:\\BeautyParlourApp\\serviceAccountKey.json');
    }
} catch (error) {
    console.log('⚠️  Failed to initialize Firebase:', error.message);
}

// ══════════════════════════════════════════════════════════════════
// INITIALIZE DEFAULT DATA
// ══════════════════════════════════════════════════════════════════

async function initializeDefaultData() {
    if (!firebaseDb) return;

    try {
        // Check if services collection is empty
        const servicesSnap = await firebaseDb.collection('services').limit(1).get();
        if (servicesSnap.empty) {
            console.log('📋 Seeding default services...');
            const defaultServices = [
                { name: 'Haircut', price: 499, duration: '45 mins', description: 'Classic styling and finish', category: 'Hair' },
                { name: 'Facial', price: 899, duration: '60 mins', description: 'Deep cleansing and glowing skin treatment', category: 'Skincare' },
                { name: 'Bridal Makeup', price: 5999, duration: '180 mins', description: 'Complete bridal look by certified artists', category: 'Makeup' },
                { name: 'Hair Spa', price: 1299, duration: '90 mins', description: 'Nourishing spa for smooth, healthy hair', category: 'Hair' },
                { name: 'Waxing', price: 699, duration: '30 mins', description: 'Gentle waxing for clean and soft skin', category: 'Hair Removal' }
            ];

            for (let service of defaultServices) {
                await firebaseDb.collection('services').add(service);
            }
            console.log('✅ Services seeded successfully');
        }

        // Check if offers collection is empty
        const offersSnap = await firebaseDb.collection('offers').limit(1).get();
        if (offersSnap.empty) {
            console.log('🎁 Seeding default offers...');
            const defaultOffers = [
                { title: '20% off Facials', validTill: '2026-04-10', code: 'GLOW20', discount: 20 },
                { title: '15% off Hair Spa', validTill: '2026-04-15', code: 'RELAX15', discount: 15 },
                { title: 'Free Haircut with Spa', validTill: '2026-04-20', code: 'COMBO50', discount: 50 }
            ];

            for (let offer of defaultOffers) {
                await firebaseDb.collection('offers').add(offer);
            }
            console.log('✅ Offers seeded successfully');
        }
    } catch (error) {
        console.error('⚠️  Error seeding data:', error.message);
    }
}

// ══════════════════════════════════════════════════════════════════
// SERVICES ENDPOINTS (FIREBASE)
// ══════════════════════════════════════════════════════════════════

app.get('/api/services', async (req, res) => {
    console.log('📋 GET /api/services');

    if (!firebaseDb) {
        return res.status(503).json({ success: false, error: 'Firebase not initialized' });
    }

    try {
        const snapshot = await firebaseDb.collection('services').get();
        const services = [];
        snapshot.forEach(doc => {
            services.push({ id: doc.id, ...doc.data() });
        });
        res.json({ success: true, data: services });
    } catch (error) {
        console.error('❌ Error:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

app.get('/api/services/:id', async (req, res) => {
    console.log(`📋 GET /api/services/${req.params.id}`);

    if (!firebaseDb) {
        return res.status(503).json({ success: false, error: 'Firebase not initialized' });
    }

    try {
        const doc = await firebaseDb.collection('services').doc(req.params.id).get();
        if (!doc.exists) {
            return res.status(404).json({ success: false, error: 'Service not found' });
        }
        res.json({ success: true, data: { id: doc.id, ...doc.data() } });
    } catch (error) {
        console.error('❌ Error:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

// ══════════════════════════════════════════════════════════════════
// BOOKINGS ENDPOINTS (FIREBASE)
// ══════════════════════════════════════════════════════════════════

app.post('/api/bookings', async (req, res) => {
    console.log('📅 POST /api/bookings', req.body);

    if (!firebaseDb) {
        return res.status(503).json({ success: false, error: 'Firebase not initialized' });
    }

    const { userId, service, date, time } = req.body;
    if (!userId || !service || !date || !time) {
        return res.status(400).json({ success: false, error: 'Missing required fields' });
    }

    try {
        const newBooking = {
            userId,
            service,
            date,
            time,
            status: 'confirmed',
            createdAt: new Date().toISOString()
        };

        const docRef = await firebaseDb.collection('bookings').add(newBooking);
        console.log('✅ Booking created:', docRef.id);

        res.status(201).json({
            success: true,
            data: { id: docRef.id, ...newBooking },
            message: 'Booking confirmed!'
        });
    } catch (error) {
        console.error('❌ Error:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

app.get('/api/bookings/:userId', async (req, res) => {
    console.log(`📅 GET /api/bookings/${req.params.userId}`);

    if (!firebaseDb) {
        return res.status(503).json({ success: false, error: 'Firebase not initialized' });
    }

    try {
        const snapshot = await firebaseDb.collection('bookings')
            .where('userId', '==', req.params.userId)
            .get();

        const userBookings = [];
        snapshot.forEach(doc => {
            userBookings.push({ id: doc.id, ...doc.data() });
        });

        res.json({ success: true, data: userBookings, count: userBookings.length });
    } catch (error) {
        console.error('❌ Error:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

app.delete('/api/bookings/:id', async (req, res) => {
    console.log(`🗑️  DELETE /api/bookings/${req.params.id}`);

    if (!firebaseDb) {
        return res.status(503).json({ success: false, error: 'Firebase not initialized' });
    }

    try {
        const doc = await firebaseDb.collection('bookings').doc(req.params.id).get();
        if (!doc.exists) {
            return res.status(404).json({ success: false, error: 'Booking not found' });
        }

        const bookingData = doc.data();
        await firebaseDb.collection('bookings').doc(req.params.id).delete();
        console.log('✅ Booking cancelled');

        res.json({
            success: true,
            message: 'Booking cancelled',
            data: { id: req.params.id, ...bookingData }
        });
    } catch (error) {
        console.error('❌ Error:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

app.put('/api/bookings/:id', async (req, res) => {
    console.log(`✏️  PUT /api/bookings/${req.params.id}`, req.body);

    if (!firebaseDb) {
        return res.status(503).json({ success: false, error: 'Firebase not initialized' });
    }

    try {
        const doc = await firebaseDb.collection('bookings').doc(req.params.id).get();
        if (!doc.exists) {
            return res.status(404).json({ success: false, error: 'Booking not found' });
        }

        const updateData = {};
        if (req.body.date) updateData.date = req.body.date;
        if (req.body.time) updateData.time = req.body.time;
        if (req.body.status) updateData.status = req.body.status;

        await firebaseDb.collection('bookings').doc(req.params.id).update(updateData);
        console.log('✅ Booking updated');

        const updatedDoc = await firebaseDb.collection('bookings').doc(req.params.id).get();
        res.json({
            success: true,
            message: 'Booking updated',
            data: { id: updatedDoc.id, ...updatedDoc.data() }
        });
    } catch (error) {
        console.error('❌ Error:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

// ══════════════════════════════════════════════════════════════════
// OFFERS ENDPOINTS (FIREBASE)
// ══════════════════════════════════════════════════════════════════

app.get('/api/offers', async (req, res) => {
    console.log('🎁 GET /api/offers');

    if (!firebaseDb) {
        return res.status(503).json({ success: false, error: 'Firebase not initialized' });
    }

    try {
        const snapshot = await firebaseDb.collection('offers').get();
        const offers = [];
        snapshot.forEach(doc => {
            offers.push({ id: doc.id, ...doc.data() });
        });
        res.json({ success: true, data: offers });
    } catch (error) {
        console.error('❌ Error:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

// ══════════════════════════════════════════════════════════════════
// FIREBASE USERS ENDPOINTS
// ══════════════════════════════════════════════════════════════════

app.get('/api/firebase/users', async (req, res) => {
    console.log('👥 GET /api/firebase/users');

    if (!firebaseDb) {
        return res.status(503).json({
            success: false,
            error: 'Firebase not initialized'
        });
    }

    try {
        const usersSnapshot = await firebaseDb.collection('users').get();
        const users = [];

        usersSnapshot.forEach(doc => {
            users.push({
                uid: doc.id,
                name: doc.data().name || 'N/A',
                email: doc.data().email || 'N/A',
                phone: doc.data().phone || 'N/A',
                avatarUrl: doc.data().avatarUrl || null,
                joinedDate: doc.data().joinedDate || 'N/A'
            });
        });

        console.log(`✅ Fetched ${users.length} users from Firebase`);
        res.json({
            success: true,
            data: users,
            count: users.length,
            message: `Found ${users.length} users in Firebase`
        });
    } catch (error) {
        console.error('❌ Firebase Error:', error.message);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch users: ' + error.message
        });
    }
});

app.get('/api/firebase/user/:uid', async (req, res) => {
    console.log(`👤 GET /api/firebase/user/${req.params.uid}`);

    if (!firebaseDb) {
        return res.status(503).json({
            success: false,
            error: 'Firebase not initialized'
        });
    }

    try {
        const doc = await firebaseDb.collection('users').doc(req.params.uid).get();

        if (!doc.exists) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        const userData = {
            uid: doc.id,
            name: doc.data().name || 'N/A',
            email: doc.data().email || 'N/A',
            phone: doc.data().phone || 'N/A',
            avatarUrl: doc.data().avatarUrl || null,
            joinedDate: doc.data().joinedDate || 'N/A'
        };

        console.log('✅ User found:', userData.name);
        res.json({ success: true, data: userData });
    } catch (error) {
        console.error('❌ Firebase Error:', error.message);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch user: ' + error.message
        });
    }
});

// ══════════════════════════════════════════════════════════════════
// HEALTH CHECK
// ══════════════════════════════════════════════════════════════════

app.get('/api/health', (req, res) => {
    console.log('❤️  Health check');
    res.json({
        success: true,
        message: 'Server is running with Firebase backend',
        timestamp: new Date().toISOString(),
        firebaseConnected: firebaseDb !== null
    });
});

// ══════════════════════════════════════════════════════════════════
// ERROR HANDLING
// ══════════════════════════════════════════════════════════════════

app.use((err, req, res, next) => {
    console.error('❌ Error:', err.stack);
    res.status(500).json({ success: false, error: 'Internal server error' });
});

// ══════════════════════════════════════════════════════════════════
// START SERVER
// ══════════════════════════════════════════════════════════════════

app.listen(PORT, () => {
    console.log('\n');
    console.log('╔════════════════════════════════════════════════════╗');
    console.log('║     ✅ Beauty Parlour API is RUNNING               ║');
    console.log('║     🌐 http://localhost:4444                       ║');
    console.log('║     🔥 Firebase Backend: All Data Persisted        ║');
    console.log('╚════════════════════════════════════════════════════╝');
    console.log('\n');
    console.log('📍 Available Endpoints (All Firebase-backed):');
    console.log('  ✓ GET  http://localhost:4444/api/health');
    console.log('  ✓ GET  http://localhost:4444/api/services');
    console.log('  ✓ GET  http://localhost:4444/api/services/:id');
    console.log('  ✓ GET  http://localhost:4444/api/offers');
    console.log('  ✓ POST http://localhost:4444/api/bookings');
    console.log('  ✓ GET  http://localhost:4444/api/bookings/:userId');
    console.log('  ✓ PUT  http://localhost:4444/api/bookings/:id');
    console.log('  ✓ DELETE http://localhost:4444/api/bookings/:id');
    console.log('\n');
    console.log('👥 Users Endpoints:');
    console.log('  ✓ GET  http://localhost:4444/api/firebase/users');
    console.log('  ✓ GET  http://localhost:4444/api/firebase/user/:uid');
    console.log('\n');
    console.log('📊 Firebase Collections:');
    console.log('  • services');
    console.log('  • offers');
    console.log('  • bookings');
    console.log('  • users');
    console.log('\n');
});
